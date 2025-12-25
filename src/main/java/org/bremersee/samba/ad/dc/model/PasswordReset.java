package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.OffsetDateTime;
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
@JsonSerialize(as = ImmutableDomainInfo.class)
@JsonDeserialize(as = ImmutableDomainInfo.class)
public interface PasswordReset {

  String getUsername();

  @Value.Default
  default OffsetDateTime getRequestDateTime() {
    return OffsetDateTime.now();
  }

  OffsetDateTime getPwdLastSetDateTime();

  boolean isInvitation();

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
