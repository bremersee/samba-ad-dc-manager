package org.bremersee.samba.ad.dc.model;

import static java.util.Objects.requireNonNullElse;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

/**
 * The base of a 'SamAccount' like 'User', 'Group' and 'Computer' with immutable builder.
 *
 * @author Christian Bremer
 */
@Schema(description = "The base of a 'SamAccount' like 'User', 'Group' and 'Computer'.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableSamAccountIntermediate.class)
@JsonDeserialize(as = ImmutableSamAccountIntermediate.class)
public interface SamAccountIntermediate extends SamAccount {

  @Override
  default SamAccountIntermediate withDistinguishedName(String distinguishedName) {
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
  class Builder extends ImmutableSamAccountIntermediate.Builder {

  }

}
