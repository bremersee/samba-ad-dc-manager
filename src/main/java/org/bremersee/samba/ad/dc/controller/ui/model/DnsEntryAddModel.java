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

package org.bremersee.samba.ad.dc.controller.ui.model;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import lombok.Data;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsEntryType;
import org.bremersee.samba.ad.dc.service.DnsService;
import org.springframework.util.Assert;

/**
 * The dns entry add model.
 *
 * @author Christian Bremer
 */
@Data
public class DnsEntryAddModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String name;

  private DnsEntryType type = DnsEntryType.A;

  private String value;

  private boolean addReverseEntry;

  private String reverseZoneName;

  private String nameOfReverseEntry;

  private String valueOfReverseEntry;

  /**
   * Instantiates a new dns entry add model.
   */
  public DnsEntryAddModel() {
    super();
  }

  /**
   * Instantiates a new dns entry add model.
   *
   * @param zoneName the zone name
   */
  public DnsEntryAddModel(String zoneName) {
    Assert.hasText(zoneName, "Zone name is required.");
    if (zoneName.toLowerCase().endsWith(DnsService.REVERSE_ZONE_POSTFIX)) {
      type = DnsEntryType.PTR;
    }
  }

  /**
   * To dns entry.
   *
   * @param zoneName the zone name
   * @return the dns entry
   */
  public DnsEntry toDnsEntry(String zoneName) {
    return DnsEntry.builder()
        .zoneName(zoneName)
        .name(name)
        .type(type)
        .value(value)
        .build();
  }

  /**
   * To reverse dns entry.
   *
   * @return the optional reverse dns entry
   */
  public Optional<DnsEntry> toReverseDnsEntry() {
    if (!addReverseEntry || isEmpty(reverseZoneName)
        || isEmpty(nameOfReverseEntry) || isEmpty(valueOfReverseEntry)
        || !(DnsEntryType.A.equals(type) || DnsEntryType.AAAA.equals(type))) {
      return Optional.empty();
    }
    DnsEntry dnsEntry = DnsEntry.builder()
        .zoneName(reverseZoneName)
        .name(nameOfReverseEntry)
        .type(DnsEntryType.PTR)
        .value(valueOfReverseEntry)
        .build();
    return Optional.of(dnsEntry);
  }
}
