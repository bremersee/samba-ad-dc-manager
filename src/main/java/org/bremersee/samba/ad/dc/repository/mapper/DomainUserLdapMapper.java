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

package org.bremersee.samba.ad.dc.repository.mapper;

import static java.util.Objects.isNull;
import static org.bremersee.ldaptive.LdaptiveEntryMapper.getAttributeValue;
import static org.bremersee.ldaptive.LdaptiveEntryMapper.setAttribute;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.ldaptive.transcoder.UserAccountControl;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.DomainUserAccountControl;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;

/**
 * The domain user ldap mapper.
 *
 * @author Christian Bremer
 */
@Slf4j
public class DomainUserLdapMapper extends SamAccountLdapMapper<DomainUser>
    implements LdaptiveEntryMapper<DomainUser> {

  private final Supplier<Boolean> rfc2307EnabledSupplier;

  @Getter(AccessLevel.PROTECTED)
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public DomainUserLdapMapper(Supplier<Boolean> rfc2307EnabledSupplier) {
    super(DomainUser::new);
    this.rfc2307EnabledSupplier = rfc2307EnabledSupplier;
    mappedAttributes = initMappedAttributesOfDomainUser();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfDomainUser() {
    Set<LdaptiveAttribute<?>> attributeNames = new LinkedHashSet<>(super.getMappedAttributes());
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
  public void map(LdapEntry source, DomainUser destination) {

    if (isNull(source) || isNull(destination)) {
      return;
    }
    super.map(source, destination);

    OffsetDateTime accountExpires = getAttributeValue(
        source, AdConstants.USER_ACCOUNT_EXPIRES, null);
    destination.setAccountExpires(accountExpires);

    String company = getAttributeValue(source, AdConstants.USER_COMPANY, null);
    destination.setCompany(company);

    String department = getAttributeValue(source, AdConstants.USER_DEPARTMENT, null);
    destination.setDepartment(department);

    String description = getAttributeValue(source, AdConstants.DESCRIPTION, null);
    destination.setDescription(description);

    String displayName = getAttributeValue(
        source, AdConstants.USER_DISPLAY_NAME, null);
    destination.setDisplayName(displayName);

    String gecos = getAttributeValue(source, AdConstants.USER_GECOS, null);
    destination.setGecos(gecos);

    Integer gidNumber = getAttributeValue(source, AdConstants.GID_NUMBER, null);
    destination.setGidNumber(gidNumber);

    String firstName = getAttributeValue(source, AdConstants.USER_GIVEN_NAME, null);
    destination.setFirstName(firstName);

    String homeDirectory = getAttributeValue(source, AdConstants.USER_HOME_DIRECTORY,
        null);
    destination.setHomeDirectory(homeDirectory);

    String homeDrive = getAttributeValue(source, AdConstants.USER_HOME_DRIVE, null);
    destination.setHomeDrive(homeDrive);

    String initials = getAttributeValue(source, AdConstants.USER_INITIALS, null);
    destination.setInitials(initials);

    OffsetDateTime lastLogon = getAttributeValue(
        source, AdConstants.USER_LAST_LOGON, null);
    destination.setLastLogon(lastLogon);

    String loginShell = getAttributeValue(source, AdConstants.USER_LOGIN_SHELL, null);
    destination.setLoginShell(loginShell);

    Integer logonCount = getAttributeValue(source, AdConstants.USER_LOGON_COUNT, null);
    destination.setLogonCount(logonCount);

    String email = getAttributeValue(source, AdConstants.MAIL, null);
    destination.setEmail(email);

    String mobile = getAttributeValue(source, AdConstants.USER_MOBILE, null);
    destination.setMobile(mobile);

    String nisDomain = getAttributeValue(source, AdConstants.NIS_DOMAIN, null);
    destination.setNisDomain(nisDomain);

    String officeName = getAttributeValue(source, AdConstants.USER_OFFICE_NAME, null);
    destination.setPhysicalDeliveryOfficeName(officeName);

    String preferredLanguage = getAttributeValue(
        source, AdConstants.USER_PREFERRED_LANGUAGE, null);
    destination.setPreferredLanguage(preferredLanguage);

    String profilePath = getAttributeValue(
        source, AdConstants.USER_PROFILE_PATH, null);
    destination.setProfilePath(profilePath);

    OffsetDateTime pwdLastSet = getAttributeValue(
        source, AdConstants.USER_PWD_LAST_SET, null);
    destination.setPasswordLastSet(pwdLastSet);

    String scriptPath = getAttributeValue(source, AdConstants.USER_SCRIPT_PATH, null);
    destination.setScriptPath(scriptPath);

    String lastName = getAttributeValue(source, AdConstants.USER_SN, null);
    destination.setLastName(lastName);

    String telephoneNumber = getAttributeValue(source,
        AdConstants.USER_TELEPHONE_NUMBER, null);
    destination.setTelephoneNumber(telephoneNumber);

    String title = getAttributeValue(source, AdConstants.USER_TITLE, null);
    destination.setTitle(title);

    String uid = getAttributeValue(source, AdConstants.USER_UID, null);
    String nisName = getAttributeValue(source, AdConstants.NIS_NAME, uid);
    destination.setUid(nisName);

    AdConstants.USER_UID_NUMBER
        .getValue(source)
        .consume(destination::setUidNumber);

    String unixHomeDirectory = getAttributeValue(
        source, AdConstants.USER_UNIX_HOME_DIRECTORY, null);
    destination.setUnixHomeDirectory(unixHomeDirectory);

    String userPrincipalName = getAttributeValue(source,
        AdConstants.USER_PRINCIPAL_NAME, null);
    destination.setUserPrincipalName(userPrincipalName);

    UserAccountControl accountControl = getAttributeValue(
        source, AdConstants.USER_USER_ACCOUNT_CONTROL, new UserAccountControl());
    destination.setAccountControl(DomainUserAccountControl.from(accountControl));
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(
      DomainUser source,
      LdapEntry destination) {

    if (isNull(source) || isNull(destination)) {
      return new AttributeModification[0];
    }
    var modifications = toNewList(super.mapAndComputeModifications(source, destination));

    // TODO can I set it?
    // OffsetDateTime accountExpires = source.getAccountExpires();

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
        .map(DomainUserAccountControl::toUserAccountControl)
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
