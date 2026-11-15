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

package org.bremersee.samba.ad.dc.repository.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;

/**
 * The dns zone create validator.
 *
 * @author Christian Bremer
 */
public class DnsZoneCreateValidator extends DnsEntryValidator {

  private final String zoneName;

  /**
   * Instantiates a new dns zone create validator.
   *
   * @param zoneName the zone name
   */
  public DnsZoneCreateValidator(String zoneName) {
    this.zoneName = zoneName;
  }

  @Override
  String getExpectedResponse() {
    return String.format("Zone %s created successfully", zoneName);
  }

  @Override
  String getAction() {
    return "Creating";
  }

  @Override
  String getErrorCode() {
    return ErrorCode.EC_CREATING_DNS_ZONE_FAILED;
  }

}
