package org.bremersee.samba.ad.dc.repository.mock;

import static org.springframework.util.ObjectUtils.isEmpty;

import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.DefaultDnTool;
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
public class MockedSambaToolComputer implements SambaToolComputer {

  private final DnTool dnTool;

  private final SambaStore store;

  public MockedSambaToolComputer(ApplicationProperties properties, SambaStore store) {
    this.dnTool = new DefaultDnTool(properties);
    this.store = store;
  }

  @Override
  public void moveComputer(DomainComputer domainComputer, Dn newOu) {
    if (!isEmpty(domainComputer) && DnTool.isValidDn(newOu)) {
      store.move(
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
