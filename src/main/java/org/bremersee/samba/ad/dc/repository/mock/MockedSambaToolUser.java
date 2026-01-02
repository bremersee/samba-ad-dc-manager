package org.bremersee.samba.ad.dc.repository.mock;

import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.bremersee.samba.ad.dc.repository.SambaToolUser;
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
@Profile("mock")
class MockedSambaToolUser implements SambaToolUser {

  private final SambaStore store;

  private final LdapEntryFactory ldapEntryFactory;

  MockedSambaToolUser(SambaStore store) {
    this.store = store;
    this.ldapEntryFactory = new LdapEntryFactory(store);
  }

  @Override
  public void addUser(DomainUser domainUser, Dn ou, Boolean useUsernameAsCn,
      Boolean isRfc2307Enabled) {
    Dn parentDn = store.getDnTool().addBaseDn(ou);
    store.findByDn(parentDn).ifPresent(parentNode -> {
      LdapEntry entry = ldapEntryFactory.newUserEntry(
          domainUser.getSamAccountName(),
          parentDn,
          domainUser.getPrimaryGroupId(),
          store.getNextSid());
      store.add(entry);
    });
  }

  @Override
  public void renameAndMoveUser(DomainUser oldDomainUser, DomainUser newDomainUser, Dn newDn) {
    Dn userDn;
    if (!oldDomainUser.getSamAccountName().equals(newDomainUser.getSamAccountName())) {
      userDn = store.renameEntry(
          oldDomainUser.getDn(),
          oldDomainUser.getSamAccountName());
    } else {
      userDn = oldDomainUser.getDn();
    }
    store.findByDn(userDn).ifPresent(node -> {
      AdConstants.SAM_ACCOUNT_NAME.setValue(node, newDomainUser.getSamAccountName());
      AdConstants.NAME.setValue(node, newDomainUser.getSamAccountName());
      if (DnTool.isValidDn(newDn)) {
        Dn newParentDn = store.getDnTool().addBaseDn(newDn.getParent());
        if (!userDn.getParent().equals(newParentDn)) {
          store.moveEntry(userDn, newParentDn);
        }
      }
    });
  }

  @Override
  public void deleteUser(String samAccountName) {
    SearchRequest request = SearchRequest.builder()
        .dn(store.getDnTool().getBaseDn().format())
        .filter(new AndFilter(
            new EqualityFilter(AdConstants.OBJECT_CLASS.getName(), AdConstants.OBJECT_CLASS_USER),
            new EqualityFilter(AdConstants.SAM_ACCOUNT_NAME.getName(), samAccountName)))
        .sizeLimit(1)
        .build();
    store.find(request).stream()
        .findFirst()
        .ifPresent(group -> store.remove(group.getDn()));
  }
}
