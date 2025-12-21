package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.Optional;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

/**
 * The windows/samba SID. Well known (system) SIDs are listed <a
 * href="https://support.microsoft.com/en-us/help/243330/well-known-security-identifiers-in-windows-operating-systems">here</a>.
 *
 * @author Christian Bremer
 */
@Schema(description = "The SID of the entity.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableSid.class)
@JsonDeserialize(as = ImmutableSid.class)
public interface Sid {

  String DEFAULT_SID_PREFIX = "S-1-5-21-";

  int MAX_SYSTEM_SID_SUFFIX = 999;

  /**
   * Gets the string value of this SID.
   *
   * @return the value
   */
  @Schema(description = "The SID of the entity.", requiredMode = RequiredMode.REQUIRED)
  String getValue();

  /**
   * Determines whether this SID belongs to a system entity or not.
   *
   * @return the {@code true}, if this SID belongs to a system entity, otherwise {@code false}
   */
  @Schema(description = "Determines whether this SID belongs to a system entity or not.",
      defaultValue = "false", accessMode = AccessMode.READ_ONLY)
  @JsonProperty(value = "systemEntity", defaultValue = "false", access =  Access.READ_ONLY)
  @Value.Lazy
  default boolean isSystemEntity() {
    if (!getValue().startsWith(DEFAULT_SID_PREFIX)) {
      return true;
    }
    return Optional.ofNullable(getSuffix())
        .map(suffix -> MAX_SYSTEM_SID_SUFFIX >= suffix)
        .orElse(false);
  }

  /**
   * Gets the suffix of this SID.
   *
   * @return the suffix
   */
  @Hidden
  @JsonIgnore
  @Value.Lazy
  default Integer getSuffix() {
    return Optional.ofNullable(getValue())
        .map(v -> {
          int index = v.lastIndexOf('-');
          if (index == -1) {
            return null;
          }
          try {
            return Integer.parseInt(v.substring(index + 1));
          } catch (RuntimeException e) {
            return null;
          }
        })
        .orElse(null);
  }

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
  class Builder extends ImmutableSid.Builder {

  }

}
