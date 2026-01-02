package org.bremersee.samba.ad.dc.repository.mock;

import static org.springframework.util.ObjectUtils.isEmpty;

import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.repository.SambaToolComputer;
import org.ldaptive.dn.Dn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Primary
@Component
@Profile({"test", "mock"})
class MockedSambaToolComputer implements SambaToolComputer {

  private final SambaStore store;

  MockedSambaToolComputer(SambaStore store) {
    this.store = store;
  }

  @Override
  public void moveComputer(DomainComputer domainComputer, Dn newOu) {
    if (!isEmpty(domainComputer) && DnTool.isValidDn(newOu)) {
      store.moveEntry(
          domainComputer.getDistinguishedName(),
          newOu.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER));
    }
  }

  @Override
  public void deleteComputer(DomainComputer domainComputer) {
    if (!isEmpty(domainComputer)) {
      store.remove(domainComputer.getDistinguishedName());
    }
  }
}
