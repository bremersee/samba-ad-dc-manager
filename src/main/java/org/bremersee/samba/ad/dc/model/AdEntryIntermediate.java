package org.bremersee.samba.ad.dc.model;

import static java.util.Objects.requireNonNullElse;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

/**
 * The active directory base entry model with immutable builder.
 *
 * @author Christian Bremer
 */
@Schema(description = "Active directory base entry.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableAdEntryIntermediate.class)
@JsonDeserialize(as = ImmutableAdEntryIntermediate.class)
public interface AdEntryIntermediate extends AdEntry {

  @Override
  default AdEntry withDistinguishedName(String distinguishedName) {
    return builder()
        .from(this)
        .distinguishedName(requireNonNullElse(distinguishedName, ""))
        .build();
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
  class Builder extends ImmutableAdEntryIntermediate.Builder {

  }

}
