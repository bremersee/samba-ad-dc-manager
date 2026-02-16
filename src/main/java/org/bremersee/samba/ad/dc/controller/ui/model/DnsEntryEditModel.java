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

import static java.util.Objects.nonNull;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsEntryType;

/**
 * The dns entry edit model.
 *
 * @author Christian Bremer
 */
@Data
public class DnsEntryEditModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String newName;

  private DnsEntryType newType;

  private String newValue;

  private boolean updateReverseEntry;

  private String newNameOfReverseEntry;

  private String newValueOfReverseEntry;

  /**
   * Instantiates a new dns entry edit model.
   */
  public DnsEntryEditModel() {
    super();
  }

  /**
   * Instantiates a new dns entry edit model.
   *
   * @param dnsEntry the dns entry
   */
  public DnsEntryEditModel(DnsEntry dnsEntry) {
    this(dnsEntry, null);
  }

  /**
   * Instantiates a new dns entry edit model.
   *
   * @param dnsEntry the dns entry
   * @param reverseDnsEntry the reverse dns entry
   */
  public DnsEntryEditModel(DnsEntry dnsEntry, DnsEntry reverseDnsEntry) {
    this.newName = dnsEntry.getName();
    this.newType = dnsEntry.getType();
    this.newValue = dnsEntry.getValue();
    if (nonNull(reverseDnsEntry)) {
      this.updateReverseEntry = true;
      this.newNameOfReverseEntry = reverseDnsEntry.getName();
      this.newValueOfReverseEntry = reverseDnsEntry.getValue();
    }
  }

  /**
   * To new dns entry dns entry.
   *
   * @param zoneName the zone name
   * @return the dns entry
   */
  public DnsEntry toNewDnsEntry(String zoneName) {
    return DnsEntry.builder()
        .zoneName(zoneName)
        .name(newName)
        .type(newType)
        .value(newValue)
        .build();
  }

}
