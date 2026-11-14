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

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.bremersee.samba.ad.dc.model.DnsEntry;

/**
 * The dns repository.
 *
 * @author Christian Bremer
 */
public interface DnsEntryRepository {

  /**
   * The constant ZONE_ENTRIES_NODE_NAME.
   */
  String ZONE_ENTRIES_NODE_NAME = "@";

  /**
   * Gets dns entries.
   *
   * @param zoneName the zone name
   * @return the dns entries
   */
  @NotNull
  List<DnsEntry> getDnsEntries(@NotEmpty String zoneName);

  /**
   * Add common attributes dns entry.
   *
   * @param entry the entry
   * @return the dns entry
   */
  @NotNull
  DnsEntry addCommonAttributes(@NotNull DnsEntry entry);

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

}
