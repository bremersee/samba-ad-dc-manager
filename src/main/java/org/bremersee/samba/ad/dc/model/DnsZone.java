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

import static java.util.Objects.requireNonNullElse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.OffsetDateTime;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * The dns zone.
 *
 * @author Christian Bremer
 */
@Schema(name = ModelConstants.DNS_ZONE, description = "DNS zone.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonTypeName(ModelConstants.DNS_ZONE)
@JsonSerialize(as = ImmutableDnsZone.class)
@JsonDeserialize(as = ImmutableDnsZone.class)
public interface DnsZone extends AdEntry {

  @Hidden
  @JsonIgnore
  @Value.Lazy
  @Override
  default AdEntryType getAdEntryType() {
    return AdEntryType.DNS_ZONE;
  }

  @Override
  default DnsZone withDistinguishedName(String distinguishedName) {
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
  default DnsZone withCreated(OffsetDateTime created) {
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
  default DnsZone withModified(OffsetDateTime modified) {
    return builder()
        .from(this)
        .modified(modified)
        .build();
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  @Schema(description = "The zone name.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "name", required = true)
  String getName();

  /**
   * Gets zone type.
   *
   * @return the zone type
   */
  @Schema(description = "The zone type.")
  @Nullable
  String getZoneType(); // DNS_ZONE_TYPE_PRIMARY, DNS_ZONE_TYPE_CACHE

  /**
   * Gets reverse zone.
   *
   * @return the reverse zone
   */
  @Schema(description = "Specifies whether this zone is a reverse zone or not.")
  @Nullable
  Boolean getReverseZone();

  /**
   * Gets allow update.
   *
   * @return the allow update
   */
  @Nullable
  String getAllowUpdate();

  /**
   * Gets paused.
   *
   * @return the paused
   */
  @Nullable
  Boolean getPaused();

  /**
   * Gets shutdown.
   *
   * @return the shutdown
   */
  @Nullable
  Boolean getShutdown();

  /**
   * Gets auto created.
   *
   * @return the auto created
   */
  @Nullable
  Boolean getAutoCreated();

  /**
   * Gets use database.
   *
   * @return the use database
   */
  @Nullable
  Boolean getUseDatabase();

  /**
   * Gets data file.
   *
   * @return the data file
   */
  @Nullable
  String getDataFile();

  /**
   * Gets use wins.
   *
   * @return the use wins
   */
  @Nullable
  Boolean getUseWins();

  /**
   * Gets use nbstat.
   *
   * @return the use nbstat
   */
  @Nullable
  Boolean getUseNbstat();

  /**
   * Gets aging.
   *
   * @return the aging
   */
  @Nullable
  Boolean getAging();

  /**
   * Gets fqdn.
   *
   * @return the fqdn
   */
  @Nullable
  String getFqdn();

  /**
   * Gets queued for background load.
   *
   * @return the queued for background load
   */
  @Nullable
  Boolean getQueuedForBackgroundLoad();

  /**
   * Gets background load in progress.
   *
   * @return the background load in progress
   */
  @Nullable
  Boolean getBackgroundLoadInProgress();

  /**
   * Gets read only zone.
   *
   * @return the read only zone
   */
  @Nullable
  Boolean getReadOnlyZone();

  /**
   * Gets the immutable builder.
   *
   * @return the builder
   */
  static Builder builder() {
    return new Builder();
  }

  /**
   * The immutable builder.
   */
  class Builder extends ImmutableDnsZone.Builder {

  }

}
