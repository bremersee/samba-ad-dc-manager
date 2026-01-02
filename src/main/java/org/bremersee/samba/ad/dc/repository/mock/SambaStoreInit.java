package org.bremersee.samba.ad.dc.repository.mock;

import java.util.List;
import org.bremersee.ldaptive.transcoder.UserAccountControl;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.LdapEntry;
import org.ldaptive.dn.Dn;

class SambaStoreInit {

  private final SambaStore store;

  private final LdapEntryFactory entryFactory;

  SambaStoreInit(SambaStore store) {
    this.store = store;
    this.entryFactory = new LdapEntryFactory(store);
  }

  void init() {
    store.add(createComputersOu());
    store.add(createUsersOu());
    store.add(createDomainControllersOu());
    store.add(createGroupAllowedRodcPasswordReplication());
    store.add(createGroupCertPublisher());
    store.add(createGroupDeniedRodcPasswordReplication());
    store.add(createGroupDomainAdmins());
    store.add(createGroupDomainComputers());
    store.add(createGroupDomainControllers());
    store.add(createGroupDomainGuests());
    store.add(createGroupDomainUsers());
    store.add(createGroupEnterpriseAdmins());
    store.add(createGroupEnterpriseReadOnlyDomainControllers());
    store.add(createGroupGroupPolicyCreatorOwners());
    store.add(createGroupRasAndIasServers());
    store.add(createGroupReadOnlyDomainControllers());
    store.add(createGroupSchemaAdmins());
    store.add(createAdministrator());
    store.add(createKrbtgt());
    store.add(createDomainController());
    store.add(createDhcpdUser());
    store.add(createGroupDnsAdmins());
    store.add(createGroupDnsUpdateProxy());
    store.add(createComputerData());
  }

  private Dn getComputersDn() {
    return Dn.builder().add("CN=Computers").add(store.getDnTool().getBaseDn()).build();
  }

  private Dn getDomainControllersDn() {
    return Dn.builder().add("OU=Domain Controllers").add(store.getDnTool().getBaseDn()).build();
  }

  private Dn getUsersDn() {
    return Dn.builder().add("CN=Users").add(store.getDnTool().getBaseDn()).build();
  }

  private Dn getAdministratorDn() {
    return Dn.builder().add("CN=Administrator").add(getUsersDn()).build();
  }

