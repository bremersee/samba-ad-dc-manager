/*
 * Copyright 2025-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.samba.ad.dc.repository;

import java.util.List;
import java.util.Optional;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.model.DnsZoneType;

/**
 * The samba tool dns.
 *
 * @author Christian Bremer
 */
public interface SambaToolDns {

  /**
   * The constant ZONE_ENTRIES_NODE_NAME.
   */
  String ZONE_ENTRIES_NODE_NAME = "@";

  /**
   * Gets dns zone names.
   *
   * @param hostName the host name
   * @param zoneType the zone type
   * @return the dns zone names
   */
  List<String> getDnsZoneNames(String hostName, DnsZoneType zoneType);

  /**
   * Find dns zone optional.
   *
   * @param hostName the host name
   * @param zoneName the zone name
   * @return the optional
   */
  Optional<DnsZone> findDnsZone(String hostName, String zoneName);

  /**
   * Create dns zone.
   *
   * @param hostName the host name
   * @param zoneName the zone name
   */
  void createDnsZone(String hostName, String zoneName);

  /**
   * Delete dns zone.
   *
   * @param hostName the host name
   * @param zoneName the zone name
   */
  void deleteDnsZone(String hostName, String zoneName);

  /**
   * Gets dns entries.
   *
   * @param hostName the host name
   * @param zoneName the zone name
   * @return the dns entries
   */
  List<DnsEntry> getDnsEntries(String hostName, String zoneName);

  /**
   * Add dns entry.
   *
   * @param hostName the host name
   * @param entry the entry
   */
  void addDnsEntry(String hostName, DnsEntry entry);

  /**
   * Update dns entry.
   *
   * @param hostName the host name
   * @param entry the entry
   * @param newValue the new value
   */
  void updateDnsEntry(String hostName, DnsEntry entry, String newValue);

  /**
   * Delete dns entry.
   *
   * @param hostName the host name
   * @param entry the entry
   */
  void deleteDnsEntry(String hostName, DnsEntry entry);

}
