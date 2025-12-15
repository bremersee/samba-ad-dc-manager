/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.samba.ad.dc.samaccount.user.repository.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.bremersee.ldaptive.LdaptiveEntryMapper.setAttribute;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveEntryImmutableMapper;
import org.bremersee.ldaptive.transcoder.UserAccountControl;
import org.bremersee.samba.ad.dc.common.repository.AdConstants;
import org.bremersee.samba.ad.dc.samaccount.common.repository.mapper.SamAccountLdapMapper;
import org.bremersee.samba.ad.dc.samaccount.user.model.DomainUser;
import org.bremersee.samba.ad.dc.samaccount.user.model.DomainUserAccountControl;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.springframework.util.Assert;

/**
 * The domain user ldap mapper.
 *
 * @author Christian Bremer
 */
@Slf4j
public class DomainUserLdapMapper extends LdaptiveEntryImmutableMapper<DomainUser> {

  // Will be encoded as '0'.
  private static final OffsetDateTime NEVER_EXPIRES = OffsetDateTime.parse("1601-01-01T00:00:00Z");

  // Actually max is Long.MAX_VALUE, year > 30000. Year 9999 should be great enough.
  private static final OffsetDateTime MAX_EXPIRES = OffsetDateTime.parse("9999-01-01T00:00:00Z");

  private final Supplier<Boolean> rfc2307EnabledSupplier;

  private final SamAccountLdapMapper samAccountLdapMapper;

