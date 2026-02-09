package org.bremersee.samba.ad.dc.controller.ui.mapper;

import static java.util.Objects.isNull;

import java.time.OffsetDateTime;
import org.bremersee.samba.ad.dc.controller.ui.model.UserEditModel;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.DomainUserAccountControl;
import org.bremersee.samba.ad.dc.model.ImmutableDomainUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * The user edit model mapper.
 */
@Mapper
public abstract class UserEditModelMapper {

  /**
   * The constant INSTANCE.
   */
  public static final UserEditModelMapper INSTANCE = Mappers.getMapper(UserEditModelMapper.class);

  /**
   * Instantiates a new user edit model mapper.
   */
  protected UserEditModelMapper() {
    super();
  }

  /**
   * Map user edit model.
   *
   * @param source the source
   * @return the user edit model
   */
  @Mapping(target = "newOu", source = "parentDistinguishedNameNormalized")
  @Mapping(target = "enabled", source = "accountControl.enabled")
  @Mapping(
      target = "passwordExpirationEnabled",
      source = "accountControl.passwordExpirationEnabled")
  @Mapping(target = "noExpiry", source = "source", qualifiedByName = "mapNoExpiry")
  @Mapping(target = "avatar", ignore = true)
  @Mapping(target = "removeAvatar", ignore = true)
  @Mapping(target = "renameNamesAutomatically", ignore = true)
  @Mapping(target = "accountExpiresIso", ignore = true)
  public abstract UserEditModel map(DomainUser source);

  /**
   * Map no expiry internal.
   *
   * @param source the source
   * @return the boolean
   */
  @Named("mapNoExpiry")
  boolean mapNoExpiryInternal(DomainUser source) {
    return isNull(source.getAccountExpires());
  }

  /**
   * Merge domain user.
   *
   * @param source the source
   * @param existingDomainUser the existing domain user
   * @return the domain user
   */
  public DomainUser merge(UserEditModel source, DomainUser existingDomainUser) {
    return mergeInternal(source, DomainUser.builder().from(existingDomainUser));
  }

  /**
   * Merge internal domain user.
   *
   * @param source the source
   * @param target the target
   * @return the domain user
   */
  @Mapping(
      target = "accountExpires",
      source = "source",
      qualifiedByName = "mergeAccountExpiresInternal")
  @Mapping(target = "accountControl", source = "source", qualifiedByName = "mergeAccountControl")
  @Mapping(target = "distinguishedName", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "sid", ignore = true)
  @Mapping(target = "criticalSystemObject", ignore = true)
  @Mapping(target = "memberships", ignore = true)
  @Mapping(target = "lastLogon", ignore = true)
  @Mapping(target = "logonCount", ignore = true)
  @Mapping(target = "passwordLastSet", ignore = true)
  abstract DomainUser mergeInternal(
      UserEditModel source,
      @MappingTarget ImmutableDomainUser.Builder target);

  /**
   * Merge account expires internal.
   *
   * @param source the source
   * @return the offset date time
   */
  @Named("mergeAccountExpiresInternal")
  OffsetDateTime mergeAccountExpiresInternal(UserEditModel source) {
    if (source.isNoExpiry()) {
      return null;
    }
    return source.getAccountExpires();
  }

  /**
   * Merge account control internal.
   *
   * @param source the source
   * @return the domain user account control
   */
  @Named("mergeAccountControl")
  DomainUserAccountControl mergeAccountControlInternal(UserEditModel source) {
    if (isNull(source)) {
      return null;
    }
    return DomainUserAccountControl.builder()
        .enabled(source.isEnabled())
        .passwordExpirationEnabled(source.isPasswordExpirationEnabled())
        .build();
  }

}
