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

package org.bremersee.samba.ad.dc.controller.ui.shared;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bremersee.samba.ad.dc.model.DnsZoneType;

/**
 * The dns zone type dropdown.
 *
 * @author Christian Bremer
 */
@Getter
@ToString
@EqualsAndHashCode
@SuppressWarnings("ClassCanBeRecord")
public class DnsZoneTypeDropdown implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The selected zone type.
   */
  private final DnsZoneType selectedZoneType;

  /**
   * Instantiates a new dns zone type dropdown.
   *
   * @param selectedZoneType the selected zone type
   */
  public DnsZoneTypeDropdown(DnsZoneType selectedZoneType) {
    this.selectedZoneType = Objects.requireNonNullElse(selectedZoneType, DnsZoneType.PRIMARY);
  }

  /**
   * Gets selectable zone types.
   *
   * @return the selectable zone types
   */
  public List<DnsZoneType> getSelectableZoneTypes() { // used in header.html
    return Arrays.stream(DnsZoneType.values())
        .filter(zoneType -> zoneType != selectedZoneType)
        .toList();
  }
}
