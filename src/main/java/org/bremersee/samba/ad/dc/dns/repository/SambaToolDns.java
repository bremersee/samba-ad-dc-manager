package org.bremersee.samba.ad.dc.dns.repository;

import java.util.List;
import java.util.Optional;
import org.bremersee.samba.ad.dc.dns.model.DnsEntry;
import org.bremersee.samba.ad.dc.dns.model.DnsZone;
import org.bremersee.samba.ad.dc.dns.model.DnsZoneType;

public interface SambaToolDns {

  String ZONE_ENTRIES_NODE_NAME = "@";

  List<String> getDnsZoneNames(String hostName, DnsZoneType zoneType);

  Optional<DnsZone> findDnsZone(String hostName, String zoneName);

  void createDnsZone(String hostName, String zoneName);

  void deleteDnsZone(String hostName, String zoneName);

  List<DnsEntry> getDnsEntries(String hostName, String zoneName);

  void addDnsEntry(String hostName, DnsEntry entry);

  void updateDnsEntry(String hostName, DnsEntry entry, String newValue);

  void deleteDnsEntry(String hostName, DnsEntry entry);

}
