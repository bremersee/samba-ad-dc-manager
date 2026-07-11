package org.bremersee.samba.ad.dc.repository.mock;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    return List.of(
        DhcpLease.builder()
            .ip("192.168.1.101")
            .hostname("Mercury")
            .mac("xx:xx:xx:xx:xx:xx")
            .manufacturer("Apple, Inc.")
            .begin(now.minusMinutes(15L))
            .end(now.plusMinutes(15L))
            .build(),
        DhcpLease.builder()
            .ip("192.168.1.102")
            .hostname("Saturn")
            .mac("yy:yy:yy:yy:yy:yy")
            .manufacturer("HP Inc.")
            .begin(now.minusMinutes(11L))
            .end(now.plusMinutes(20L))
            .build()
    );
  }
}
