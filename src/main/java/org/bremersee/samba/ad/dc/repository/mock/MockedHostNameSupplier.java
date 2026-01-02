package org.bremersee.samba.ad.dc.repository.mock;

import org.bremersee.samba.ad.dc.repository.HostNameSupplier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Primary
@Component
@Profile("mock")
public class MockedHostNameSupplier implements HostNameSupplier {

  @Override
  public String getHostName() {
    return "dc1";
  }
}
