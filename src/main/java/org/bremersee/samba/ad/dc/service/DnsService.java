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

package org.bremersee.samba.ad.dc.service;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.bremersee.samba.ad.dc.model.DhcpLease;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.model.DnsZoneType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * The dns service.
 *
 * @author Christian Bremer
 */
public interface DnsService {

  /**
   * The constant REVERSE_ZONE_POSTFIX.
   */
  String REVERSE_ZONE_POSTFIX = ".in-addr.arpa";

  /**
   * Gets dns zone names.
   *
   * @param type the type
   * @return the dns zone names
   */
  @NotNull
  List<String> getDnsZoneNames(@Nullable DnsZoneType type);

  /**
   * Gets dns zone.
   *
   * @param zoneName the zone name
   * @return the dns zone
   */
  @NotNull
  DnsZone getDnsZone(@NotEmpty String zoneName);

  /**
   * Create dns zone dns zone.
   *
   * @param zoneName the zone name
   * @return the dns zone
   */
  @NotNull
  DnsZone createDnsZone(@NotEmpty String zoneName);

  /**
   * Delete dns zone.
   *
   * @param zoneName the zone name
   */
  void deleteDnsZone(@NotEmpty String zoneName);

  /**
   * Gets dns entries.
   *
   * @param zoneName the zone name
   * @param pageable the pageable
   * @param query the query
   * @return the dns entries
   */
  Page<DnsEntry> getDnsEntries(
      @NotEmpty String zoneName,
      @NotNull Pageable pageable,
      String query);

  /**
   * Find dns entry optional.
   *
   * @param dnsEntry the dns entry
   * @return the optional
   */
  Optional<DnsEntry> findDnsEntry(@NotNull DnsEntry dnsEntry);

  /**
   * Find dns entry optional.
   *
   * @param ipAddress the ip address
   * @return the optional
   */
  Optional<DnsEntry> findDnsEntry(@NotEmpty String ipAddress);

  /**
   * Find reverse dns entry optional.
   *
   * @param dnsEntry the dns entry
   * @return the optional
   */
  Optional<DnsEntry> findReverseDnsEntry(@NotNull DnsEntry dnsEntry);

  /**
   * Find dns entries conflicting with stream.
   *
   * @param dnsEntry the dns entry
   * @return the stream
   */
  Stream<DnsEntry> findDnsEntriesConflictingWith(@NotNull DnsEntry dnsEntry);

  /**
   * Add dns entry.
   *
   * @param entry the entry
   */
  void addDnsEntry(@NotNull DnsEntry entry);

  /**
   * Update dns entry.
   *
   * @param entry the entry
   * @param newValue the new value
   */
  void updateDnsEntry(@NotNull DnsEntry entry, @NotEmpty String newValue);

  /**
   * Delete dns entry.
   *
   * @param entry the entry
   */
  void deleteDnsEntry(@NotNull DnsEntry entry);

  /**
   * Gets dhcp leases.
   *
   * @param pageable the pageable
   * @param query the query
   * @return the dhcp leases
   */
  @NotNull
  Page<DhcpLease> getDhcpLeases(@NotNull Pageable pageable, String query);

}