  @Getter
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public DomainUserLdapMapper(Supplier<Boolean> rfc2307EnabledSupplier) {
    this.rfc2307EnabledSupplier = rfc2307EnabledSupplier;
    samAccountLdapMapper = new SamAccountLdapMapper();
    mappedAttributes = initMappedAttributesOfDomainUser();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfDomainUser() {
    var attributeNames = new LinkedHashSet<>(samAccountLdapMapper.getMappedAttributes());
    attributeNames.add(AdConstants.USER_ACCOUNT_EXPIRES);
    attributeNames.add(AdConstants.USER_COMPANY);
    attributeNames.add(AdConstants.USER_DEPARTMENT);
    attributeNames.add(AdConstants.DESCRIPTION);
    attributeNames.add(AdConstants.USER_DISPLAY_NAME);
    attributeNames.add(AdConstants.USER_GECOS);
    attributeNames.add(AdConstants.GID_NUMBER);
    attributeNames.add(AdConstants.USER_GIVEN_NAME);
    attributeNames.add(AdConstants.USER_HOME_DIRECTORY);
    attributeNames.add(AdConstants.USER_HOME_DRIVE);
    attributeNames.add(AdConstants.USER_INITIALS);
    attributeNames.add(AdConstants.USER_LAST_LOGON);
    attributeNames.add(AdConstants.USER_LOGIN_SHELL);
    attributeNames.add(AdConstants.USER_LOGON_COUNT);
    attributeNames.add(AdConstants.MAIL);
    attributeNames.add(AdConstants.USER_MOBILE);
    attributeNames.add(AdConstants.NIS_DOMAIN);
    attributeNames.add(AdConstants.USER_OFFICE_NAME);
    attributeNames.add(AdConstants.USER_PREFERRED_LANGUAGE);
    attributeNames.add(AdConstants.USER_PROFILE_PATH);
    attributeNames.add(AdConstants.USER_PWD_LAST_SET);
    attributeNames.add(AdConstants.USER_SCRIPT_PATH);
    attributeNames.add(AdConstants.USER_SN);
    attributeNames.add(AdConstants.USER_TELEPHONE_NUMBER);
    attributeNames.add(AdConstants.USER_TITLE);
    attributeNames.add(AdConstants.USER_UID);
    attributeNames.add(AdConstants.USER_UID_NUMBER);
    attributeNames.add(AdConstants.NIS_NAME);
    attributeNames.add(AdConstants.USER_UNIX_HOME_DIRECTORY);
    attributeNames.add(AdConstants.USER_PRINCIPAL_NAME);
    attributeNames.add(AdConstants.USER_USER_ACCOUNT_CONTROL);
    return attributeNames;
  }

  @Override
  public String[] getObjectClasses() {
    return new String[0];
  }

  @Override
  public String[] getMappedAttributeNames() {
    return getMappedAttributes().stream()
        .map(LdaptiveAttribute::getName)
        .toArray(String[]::new);
  }

  @Override
  public String[] getBinaryAttributeNames() {
    return getMappedAttributes().stream()
        .filter(LdaptiveAttribute::isBinary)
        .map(LdaptiveAttribute::getName)
        .toArray(String[]::new);
  }

  @Override
  public String mapDn(DomainUser domainObject) {
    Assert.hasText(domainObject.getDistinguishedName(), "DN of ldap entry is required.");
    return domainObject.getDistinguishedName();
  }

  @Override
  public DomainUser map(LdapEntry source) {
    if (isEmpty(source)) {
      return null;
    }
    var builder = DomainUser.builder()
        .from(samAccountLdapMapper.map(source));
    AdConstants.USER_ACCOUNT_EXPIRES
        .getValue(source)
        .filter(expires -> expires.isBefore(MAX_EXPIRES))
        .ifPresent(builder::accountExpires);
    AdConstants.USER_COMPANY
        .getValue(source)
        .ifPresent(builder::company);
    AdConstants.USER_DEPARTMENT
        .getValue(source)
        .ifPresent(builder::department);
    AdConstants.DESCRIPTION
        .getValue(source)
        .ifPresent(builder::description);
    AdConstants.USER_DISPLAY_NAME
        .getValue(source)
        .ifPresent(builder::displayName);
    AdConstants.USER_GECOS
        .getValue(source)
        .ifPresent(builder::gecos);
    AdConstants.GID_NUMBER
        .getValue(source)
        .ifPresent(builder::gidNumber);
    AdConstants.USER_GIVEN_NAME
        .getValue(source)
        .ifPresent(builder::firstName);
    AdConstants.USER_HOME_DIRECTORY
        .getValue(source)
        .ifPresent(builder::homeDirectory);
    AdConstants.USER_HOME_DRIVE
        .getValue(source)
        .ifPresent(builder::homeDrive);
    AdConstants.USER_INITIALS
        .getValue(source)
        .ifPresent(builder::initials);
    AdConstants.USER_LAST_LOGON
        .getValue(source)
        .ifPresent(builder::lastLogon);
    AdConstants.USER_LOGIN_SHELL
        .getValue(source)
        .ifPresent(builder::loginShell);
    AdConstants.USER_LOGON_COUNT
        .getValue(source)
        .ifPresent(builder::logonCount);
    AdConstants.MAIL
        .getValue(source)
        .ifPresent(builder::email);
    AdConstants.USER_MOBILE
        .getValue(source)
        .ifPresent(builder::mobile);
    AdConstants.NIS_DOMAIN
        .getValue(source)
        .ifPresent(builder::nisDomain);
    AdConstants.USER_OFFICE_NAME
        .getValue(source)
        .ifPresent(builder::physicalDeliveryOfficeName);
    AdConstants.USER_PREFERRED_LANGUAGE
        .getValue(source)
        .ifPresent(builder::preferredLanguage);
    AdConstants.USER_PROFILE_PATH
        .getValue(source)
        .ifPresent(builder::profilePath);
    AdConstants.USER_PWD_LAST_SET
        .getValue(source)
        .ifPresent(builder::passwordLastSet);
    AdConstants.USER_SCRIPT_PATH
        .getValue(source)
        .ifPresent(builder::scriptPath);
    AdConstants.USER_SN
        .getValue(source)
        .ifPresent(builder::lastName);
    AdConstants.USER_TELEPHONE_NUMBER
        .getValue(source)
        .ifPresent(builder::telephoneNumber);
    AdConstants.USER_TITLE
        .getValue(source)
        .ifPresent(builder::title);
    AdConstants.USER_UID
        .getValue(source, AdConstants.NIS_NAME.getValue(source).orElse(null))
        .ifPresent(builder::uid);
    AdConstants.USER_UID_NUMBER
        .getValue(source)
        .ifPresent(builder::uidNumber);
    AdConstants.USER_UNIX_HOME_DIRECTORY
        .getValue(source)
        .ifPresent(builder::unixHomeDirectory);
    AdConstants.USER_PRINCIPAL_NAME
        .getValue(source)
        .ifPresent(builder::userPrincipalName);
    AdConstants.USER_USER_ACCOUNT_CONTROL
        .getValue(source)
        .ifPresent(accountControl -> builder
            .accountControl(DomainUserAccountControl.from(accountControl)));
    return builder.build();
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(
      DomainUser source,
      LdapEntry destination) {

    if (isNull(source) || isNull(destination)) {
      return new AttributeModification[0];
    }
    var modifications = new ArrayList<>(Arrays.asList(samAccountLdapMapper
        .mapAndComputeModifications(source, destination)));

    Optional.ofNullable(source.getAccountExpires())
        .filter(expires -> expires.isBefore(MAX_EXPIRES))
        .ifPresentOrElse(
            expires -> AdConstants.USER_ACCOUNT_EXPIRES.setValue(destination, expires),
            () -> AdConstants.USER_ACCOUNT_EXPIRES.setValue(
                destination,
                NEVER_EXPIRES,
                // The value decoder interprets '0' as null. But null would remove the attribute
                // and that is not allowed.
                (oldValue, newValue) -> nonNull(oldValue)));

    String company = source.getCompany();
    setAttribute(destination, AdConstants.USER_COMPANY, company, modifications);

    String department = source.getDepartment();
    setAttribute(destination, AdConstants.USER_DEPARTMENT, department, modifications);

    String description = source.getDescription();
    setAttribute(destination, AdConstants.DESCRIPTION, description, modifications);

    String displayName = source.getDisplayName();
    if (isEmpty(displayName) && !isEmpty(source.getFirstName()) && !isEmpty(source.getLastName())) {
      displayName = source.getFirstName() + " " + source.getLastName();
    }
    setAttribute(destination, AdConstants.USER_DISPLAY_NAME, displayName,
        modifications);

    String firstName = source.getFirstName();
    setAttribute(destination, AdConstants.USER_GIVEN_NAME, firstName, modifications);

    String homeDirectory = source.getHomeDirectory();
    setAttribute(
        destination,
        AdConstants.USER_HOME_DIRECTORY,
        homeDirectory,
        modifications);

    String homeDrive = source.getHomeDrive();
    setAttribute(destination, AdConstants.USER_HOME_DRIVE, homeDrive, modifications);

    String initials = source.getInitials();
    setAttribute(destination, AdConstants.USER_INITIALS, initials, modifications);

    String email = source.getEmail();
    setAttribute(destination, AdConstants.MAIL, email, modifications);

    String mobile = source.getMobile();
    setAttribute(destination, AdConstants.USER_MOBILE, mobile, modifications);

    String officeName = source.getPhysicalDeliveryOfficeName();
    setAttribute(destination, AdConstants.USER_OFFICE_NAME, officeName, modifications);

    String preferredLanguage = source.getPreferredLanguage();
    setAttribute(
        destination,
        AdConstants.USER_PREFERRED_LANGUAGE,
        preferredLanguage,
        modifications);

    String profilePath = source.getProfilePath();
    setAttribute(
        destination,
        AdConstants.USER_PROFILE_PATH,
        profilePath,
        modifications);

    String scriptPath = source.getScriptPath();
    setAttribute(destination, AdConstants.USER_SCRIPT_PATH, scriptPath, modifications);

    String lastName = source.getLastName();
    setAttribute(destination, AdConstants.USER_SN, lastName, modifications);

    String telephoneNumber = source.getTelephoneNumber();
    setAttribute(
        destination,
        AdConstants.USER_TELEPHONE_NUMBER,
        telephoneNumber,
        modifications);

    String title = source.getTitle();
    setAttribute(destination, AdConstants.USER_TITLE, title, modifications);

    String userPrincipalName = source.getUserPrincipalName();
    setAttribute(destination, AdConstants.USER_PRINCIPAL_NAME, userPrincipalName,
        modifications);

    UserAccountControl userAccountControl = Optional.ofNullable(source.getAccountControl())
        .map(DomainUserAccountControl::getUserAccountControl)
        .orElseGet(UserAccountControl::new);
    setAttribute(
        destination,
        AdConstants.USER_USER_ACCOUNT_CONTROL,
        userAccountControl,
        modifications);

    if (Boolean.TRUE.equals(rfc2307EnabledSupplier.get())) {
      String gecos = Optional.ofNullable(source.getGecos()).orElse(displayName);
      setAttribute(destination, AdConstants.USER_GECOS, gecos, modifications);

      Integer gidNumber = source.getGidNumber();
      setAttribute(destination, AdConstants.GID_NUMBER, gidNumber, modifications);

      String loginShell = source.getLoginShell();
      setAttribute(
          destination,
          AdConstants.USER_LOGIN_SHELL,
          loginShell,
          modifications);

      String nisDomain = source.getNisDomain();
      setAttribute(destination, AdConstants.NIS_DOMAIN, nisDomain, modifications);

      String uid = source.getUid();
      setAttribute(destination, AdConstants.USER_UID, uid, modifications);
      setAttribute(destination, AdConstants.NIS_NAME, uid, modifications);

      Integer uidNumber = source.getUidNumber();
      setAttribute(destination, AdConstants.USER_UID_NUMBER, uidNumber, modifications);

      String unixHomeDirectory = source.getUnixHomeDirectory();
      setAttribute(
          destination,
          AdConstants.USER_UNIX_HOME_DIRECTORY,
          unixHomeDirectory,
          modifications);
    }

    return modifications.toArray(new AttributeModification[0]);
  }

}
