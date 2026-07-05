package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
@Serial.Version(1L)
@JsonSerialize(as = ImmutablePasswordReset.class)
@JsonDeserialize(as = ImmutablePasswordReset.class)
public interface PasswordReset extends Serializable {

  @JsonProperty(value = "username", required = true)
  String getUsername();

  @JsonProperty(value = "requested", required = true)
  @Value.Default
  default OffsetDateTime getRequestDateTime() {
    return OffsetDateTime.now(ZoneOffset.UTC);
  }

  @JsonProperty(value = "pwdLastSet", required = true)
  OffsetDateTime getPwdLastSetDateTime();

  @JsonProperty(value = "invitation", defaultValue = "false")
  @Value.Default
  default boolean isInvitation() {
    return false;
  }

  static PasswordReset of(DomainUser domainUser, boolean isInvitation) {
    return builder()
        .username(domainUser.getSamAccountName())
        .pwdLastSetDateTime(domainUser.getPasswordLastSet())
        .invitation(isInvitation)
        .build();
  }

  static Builder builder() {
    return new Builder();
  }

  class Builder extends ImmutablePasswordReset.Builder {

  }

}
