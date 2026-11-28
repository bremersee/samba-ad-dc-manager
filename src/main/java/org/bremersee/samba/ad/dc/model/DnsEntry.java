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

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.regex.Pattern;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * The dns entry.
 *
 * @author Christian Bremer
 */
@Hidden
@Schema(name = ModelConstants.DNS_ENTRY, description = "DNS entry.")
@Value.Style(
    visibility = ImplementationVisibility.PUBLIC,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Value.Modifiable
@Serial.Version(1L)
@JsonTypeName(ModelConstants.DNS_ENTRY)
@JsonSerialize(as = ImmutableDnsEntry.class)
@JsonDeserialize(as = ImmutableDnsEntry.class)
public interface DnsEntry extends AdEntry {

  /**
   * The constant IPV4_PATTERN.
   */
  Pattern IPV4_PATTERN = Pattern.compile(
      "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

  /**
   * The constant CONFLICT_IDENTIFIER.
   */
  String CONFLICT_IDENTIFIER = "CNF";

  /**
   * The constant CONFLICT_NAME_PART.
   */
  String CONFLICT_NAME_PART = "\\0A" + CONFLICT_IDENTIFIER + ':';

  @Hidden
  @JsonIgnore
  @Value.Lazy
  @Override
  default AdEntryType getAdEntryType() {
    return AdEntryType.DNS_ENTRY;
  }

  @Override
  default DnsEntry withDistinguishedName(String distinguishedName) {
    return builder()
        .from(this)
        .distinguishedName(requireNonNullElse(distinguishedName, ""))
        .build();
  }

  @Schema(description = "The creation date.")
  @Nullable
  @Override
  OffsetDateTime getCreated();

  @Override
  default DnsEntry withCreated(OffsetDateTime created) {
    return builder()
        .from(this)
        .created(created)
        .build();
  }

  @Schema(description = "The last modification date.")
  @Nullable
  @Override
  OffsetDateTime getModified();

  @Override
  default DnsEntry withModified(OffsetDateTime modified) {
    return builder()
        .from(this)
        .modified(modified)
        .build();
  }

  /**
   * Gets zone name.
   *
   * @return the zone name
   */
  @Schema(description = "The zone name of this dns entry.")
  @Nullable
  String getZoneName();

  /**
   * With zone name dns entry.
   *
   * @param zoneName the zone name
   * @return the dns entry
   */
  default DnsEntry withZoneName(String zoneName) {
    return builder()
        .from(this)
        .zoneName(zoneName)
        .build();
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  @Schema(description = "The name of this dns entry.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "name", required = true)
  String getName();

  /**
   * Gets type.
   *
   * @return the type
   */
  @Schema(description = "The type of this dns entry.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "type", required = true)
  DnsEntryType getType();

  /**
   * Gets value.
   *
   * @return the value
   */
  @Schema(description = "The value of this dns entry.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "value", required = true)
  String getValue();

  /**
   * With value dns entry.
   *
   * @param value the value
   * @return the dns entry
   */
  default DnsEntry withValue(String value) {
    return builder()
        .from(this)
        .value(value)
        .build();
  }

  /**
   * Gets flags.
   *
   * @return the flags
   */
  @Schema(description = "The flags of this dns entry.")
  @Nullable
  String getFlags();

  /**
   * Gets serial.
   *
   * @return the serial
   */
  @Schema(description = "The serial of this dns entry.")
  @Nullable
  Integer getSerial();

  /**
   * Gets ttl seconds.
   *
   * @return the ttl seconds
   */
  @Schema(description = "The ttl seconds of this dns entry.")
  @Nullable
  Integer getTtlSeconds();

  /**
   * Gets display name.
   *
   * @return the display name
   */
  @Schema(description = "The display name of this dns entry.", accessMode = AccessMode.READ_ONLY)
  @JsonProperty(value = "displayName", access = Access.READ_ONLY)
  @Value.Lazy
  default String getDisplayName() {
    if (isNull(getName())) {
      return null;
    }
    int index = getName().indexOf(CONFLICT_NAME_PART);
    return index > 0 ? getName().substring(0, index) : getName();
  }

  /**
   * Is conflict boolean.
   *
   * @return the boolean
   */
  @Schema(description = "Determines whether this DNS entry is marked as conflict or not.",
      defaultValue = "false", accessMode = AccessMode.READ_ONLY)
  @JsonProperty(value = "conflict", defaultValue = "false", access = Access.READ_ONLY)
  @Value.Lazy
  default boolean isConflict() {
    if (isNull(getName())) {
      return false;
    }
    int index = getName().indexOf(CONFLICT_NAME_PART);
    if (index < 0) {
      return false;
    }
    String guid = getName().substring(index + CONFLICT_NAME_PART.length());
    try {
      UUID.fromString(guid);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Gets the immutable builder.
   *
   * @return the builder
   */
  static ImmutableDnsEntry.Builder builder() {
    return ImmutableDnsEntry.builder();
  }

}
