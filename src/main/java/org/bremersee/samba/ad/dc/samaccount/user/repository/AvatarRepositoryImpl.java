package org.bremersee.samba.ad.dc.samaccount.user.repository;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.common.repository.AdConstants;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.common.converter.TreeSearchScopeConverter;
import org.bremersee.samba.ad.dc.samaccount.user.model.AvatarDefault;
import org.bremersee.samba.ad.dc.samaccount.user.model.DomainUser;
import org.bremersee.samba.ad.dc.common.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.samaccount.common.repository.SamAccountRepository;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModification.Type;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyRequest;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.EqualityFilter;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.OrFilter;
import org.springframework.stereotype.Component;

@Component("avatarRepository")
@Slf4j
public class AvatarRepositoryImpl extends SamAccountRepository
    implements AvatarRepository {

  private final List<AvatarProvider> avatarProviders;

  private final ImageTool imageTool;

  AvatarRepositoryImpl(
      DomainControllerProperties properties,
      LdaptiveTemplate ldapTemplate,
      List<AvatarProvider> avatarProviders,
      ImageTool imageTool) {
    super(properties, ldapTemplate);
    this.avatarProviders = avatarProviders;
    this.imageTool = imageTool;
  }

  @Override
  protected Dn getDefaultOu() {
    return getProperties().getUser().getDefaultOu();
  }

  @Override
  protected String getObjectClassValue() {
    return AdConstants.OBJECT_CLASS_USER;
  }

  @Override
  protected String[] getBinaryAttributes() {
    return new String[]{AdConstants.USER_JPEG_PHOTO.getName()};
  }

  @Override
  protected String[] getReturnAttributes() {
    return new String[]{
        AdConstants.USER_JPEG_PHOTO.getName(),
        AdConstants.MAIL.getName(),
    };
  }

  @Override
  public boolean existsAvatarInActiveDirectory(String user, Dn ou, TreeSearchScope searchScope) {
    log.debug("existsAvatarInActiveDirectory({}, {}, {})", user, ou, searchScope);
    return findLdapEntryForAvatar(user, ou, searchScope)
        .map(ldapEntry -> ldapEntry.getAttribute(AdConstants.USER_JPEG_PHOTO.getName()))
        .map(LdapAttribute::getBinaryValue)
        .map(this::isAvatarNotEmpty)
        .orElse(false);
  }

  @Override
  public Optional<byte[]> findAvatar(String userNameOrEmail, Dn ou, TreeSearchScope searchScope,
      AvatarDefault avatarDefault, Integer size) {

    log.debug("findAvatar({}, {}, {})", userNameOrEmail, avatarDefault, size);
    int avatarSize = getAvatarSize(size);
    return findLdapEntryForAvatar(userNameOrEmail, ou, searchScope)
        .flatMap(ldapEntry -> findAvatar(ldapEntry)
            .filter(this::isAvatarNotEmpty)
            .map(avatar -> scaleAvatarForDisplaying(avatar, avatarSize))
            .filter(this::isAvatarNotEmpty)
            .or(() -> {
              String mail = getEmail(ldapEntry, userNameOrEmail);
              return findAvatarsOfProviders(mail, avatarDefault, avatarSize)
                  .findFirst();
            }))
        .or(() -> findAvatarsOfProviders(userNameOrEmail, avatarDefault, avatarSize)
            .findFirst());
  }

  private Optional<byte[]> findAvatar(LdapEntry ldapEntry) {
    return Optional.ofNullable(AdConstants.USER_JPEG_PHOTO.getValue(ldapEntry).get());
  }

  private Stream<byte[]> findAvatarsOfProviders(String user, AvatarDefault avatarDefault,
      Integer size) {
    log.debug("findAvatarsOfProviders({}, {}, {})", user, avatarDefault, size);
    return avatarProviders.stream()
        .flatMap(provider -> provider.findAvatar(user, avatarDefault, size).stream())
        .filter(this::isAvatarNotEmpty);
  }

  private byte[] scaleAvatarForDisplaying(byte[] avatar, int avatarSize) {
    try {
      BufferedImage img = imageTool.toSquareImage(avatar);
      BufferedImage scaledImg = imageTool
          .scaleImage(img, new Dimension(avatarSize, avatarSize));
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ImageIO.write(scaledImg, "JPG", out);
      return out.toByteArray();

    } catch (IOException e) {
      log.error("Creating image from ldap attribute {} failed.",
          AdConstants.USER_JPEG_PHOTO.getName(), e);
      return new byte[0];
    }
  }

  private byte[] scaleAvatarForWriting(InputStream avatar) {
    try (InputStream in = avatar) {
      BufferedImage img = ImageIO.read(in);
      int width = img.getWidth();
      int height = img.getHeight();
      int max = Math.max(width, height);
      if (max > MAX_AVATAR_SIZE) {
        float factor = Integer.valueOf(MAX_AVATAR_SIZE).floatValue() / max;
        width = Math.min(Math.round(factor * width), MAX_AVATAR_SIZE);
        height = Math.min(Math.round(factor * height), MAX_AVATAR_SIZE);
        img = imageTool.scaleImage(img, new Dimension(width, height));
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ImageIO.write(img, "jpg", out);
      return out.toByteArray();
    } catch (IOException e) {
      throw ServiceException.internalServerError(
          "Saving avatar failed.",
          EC_SAVING_AVATAR_FAILED,
          e);
    }
  }

  private String getEmail(LdapEntry ldapEntry, String defaultEmail) {
    return AdConstants.MAIL.getValue(ldapEntry, defaultEmail).get();
  }

  private Optional<LdapEntry> findLdapEntryForAvatar(
      String userNameOrEmail,
      Dn ou,
      TreeSearchScope searchScope) {

    if (isEmpty(userNameOrEmail)) {
      log.debug("Avatar not found because userNameOrEmail is empty.");
      return Optional.empty();
    }
    Filter objectClassFilter = new EqualityFilter(
        AdConstants.OBJECT_CLASS.getName(),
        getObjectClassValue());
    Filter nameFilter = new EqualityFilter(getUniqueNameAttributeName(), userNameOrEmail);
    Filter emailFilter = new EqualityFilter(AdConstants.MAIL.getName(), userNameOrEmail);
    Filter orFilter = new OrFilter(nameFilter, emailFilter);
    Filter filter = new AndFilter(objectClassFilter, orFilter);
    SearchScope scope = TreeSearchScopeConverter.toSearchScope(searchScope);
    SearchRequest searchRequest = searchOneRequest(
        userNameOrEmail,
        ou,
        filter,
        scope);
    log.debug("findAvatar, searchRequest = {}", searchRequest);
    return getLdapTemplate()
        .findOne(searchRequest)
        .filter(getIgnoredEntryFilter(ou, scope));
  }

  @Override
  public void saveAvatar(String userName, InputStream avatar) {
    log.debug("saveAvatar({}, InputStream)", userName);
    LdapAttribute ldapAttribute = AdConstants.USER_JPEG_PHOTO
        .createAttribute(scaleAvatarForWriting(avatar));
    modifyAvatar(userName, ldapAttribute, Type.REPLACE);
  }

  @Override
  public void removeAvatar(String userName) {
    log.debug("removeAvatar({})", userName);
    modifyAvatar(userName, AdConstants.USER_JPEG_PHOTO.createAttribute(), Type.DELETE);
  }

  private void modifyAvatar(
      String userName,
      LdapAttribute ldapAttribute,
      AttributeModification.Type modificationType) {

    AttributeModification mod = new AttributeModification(modificationType, ldapAttribute);
    findDnOfSamAccountName(userName).ifPresentOrElse(dn -> {
          ModifyRequest modifyRequest = ModifyRequest.builder()
              .dn(dn)
              .modifications(mod)
              .build();
          getLdapTemplate().modify(modifyRequest);
        },
        () -> {
          throw ServiceException.notFoundWithErrorCode(
              DomainUser.class.getSimpleName(),
              userName,
              EC_SAM_ACCOUNT_NOT_FOUND);
        });
  }

}
