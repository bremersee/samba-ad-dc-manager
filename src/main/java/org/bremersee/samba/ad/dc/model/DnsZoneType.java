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

package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import lombok.Getter;

/**
 * The enum DnsZoneType.
 *
 * @author Christian Bremer
 */
@Schema(name = "DnsZoneType", description = "The type of a DNS zone.", enumAsRef = true)
public enum DnsZoneType {

  /**
   * List primary zones (default).
   */
  PRIMARY("primary", "Primary Zones", "dns-zone-type.primary.label"),

  /**
   * List secondary zones.
   */
  SECONDARY("secondary", "Secondary Zones", "dns-zone-type.secondary.label"),

  /**
   * List cached zones.
   */
  CACHE("cache", "Cached Zones", "dns-zone-type.cache.label"),

  /**
   * List automatically created zones.
   */
  AUTO("auto", "Auto-Created Zones", "dns-zone-type.auto.label"),

  /**
   * List forward zones.
   */
  FORWARD("forward", "Forward Zones", "dns-zone-type.forward.label"),

  /**
   * List reverse zones.
   */
  REVERSE("reverse", "Reverse Zones", "dns-zone-type.reverse.label"),

  /**
   * List directory integrated zones.
   */
  DS("ds", "Directory Integrated Zones", "dns-zone-type.ds.label"),

  /**
   * List non-directory zones.
   */
  NON_DS("non-ds", "Non-Directory Zones", "dns-zone-type.non-ds.label");

  private final String parameterValue;

  @Getter
  private final String defaultDisplayName;

  @Getter
  private final String i18nCode;

  DnsZoneType(String parameterValue, String defaultDisplayName, String i18nCode) {
    this.parameterValue = parameterValue;
    this.defaultDisplayName = defaultDisplayName;
    this.i18nCode = i18nCode;
  }

  @JsonValue
  public String getParameterValue() {
    return parameterValue;
  }

  @JsonCreator
  public static DnsZoneType fromValue(String value) {
    return Arrays.stream(values())
        .filter(type -> type.getParameterValue().equalsIgnoreCase(value)
            || type.name().equalsIgnoreCase(value))
        .findFirst()
        .orElse(PRIMARY);
  }
}