  private LdapEntry createComputersOu() {
    String rdn = getComputersDn().getRDn().format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER);
    LdapEntry entry = entryFactory.newEntry(rdn, store.getDnTool().getBaseDn());
    AdConstants.OBJECT_CLASS.setValues(entry, List.of(
        "container", "top"
    ));
    AdConstants.CN.setValue(entry, "Computers");
    AdConstants.NAME.setValue(entry, "Computers");
    AdConstants.DESCRIPTION
        .setValue(entry, "Default container for upgraded computer accounts");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(entry, true);
    return entry;
  }

  private LdapEntry createUsersOu() {
    String rdn = getUsersDn().getRDn().format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER);
    LdapEntry entry = entryFactory.newEntry(rdn, store.getDnTool().getBaseDn());
    AdConstants.OBJECT_CLASS.setValues(entry, List.of(
        "container", "top"
    ));
    AdConstants.CN.setValue(entry, "Users");
    AdConstants.NAME.setValue(entry, "Users");
    AdConstants.DESCRIPTION.setValue(entry, "Default container for upgraded user accounts");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(entry, true);
    return entry;
  }

  private LdapEntry createDomainControllersOu() {
    String name = getDomainControllersDn().getRDn().getNameValue().getStringValue();
    LdapEntry entry = entryFactory
        .newOrganizationalUnitEntry(name, store.getDnTool().getBaseDn());
    AdConstants.DESCRIPTION.setValue(entry, "Default container for upgraded user accounts");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(entry, true);
    return entry;
  }

  private LdapEntry createGroupAllowedRodcPasswordReplication() {
    LdapEntry entry = entryFactory.newGroupEntry(
        "Allowed RODC Password Replication Group",
        getUsersDn(),
        -2147483644,
        store.getSid(571));
    AdConstants.DESCRIPTION.setValue(entry, "Members in this group can have their passwords "
        + "replicated to all read-only domain controllers in the domain");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(entry, true);
    return entry;
  }

  private LdapEntry createGroupCertPublisher() {
    LdapEntry entry = entryFactory.newGroupEntry(
        "Cert Publishers",
        getUsersDn(),
        -2147483644,
        store.getSid(517));
    AdConstants.DESCRIPTION.setValue(entry, "Members of this group are permitted to publish "
        + "certificates to the directory");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(entry, true);
    return entry;
  }

  private LdapEntry createGroupDeniedRodcPasswordReplication() {
    LdapEntry entry = entryFactory.newGroupEntry(
        "Denied RODC Password Replication Group",
        getUsersDn(),
        -2147483644,
        store.getSid(572));
    AdConstants.DESCRIPTION.setValue(entry, "Members in this group cannot have their "
        + "passwords replicated to any read-only domain controllers in the domain");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(entry, true);
    AdConstants.GROUP_MEMBER.setValues(entry, List.of(
        Dn.builder().add("CN=krbtgt").add(getUsersDn()).build()
    ));
    return entry;
  }

  private LdapEntry createGroupDomainAdmins() {
    LdapEntry node = entryFactory.newGroupEntry(
        "Domain Admins",
        getUsersDn(),
        -2147483646,
        store.getSid(512));
    AdConstants.DESCRIPTION.setValue(node, "Designated administrators of the domain");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(node, true);
    AdConstants.GROUP_MEMBER.setValues(node, List.of(
        getAdministratorDn()
    ));
    return node;
  }

  private LdapEntry createGroupDomainComputers() {
    LdapEntry node = entryFactory.newGroupEntry(
        "Domain Computers",
        getUsersDn(),
        -2147483646,
        store.getSid(515));
    AdConstants.DESCRIPTION.setValue(node, "All workstations and servers joined to the domain");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(node, true);
    return node;
  }

  private LdapEntry createGroupDomainControllers() {
    LdapEntry node = entryFactory.newGroupEntry(
        "Domain Controllers",
        getUsersDn(),
        -2147483646,
        store.getSid(516));
    AdConstants.DESCRIPTION.setValue(node, "All domain controllers in the domain");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(node, true);
    return node;
  }

  private LdapEntry createGroupDomainGuests() {
    LdapEntry node = entryFactory.newGroupEntry(
        "Domain Guests",
        getUsersDn(),
        -2147483646,
        store.getSid(514));
    AdConstants.DESCRIPTION.setValue(node, "All domain guests");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(node, true);
    return node;
  }

  private LdapEntry createGroupDomainUsers() {
    LdapEntry node = entryFactory.newGroupEntry(
        "Domain Users",
        getUsersDn(),
        -2147483646,
        store.getSid(513));
    AdConstants.DESCRIPTION.setValue(node, "All domain users");
    AdConstants.GID_NUMBER.setValue(node, 100);
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(node, true);
    return node;
  }

  private LdapEntry createGroupEnterpriseAdmins() {
    LdapEntry node = entryFactory.newGroupEntry(
        "Enterprise Admins",
        getUsersDn(),
        -2147483640,
        store.getSid(519));
    AdConstants.DESCRIPTION.setValue(node, "Designated administrators of the enterprise");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(node, true);
    AdConstants.GROUP_MEMBER.setValues(node, List.of(
        getAdministratorDn()
    ));
    return node;
  }

  private LdapEntry createGroupEnterpriseReadOnlyDomainControllers() {
    LdapEntry node = entryFactory.newGroupEntry(
        "Enterprise Read-only Domain Controllers",
        getUsersDn(),
        -2147483640,
        store.getSid(498));
    AdConstants.DESCRIPTION.setValue(node, "Members of this group are Read-Only Domain "
        + "Controllers in the enterprise");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(node, true);
    return node;
  }

  private LdapEntry createGroupGroupPolicyCreatorOwners() {
    LdapEntry node = entryFactory.newGroupEntry(
        "Group Policy Creator Owners",
        getUsersDn(),
        -2147483646,
        store.getSid(520));
    AdConstants.DESCRIPTION.setValue(node, "Members in this group can modify group policy "
        + "for the domain");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(node, true);
    AdConstants.GROUP_MEMBER.setValues(node, List.of(
        getAdministratorDn()
    ));
    return node;
  }

  private LdapEntry createGroupRasAndIasServers() {
    LdapEntry node = entryFactory.newGroupEntry(
        "RAS and IAS Servers",
        getUsersDn(),
        -2147483644,
        store.getSid(553));
    AdConstants.DESCRIPTION.setValue(node, "Servers in this group can access remote access "
        + "properties of users");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(node, true);
    return node;
  }

  private LdapEntry createGroupReadOnlyDomainControllers() {
    LdapEntry node = entryFactory.newGroupEntry(
        "Read-only Domain Controllers",
        getUsersDn(),
        -2147483646,
        store.getSid(521));
    AdConstants.DESCRIPTION.setValue(node,
        "Members of this group are Read-Only Domain Controllers in the domain");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(node, true);
    return node;
  }

  private LdapEntry createGroupSchemaAdmins() {
    LdapEntry node = entryFactory.newGroupEntry(
        "Schema Admins",
        getUsersDn(),
        -2147483640,
        store.getSid(518));
    AdConstants.DESCRIPTION.setValue(node, "Designated administrators of the schema");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(node, true);
    AdConstants.GROUP_MEMBER.setValues(node, List.of(
        getAdministratorDn()
    ));
    return node;
  }

  private LdapEntry createAdministrator() {
    LdapEntry node = entryFactory.newUserEntry(
        getAdministratorDn().getRDn().getNameValue().getStringValue(),
        getUsersDn(),
        513,
        store.getSid(500));
    AdConstants.DESCRIPTION.setValue(node, "Built-in account for administering the "
        + "computer/domain");
    AdConstants.USER_DISPLAY_NAME.setValue(node, "Domain Administrator");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(node, true);
    AdConstants.MEMBER_OF_GROUP.setValues(node, List.of(
        Dn.builder().add("Domain Admins").add(getUsersDn()).build(),
        Dn.builder().add("Enterprise Admins").add(getUsersDn()).build(),
        Dn.builder().add("Group Policy Creator Owners").add(getUsersDn()).build(),
        Dn.builder().add("Schema Admins").add(getUsersDn()).build()
    ));
    return node;
  }

  private LdapEntry createKrbtgt() {
    LdapEntry node = entryFactory.newUserEntry(
        "krbtgt",
        getUsersDn(),
        513,
        store.getNextSid());
    AdConstants.DESCRIPTION.setValue(node, "Key Distribution Center Service Account");
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(node, true);
    AdConstants.USER_USER_ACCOUNT_CONTROL.setValue(node, new UserAccountControl(514));
    AdConstants.MEMBER_OF_GROUP.setValues(node, List.of(
        Dn.builder().add("CN=Denied RODC Password Replication Group").add(getUsersDn()).build()
    ));
    return node;
  }

  private LdapEntry createDomainController() {
    LdapEntry node = entryFactory.newComputerEntry(
        "DC1",
        null,
        516,
        store.getNextSid());
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(node, true);
    AdConstants.COMPUTER_DNS_HOST_NAME.setValue(node, "DC1." + store.getDomainInfo().getDomain());
    AdConstants.USER_USER_ACCOUNT_CONTROL.setValue(node, new UserAccountControl(532480));
    String host = "HOST/dc1";
    String ldap = "ldap/dc1";
    AdConstants.COMPUTER_SERVICE_PRINCIPAL_NAME.setValues(node, List.of(
        "GC/DC1." + store.getDomainInfo().getDomain() + '/' + store.getDomainInfo().getDomain(),
        "HOST/DC1",
        host + store.getDomainInfo().getDomain(),
        host + store.getDomainInfo().getDomain() + '/' + store.getDomainInfo().getNetbiosDomain(),
        host + store.getDomainInfo().getDomain() + '/' + store.getDomainInfo().getDomain(),
        "ldap/DC1",
        ldap + store.getDomainInfo().getDomain(),
        ldap + store.getDomainInfo().getDomain() + "/DomainDnsZones." + store.getDomainInfo()
            .getDomain(),
        ldap + store.getDomainInfo().getDomain() + '/' + store.getDomainInfo().getNetbiosDomain(),
        ldap + store.getDomainInfo().getDomain() + '/' + store.getDomainInfo().getDomain(),
        ldap + store.getDomainInfo().getDomain() + "/ForestDnsZones."
            + store.getDomainInfo().getDomain(),
        "RestrictedKrbHost/DC1",
        "RestrictedKrbHost/dc1." + store.getDomainInfo().getDomain()
    ));
    return node;
  }

  private LdapEntry createDhcpdUser() {
    LdapEntry node = entryFactory.newUserEntry(
        "dhcpduser",
        getUsersDn(),
        513,
        store.getNextSid());
    AdConstants.MEMBER_OF_GROUP.setValues(node, List.of(
        Dn.builder().add("CN=DnsAdmins").add(getUsersDn()).build()
    ));
    return node;
  }

  private LdapEntry createGroupDnsAdmins() {
    LdapEntry node = entryFactory.newGroupEntry(
        "DnsAdmins",
        getUsersDn(),
        -2147483644,
        store.getNextSid());
    AdConstants.DESCRIPTION.setValue(node, "DNS Administrators Group");
    AdConstants.GROUP_MEMBER.setValues(node, List.of(
        Dn.builder().add("CN=dhcpduser").add(getUsersDn()).build()
    ));
    return node;
  }

  private LdapEntry createGroupDnsUpdateProxy() {
    LdapEntry node = entryFactory.newGroupEntry(
        "DnsUpdateProxy",
        getUsersDn(),
        -2147483646,
        store.getNextSid());
    AdConstants.DESCRIPTION.setValue(node, "DNS clients who are permitted to perform "
        + "dynamic updates on behalf of some other clients (such as DHCP servers).");
    return node;
  }

  private LdapEntry createComputerData() {
    LdapEntry node = entryFactory.newComputerEntry(
        "DATA",
        getComputersDn(),
        515,
        store.getNextSid());
    AdConstants.COMPUTER_DNS_HOST_NAME.setValue(node, "data." + store.getDomainInfo().getDomain());
    AdConstants.USER_USER_ACCOUNT_CONTROL.setValue(node, new UserAccountControl(4096));
    AdConstants.COMPUTER_SERVICE_PRINCIPAL_NAME.setValues(node, List.of(
        "HOST/DATA",
        "HOST/DATA." + store.getDomainInfo().getDomain(),
        "nfs/data",
        "nfs/data." + store.getDomainInfo().getDomain()
    ));
    return node;
  }


}
