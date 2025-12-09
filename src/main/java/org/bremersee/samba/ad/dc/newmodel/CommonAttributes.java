package org.bremersee.samba.ad.dc.newmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.OffsetDateTime;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.springframework.lang.Nullable;

@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Value.Modifiable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableCommonAttributes.class)
@JsonDeserialize(as = ImmutableCommonAttributes.class)
public interface CommonAttributes {

  @JsonProperty("distinguishedName")
  @Nullable
  String getDistinguishedName();

  @JsonProperty("created")
  @Value.Default
  @Value.Auxiliary
  default OffsetDateTime getCreated() {
    return OffsetDateTime.now();
  }

  static Builder builder() {
    return new Builder();
  }

  class Builder extends ImmutableCommonAttributes.Builder {

  }

}
