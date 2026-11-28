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
import static org.bremersee.samba.ad.dc.model.DnsEntry.IPV4_PATTERN;

import java.util.Comparator;
import org.bremersee.samba.ad.dc.model.DnsEntry;

/**
 * The dns entry value comparator.
 *
 * @author Christian Bremer
 */
public class DnsEntryValueComparator implements Comparator<DnsEntry> {

  @Override
  public int compare(DnsEntry o1, DnsEntry o2) {
    String n1 = requireNonNullElse(o1.getValue(), "");
    String n2 = requireNonNullElse(o2.getValue(), "");
    if (IPV4_PATTERN.matcher(n1).matches() && IPV4_PATTERN.matcher(n2).matches()) {
      return compareIp4(n1, n2);
    }
    return n1.compareToIgnoreCase(n2);
  }

  private int compareIp4(String n1, String n2) {
    String[] a1 = n1.split("\\.");
    String[] a2 = n2.split("\\.");
    if (a1.length == a2.length) {
      for (int i = a1.length - 1; i >= 0; i--) {
        try {
          int i1 = Integer.parseInt(a1[i]);
          int i2 = Integer.parseInt(a2[i]);
          int r = Integer.compare(i1, i2);
          if (r != 0) {
            return r;
          }
        } catch (NumberFormatException e) {
          int r = a1[i].compareToIgnoreCase(a2[i]);
          if (r != 0) {
            return r;
          }
        }
      }
    }
    return n1.compareToIgnoreCase(n2);
  }
}
