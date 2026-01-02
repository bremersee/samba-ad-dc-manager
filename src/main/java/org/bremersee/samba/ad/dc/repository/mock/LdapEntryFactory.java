package org.bremersee.samba.ad.dc.repository.mock;

import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;

import java.time.OffsetDateTime;
import java.util.List;
import org.bremersee.ldaptive.transcoder.UserAccountControl;
import org.bremersee.samba.ad.dc.model.Sid;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.LdapEntry;
import org.ldaptive.dn.Dn;

@SuppressWarnings("ClassCanBeRecord")
class LdapEntryFactory {

  private final SambaStore store;

  LdapEntryFactory(SambaStore store) {
    this.store = store;
  }

  LdapEntry newEntry(String rdn, Dn parentDn) {
    Dn dn = new Dn(rdn);
    dn.add(store.getDnTool().addBaseDn(parentDn));
    LdapEntry entry = new LdapEntry();
    entry.setDn(rdn);
    AdConstants.DN.setValue(entry, dn);
    OffsetDateTime now = OffsetDateTime.now();
    AdConstants.WHEN_CREATED.setValue(entry, now);
    AdConstants.WHEN_CHANGED.setValue(entry, now);
    return entry;
  }

  LdapEntry newOrganizationalUnitEntry(String name, Dn parentDn) {
    LdapEntry entry = newEntry("OU=" + name, parentDn);
    AdConstants.OBJECT_CLASS.setValues(entry, List.of(
        "organizationalUnit", "top"
    ));
    AdConstants.NAME.setValue(entry, name);
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(entry, false);
    return entry;
  }

  LdapEntry newGroupEntry(
      String samAccountName, Dn parentDn,
      int groupType,
      Sid sid) {
    LdapEntry entry = newEntry("CN=" + samAccountName, parentDn);
    AdConstants.OBJECT_CLASS.setValues(entry, List.of(
        "group", "top"
    ));
    AdConstants.GROUP_TYPE.setValue(entry, groupType);
    AdConstants.NIS_NAME.setValue(entry, samAccountName);
    AdConstants.NAME.setValue(entry, samAccountName);
    AdConstants.OBJECT_SID.setValue(entry, requireNonNullElseGet(sid, store::getNextSid));
    AdConstants.SAM_ACCOUNT_NAME.setValue(entry, samAccountName);
    return entry;
  }

  LdapEntry newUserEntry(
      String samAccountName,
      Dn parentDn,
      Integer primaryGroupId,
      Sid sid) {
    OffsetDateTime now = OffsetDateTime.now();
    LdapEntry entry = newEntry("CN=" + samAccountName, parentDn);
    AdConstants.OBJECT_CLASS.setValues(entry, List.of(
        "organizationalPerson", "person", "top", "user"
    ));
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(entry, false);
    AdConstants.USER_LAST_LOGON.setValue(entry, now);
    AdConstants.USER_LOGON_COUNT.setValue(entry, 1);
    AdConstants.NAME.setValue(entry, samAccountName);
    AdConstants.OBJECT_SID.setValue(entry, requireNonNullElseGet(sid, store::getNextSid));
    AdConstants.PRIMARY_GROUP_ID.setValue(entry, requireNonNullElse(primaryGroupId, 513));
    AdConstants.USER_PWD_LAST_SET.setValue(entry, now);
    AdConstants.SAM_ACCOUNT_NAME.setValue(entry, samAccountName);
    AdConstants.USER_USER_ACCOUNT_CONTROL.setValue(entry, new UserAccountControl());
    return entry;
  }

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
