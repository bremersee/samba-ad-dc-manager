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

package org.bremersee.samba.ad.dc.model.comparator;

import static java.util.Objects.requireNonNullElse;

import java.util.Comparator;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsEntryType;
import org.bremersee.samba.ad.dc.repository.DnsEntryRepository;

/**
 * The dns entry name comparator.
 *
 * @author Christian Bremer
 */
public class DnsEntryNameComparator implements Comparator<DnsEntry> {

  @Override
  public int compare(DnsEntry o1, DnsEntry o2) {
    String n1 = requireNonNullElse(o1.getName(), "");
    String n2 = requireNonNullElse(o2.getName(), "");
    if (DnsEntryRepository.ZONE_ENTRIES_NODE_NAME.equals(n1)
        && !DnsEntryRepository.ZONE_ENTRIES_NODE_NAME.equals(n2)) {
      return -1;
    }
    if (!DnsEntryRepository.ZONE_ENTRIES_NODE_NAME.equals(n1)
        && DnsEntryRepository.ZONE_ENTRIES_NODE_NAME.equals(n2)) {
      return 1;
    }
    if (DnsEntryType.PTR.equals(o1.getType()) && DnsEntryType.PTR.equals(o2.getType())) {
      return String.format("%255s", n1).compareToIgnoreCase(String.format("%255s", n2));
    }
    return n1.compareToIgnoreCase(n2);
  }
}
