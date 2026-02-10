package org.bremersee.samba.ad.dc.model;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.springframework.lang.Nullable;

@Schema(name = "DnsEntry", description = "DNS entry.", implementation = DnsEntry.class)
@Value.Style(
    visibility = ImplementationVisibility.PUBLIC,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*",
    passAnnotations = {
        Schema.class
    })
@Value.Immutable
@Value.Modifiable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableDnsEntry.class)
@JsonDeserialize(as = ImmutableDnsEntry.class)
public interface DnsEntry extends AdEntry {

  String CONFLICT_IDENTIFIER = "CNF";

  String CONFLICT_NAME_PART = "\\0A" + CONFLICT_IDENTIFIER + ':';

  @Override
  default DnsEntry withDistinguishedName(String distinguishedName) {
    return builder()
        .from(this)
        .distinguishedName(requireNonNullElse(distinguishedName, ""))
        .build();
  }

  @Override
  default DnsEntry withCreated(OffsetDateTime created) {
    return builder()
        .from(this)
        .created(created)
        .build();
  }

  @Override
  default DnsEntry withModified(OffsetDateTime modified) {
    return builder()
        .from(this)
        .modified(modified)
        .build();
  }

  @Schema(description = "The zone name of this dns entry.")
  @Nullable
  String getZoneName();

  default DnsEntry withZoneName(String zoneName) {
    return builder()
        .from(this)
        .zoneName(zoneName)
        .build();
  }

  @Schema(description = "The name of this dns entry.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "name", required = true)
  String getName();

  @Schema(description = "The type of this dns entry.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "type", required = true)
  DnsEntryType getType();

  @Schema(description = "The value of this dns entry.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "value", required = true)
  String getValue();

  default DnsEntry withValue(String value) {
    return builder()
        .from(this)
        .value(value)
        .build();
  }

  @Schema(description = "The flags of this dns entry.")
  @Nullable
  String getFlags();

  @Schema(description = "The serial of this dns entry.")
  @Nullable
  Integer getSerial();

  @Schema(description = "The ttl seconds of this dns entry.")
  @Nullable
  Integer getTtlSeconds();

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
