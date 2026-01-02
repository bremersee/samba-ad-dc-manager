package org.bremersee.samba.ad.dc.repository.mock;

import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.bremersee.samba.ad.dc.repository.SambaToolGroup;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.dn.Dn;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.EqualityFilter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Primary
@Component
@Profile({"test", "mock"})
class MockedSambaToolGroup implements SambaToolGroup {

  private final SambaStore store;

  private final LdapEntryFactory ldapEntryFactory;

  MockedSambaToolGroup(SambaStore store) {
    this.store = store;
    this.ldapEntryFactory = new LdapEntryFactory(store);
  }

  @Override
  public void addGroup(DomainGroup domainGroup, Dn ou, Boolean isRfc2307Enabled) {
    Dn parentDn = store.getDnTool().addBaseDn(ou);
    store.findByDn(parentDn.format()).ifPresent(parentNode -> {
      LdapEntry entry = ldapEntryFactory.newGroupEntry(
          domainGroup.getSamAccountName(),
          parentDn,
          domainGroup.getGroupType().getValue(),
          store.getNextSid());
      store.add(entry);
    });
  }

  @Override
  public void renameAndMoveGroup(DomainGroup oldDomainGroup, DomainGroup newDomainGroup, Dn newDn) {
    Dn groupDn;
    if (!oldDomainGroup.getSamAccountName().equals(newDomainGroup.getSamAccountName())) {
      groupDn = store.renameEntry(
          new Dn(oldDomainGroup.getDistinguishedName()),
          newDomainGroup.getSamAccountName());
    } else {
      groupDn = new Dn(oldDomainGroup.getDistinguishedName());
    }
    store.findByDn(groupDn.format()).ifPresent(node -> {
      AdConstants.SAM_ACCOUNT_NAME.setValue(node, newDomainGroup.getSamAccountName());
      AdConstants.NAME.setValue(node, newDomainGroup.getSamAccountName());
      AdConstants.NIS_NAME.setValue(node, newDomainGroup.getSamAccountName());
      store.moveEntry(node.getDn(), newDn.getParent().format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER));
    });
  }

  @Override
  public void deleteGroup(String samAccountName) {
    SearchRequest request = SearchRequest.builder()
        .dn(store.getDnTool().getBaseDn().format())
        .filter(new AndFilter(
            new EqualityFilter(AdConstants.OBJECT_CLASS.getName(), AdConstants.OBJECT_CLASS_GROUP),
            new EqualityFilter(AdConstants.SAM_ACCOUNT_NAME.getName(), samAccountName)))
        .sizeLimit(1)
        .build();
    store.find(request).stream()
        .findFirst()
        .ifPresent(group -> store.remove(group.getDn()));

  }
}
