package org.bremersee.samba.ad.dc.repository.mock;

import java.util.List;
import java.util.Optional;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.model.DnsZoneType;
import org.bremersee.samba.ad.dc.repository.SambaToolDns;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Primary
@Component
@Profile("mock")
public class MockedSambaToolDns implements SambaToolDns {

  @Override
  public List<String> getDnsZoneNames(String hostName, DnsZoneType zoneType) {
    return List.of();
  }

  @Override
  public Optional<DnsZone> findDnsZone(String hostName, String zoneName) {
    return Optional.empty();
  }

  @Override
  public void createDnsZone(String hostName, String zoneName) {

  }

  @Override
  public void deleteDnsZone(String hostName, String zoneName) {

  }

  @Override
  public List<DnsEntry> getDnsEntries(String hostName, String zoneName) {
    return List.of();
  }

  @Override
  public void addDnsEntry(String hostName, DnsEntry entry) {

  }

  @Override
  public void updateDnsEntry(String hostName, DnsEntry entry, String newValue) {

  }

  @Override
  public void deleteDnsEntry(String hostName, DnsEntry entry) {

  }
}
