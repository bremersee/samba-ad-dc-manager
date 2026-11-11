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

import java.util.Comparator;
import org.bremersee.samba.ad.dc.model.AdEntry;

/**
 * The active directory entry type comparator.
 *
 * @author Christian Bremer
 */
public class AdEntryTypeComparator implements Comparator<AdEntry> {

  @Override
  public int compare(AdEntry o1, AdEntry o2) {
    return Integer.compare(o1.getAdEntryType().getSortOrder(),
        o2.getAdEntryType().getSortOrder());
  }
}
