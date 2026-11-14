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

  @Schema(description = "The zone name.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "name", required = true)
  String getName();

  @Schema(description = "The zone type.")
  @Nullable
  String getZoneType(); // DNS_ZONE_TYPE_PRIMARY, DNS_ZONE_TYPE_CACHE

  @Schema(description = "Specifies whether this zone is a reverse zone or not.")
  @Nullable
  Boolean getReverseZone();

  @Nullable
  String getAllowUpdate();

  @Nullable
  Boolean getPaused();

  @Nullable
  Boolean getShutdown();

  @Nullable
  Boolean getAutoCreated();

  @Nullable
  Boolean getUseDatabase();

  @Nullable
  String getDataFile();

  @Nullable
  Boolean getUseWins();

  @Nullable
  Boolean getUseNbstat();

  @Nullable
  Boolean getAging();

  @Nullable
  String getFqdn();

  @Nullable
  Boolean getQueuedForBackgroundLoad();

  @Nullable
  Boolean getBackgroundLoadInProgress();

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
