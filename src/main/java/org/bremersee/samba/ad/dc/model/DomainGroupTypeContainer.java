package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

@Schema(description = "The domain group type container.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableDomainGroupTypeContainer.class)
@JsonDeserialize(as = ImmutableDomainGroupTypeContainer.class)
public interface DomainGroupTypeContainer {

  @Schema(description = "The domain group type value. "
      + "Well known group types have the following values: "
      + "Global security: -2147483646, "
      + "global distribution: 2, "
      + "domain local security: -2147483644, "
      + "domain local distribution: 4, "
      + "universal security: -2147483640, "
      + "universal distribution: 8",
      defaultValue = "-2147483646", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "groupTypeValue", defaultValue = "-2147483646", required = true)
  @Value.Default
  default int getGroupTypeValue() {
    return DomainGroupType.GLOBAL_SECURITY.getValue();
  }

  @Schema(description = "The domain group type.",
      defaultValue = "global_security", accessMode = AccessMode.READ_ONLY)
  @JsonProperty(value = "groupType",
      defaultValue = "global_security", access = Access.READ_ONLY)
  @Value.Lazy
  default DomainGroupType getGroupType() { // TODO rename groupTypeName
    return DomainGroupType.fromValue(getGroupTypeValue());
  }

  static DomainGroupTypeContainer defaultContainer() {
    return containerWithGroupTypeValue(DomainGroupType.GLOBAL_SECURITY.getValue());
  }

  static DomainGroupTypeContainer containerWithGroupTypeValue(int groupTypeValue) {
    return builder().groupTypeValue(groupTypeValue).build();
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
  class Builder extends ImmutableDomainGroupTypeContainer.Builder {

  }

}
