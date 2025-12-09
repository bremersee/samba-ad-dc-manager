package org.bremersee.samba.ad.dc.repository.tools;

import java.util.List;
import java.util.Optional;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.model.DnsZoneType;

public interface DnsTool {

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
