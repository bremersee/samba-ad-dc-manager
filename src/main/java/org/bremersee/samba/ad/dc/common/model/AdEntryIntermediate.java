package org.bremersee.samba.ad.dc.common.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

/**
 * The active directory base entry model.
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
