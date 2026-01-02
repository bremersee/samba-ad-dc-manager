package org.bremersee.samba.ad.dc.repository.mock;

import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.repository.SambaToolOu;
import org.ldaptive.LdapEntry;
import org.ldaptive.dn.Dn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Primary
@Component
@Profile("mock")
class MockedSambaToolOu implements SambaToolOu {

  private final SambaStore store;

  private final LdapEntryFactory ldapEntryFactory;

  public MockedSambaToolOu(SambaStore store) {
    this.store = store;
    this.ldapEntryFactory = new LdapEntryFactory(store);
  }

  @Override
  public void addOrganizationalUnit(OrganizationalUnit ou, Dn parentOu) {
    Dn parentDn = store.getDnTool().addBaseDn(parentOu);
    store.findByDn(parentDn.format()).ifPresent(parentNode -> {
      LdapEntry entry = ldapEntryFactory.newOrganizationalUnitEntry(
          ou.getName(),
          parentDn);
      store.add(entry);
    });
  }

  @Override
  public Dn moveOrganizationalUnit(Dn ouDn, Dn newParentOu) {
    return store.moveOrganizationalUnit(ouDn, newParentOu);
  }

  @Override
  public Dn renameOrganizationalUnit(Dn ouDn, String newName) {
    return store.renameOrganizationalUnit(ouDn, newName);
  }

  @Override
  public void deleteOrganizationalUnit(Dn ouDn) {
    store.remove(store.getDnTool().addBaseDn(ouDn).format());
  }
}
