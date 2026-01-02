package org.bremersee.samba.ad.dc.repository.mock;

import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.repository.SambaToolDomain;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Primary
@Component
@Profile({"test", "mock"})
class MockedSambaToolDomain implements SambaToolDomain {

  private final SambaStore store;

  MockedSambaToolDomain(SambaStore store) {
    this.store = store;
  }

  @Override
  public DomainInfo getDomainInfo(String ipOrHostname) {
    return store.getDomainInfo();
  }

  @Override
  public PasswordInformation getPasswordInformation() {
    return store.getPasswordInformation();
  }
}
