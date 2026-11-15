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
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.model.DnsZoneType;

/**
 * The interface DnsRepository.
 *
 * @author Christian Bremer
 */
public interface DnsZoneRepository {

  /**
   * Gets dns zone names.
   *
   * @param type the type
   * @return the dns zone names
   */
  @NotNull
  List<String> getDnsZoneNames(@NotNull DnsZoneType type);

  /**
   * Gets dns zone.
   *
   * @param zoneName the zone name
   * @return the dns zone
   */
  @NotNull
  DnsZone getDnsZone(@NotEmpty String zoneName);

  /**
   * Create dns zone.
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

}
