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
@JsonSerialize(as = ImmutableEmailChange.class)
@JsonDeserialize(as = ImmutableEmailChange.class)
public interface EmailChange extends Serializable {

  @JsonProperty(value = "username", required = true)
  String getUsername();

  @JsonProperty(value = "oldEmail", required = true)
  String getOldEmail();

  @JsonProperty(value = "newEmail", required = true)
  String getNewEmail();

  @JsonProperty(value = "requested", required = true)
  @Value.Default
  default OffsetDateTime getRequestDateTime() {
    return OffsetDateTime.now(ZoneOffset.UTC);
  }

  static EmailChange of(DomainUser domainUser, String newEmail) {
    return builder()
        .username(domainUser.getSamAccountName())
        .oldEmail(domainUser.getEmail())
        .newEmail(newEmail)
        .build();
  }

  static Builder builder() {
    return new Builder();
  }

  class Builder extends ImmutableEmailChange.Builder {

  }

}
