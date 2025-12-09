package org.bremersee.samba.ad.dc.newmodel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

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
@JsonSerialize(as = ImmutableSamAccount.class)
@JsonDeserialize(as = ImmutableSamAccount.class)
public interface SamAccount extends CommonAttributes {

  String getSamAccountName();

  static Builder builder() {
    return new Builder();
  }

  class Builder extends ImmutableSamAccount.Builder {

  }

}
