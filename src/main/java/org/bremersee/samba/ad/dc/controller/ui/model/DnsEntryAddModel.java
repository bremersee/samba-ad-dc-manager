/*
 * Copyright 2025 the original author or authors.
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

import static java.util.Objects.nonNull;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsEntryType;
import org.bremersee.samba.ad.dc.service.DnsService;

/**
 * The dns entry add model.
 *
 * @author Christian Bremer
 */
@Data
@NoArgsConstructor
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

  public DnsEntryAddModel(String zoneName) {
    if (nonNull(zoneName) && zoneName.toLowerCase().endsWith(DnsService.REVERSE_ZONE_POSTFIX)) {
      type = DnsEntryType.PTR;
    }
  }

  public DnsEntry toDnsEntry(String zoneName) {
    return DnsEntry.builder()
        .zoneName(zoneName)
        .name(name)
        .type(type)
        .value(value)
        .build();
  }

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
