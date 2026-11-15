/*
 * Copyright 2025-2026  the original author or authors.
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

package org.bremersee.samba.ad.dc.repository.mock;

import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bremersee.ldaptive.transcoder.UserAccountControl;
import org.bremersee.samba.ad.dc.config.MockProperties;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.Sid;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.LdapEntry;
import org.ldaptive.dn.Dn;
import org.springframework.util.Assert;

/**
 * The ldap entry factory.
 *
 * @author Christian Bremer
 */
class LdapEntryFactory {

  private final SambaStore store;

  /**
   * Instantiates a new ldap entry factory.
   *
   * @param store the store
   */
  LdapEntryFactory(SambaStore store) {
    this.store = store;
  }

  /**
   * Sets default values.
   *
   * @param entry the entry
   */
  void setDefaultValues(LdapEntry entry) {
    Assert.hasText(entry.getDn(), "DN must be present.");
    Dn dn = new Dn(entry.getDn());
    Set<String> objectClasses = AdConstants.OBJECT_CLASS.getValues(entry)
        .collect(Collectors.toSet());
    Assert.notEmpty(objectClasses, "Object classes have to be present.");
    AdConstants.DN.setValue(entry, dn);
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    AdConstants.WHEN_CREATED.setValue(entry, now);
    AdConstants.WHEN_CHANGED.setValue(entry, now);
    AdConstants.NAME.setValue(entry, dn.getRDn().getNameValue().getStringValue());
    if (objectClasses.contains(AdConstants.OBJECT_CLASS_GROUP)) {
      AdConstants.OBJECT_SID.setValue(entry, store.getNextSid());
    } else if (objectClasses.contains(AdConstants.OBJECT_CLASS_USER)) {
      AdConstants.USER_LAST_LOGON.setValue(entry, now);
      AdConstants.USER_LOGON_COUNT.setValue(entry, 0);
      AdConstants.OBJECT_SID.setValue(entry, store.getNextSid());
      AdConstants.PRIMARY_GROUP_ID.setValue(entry, 513);
      AdConstants.USER_PWD_LAST_SET.setValue(entry, now);
      AdConstants.USER_USER_ACCOUNT_CONTROL
          .setValue(entry, new UserAccountControl(), (e, n) -> isEmpty(e));
      AdConstants.USER_UNICODE_PWD.setValue(entry, MockProperties.ADMIN_FALLBACK_PASSWORD);
    }
  }

  /**
   * New entry ldap entry.
   *
   * @param rdn the rdn
   * @param parentDn the parent dn
   * @return the ldap entry
   */
  LdapEntry newEntry(String rdn, Dn parentDn) {
    Dn dn = new Dn(rdn);
    dn.add(store.getDnTool().addBaseDn(parentDn));
    LdapEntry entry = new LdapEntry();
    entry.setDn(dn.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER));
    AdConstants.DN.setValue(entry, dn);
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    AdConstants.WHEN_CREATED.setValue(entry, now);
    AdConstants.WHEN_CHANGED.setValue(entry, now);
    return entry;
  }

  /**
   * New organizational unit entry ldap entry.
   *
   * @param name the name
   * @param parentDn the parent dn
   * @return the ldap entry
   */
  LdapEntry newOrganizationalUnitEntry(String name, Dn parentDn) {
    LdapEntry entry = newEntry("OU=" + name, parentDn);
    AdConstants.OBJECT_CLASS.setValues(entry, List.of(
        AdConstants.OBJECT_CLASS_OU, "top"
    ));
    AdConstants.NAME.setValue(entry, name);
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(entry, false);
    return entry;
  }

  /**
   * New group entry ldap entry.
   *
   * @param samAccountName the sam account name
   * @param parentDn the parent dn
   * @param groupType the group type
   * @param sid the sid
   * @return the ldap entry
   */
  LdapEntry newGroupEntry(
      String samAccountName, Dn parentDn,
      int groupType,
      Sid sid) {
    LdapEntry entry = newEntry("CN=" + samAccountName, parentDn);
    AdConstants.OBJECT_CLASS.setValues(entry, List.of(
        AdConstants.OBJECT_CLASS_GROUP, "top"
    ));
    AdConstants.GROUP_TYPE.setValue(entry, groupType);
    AdConstants.NIS_NAME.setValue(entry, samAccountName);
    AdConstants.NAME.setValue(entry, samAccountName);
    AdConstants.OBJECT_SID.setValue(entry, requireNonNullElseGet(sid, store::getNextSid));
    AdConstants.SAM_ACCOUNT_NAME.setValue(entry, samAccountName);
    return entry;
  }

  /**
   * New user entry ldap entry.
   *
   * @param samAccountName the sam account name
   * @param parentDn the parent dn
   * @param primaryGroupId the primary group id
   * @param sid the sid
   * @return the ldap entry
   */
  LdapEntry newUserEntry(
      String samAccountName,
      Dn parentDn,
      Integer primaryGroupId,
      Sid sid) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    LdapEntry entry = newEntry("CN=" + samAccountName, parentDn);
    AdConstants.OBJECT_CLASS.setValues(entry, List.of(
        "organizationalPerson", "person", "top", "user"
    ));
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(entry, false);
    AdConstants.USER_LAST_LOGON.setValue(entry, now);
    AdConstants.USER_LOGON_COUNT.setValue(entry, 0);
    AdConstants.NAME.setValue(entry, samAccountName);
    AdConstants.OBJECT_SID.setValue(entry, requireNonNullElseGet(sid, store::getNextSid));
    AdConstants.PRIMARY_GROUP_ID.setValue(entry, requireNonNullElse(primaryGroupId, 513));
    AdConstants.USER_PWD_LAST_SET.setValue(entry, now);
    AdConstants.SAM_ACCOUNT_NAME.setValue(entry, samAccountName);
    AdConstants.USER_USER_ACCOUNT_CONTROL.setValue(entry, new UserAccountControl());
    AdConstants.USER_UNICODE_PWD.setValue(entry, MockProperties.ADMIN_FALLBACK_PASSWORD);
    return entry;
  }

  /**
   * New computer entry ldap entry.
   *
   * @param name the name
   * @param parentDn the parent dn
   * @param primaryGroupId the primary group id
   * @param sid the sid
   * @return the ldap entry
   */
  LdapEntry newComputerEntry(
      String name,
      Dn parentDn,
      int primaryGroupId,
      Sid sid) {
    LdapEntry entry = newUserEntry(name, parentDn, primaryGroupId, sid);
    AdConstants.OBJECT_CLASS.setValues(entry, List.of(
        "computer", "organizationalPerson", "person", "top", "user"
    ));
    AdConstants.USER_DISPLAY_NAME.setValue(entry, name + '$');
    AdConstants.SAM_ACCOUNT_NAME.setValue(entry, name + '$');
    return entry;
  }

}
