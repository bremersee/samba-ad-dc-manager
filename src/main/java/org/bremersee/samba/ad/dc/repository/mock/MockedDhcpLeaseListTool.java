package org.bremersee.samba.ad.dc.repository.mock;

import java.util.List;
import org.bremersee.samba.ad.dc.model.DhcpLease;
import org.bremersee.samba.ad.dc.repository.DhcpLeaseListTool;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Primary
@Component
@Profile("mock")
public class MockedDhcpLeaseListTool implements DhcpLeaseListTool {

  @Override
  public List<DhcpLease> findActive() {
    return List.of();
  }
}
