package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Optional;
import org.bremersee.ldaptive.transcoder.UserAccountControl;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

@Schema(description = "Domain user's account control.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableDomainUserAccountControl.class)
@JsonDeserialize(as = ImmutableDomainUserAccountControl.class)
public interface DomainUserAccountControl {

  @Schema(
      description = "Specifies whether the account of the domain user is a normal one or not.",
      defaultValue = "true")
  @JsonProperty(value = "normalAccount", defaultValue = "true")
  @Value.Default
  default boolean isNormalAccount() {
    return true;
  }

  @Schema(
      description = "Specifies whether the domain user is enabled or not.",
      defaultValue = "true")
  @JsonProperty(value = "enabled", defaultValue = "true")
  @Value.Default
  default boolean isEnabled() {
    return true;
  }

  @Schema(
      description = "Specifies whether the password expiration is enabled or not.",
      defaultValue = "false")
  @JsonProperty(value = "passwordExpirationEnabled", defaultValue = "false")
  @Value.Default
  default boolean isPasswordExpirationEnabled() {
    return false;
  }

  @Hidden
  @JsonIgnore
  @Value.Lazy
  default UserAccountControl getUserAccountControl() {
    UserAccountControl userAccountControl = new UserAccountControl();
    userAccountControl.setNormalAccount(isNormalAccount());
    userAccountControl.setEnabled(isEnabled());
    userAccountControl.setPasswordExpirationEnabled(isPasswordExpirationEnabled());
    return userAccountControl;
  }

  static DomainUserAccountControl defaultAccountControl() {
    return builder().build();
  }

  static DomainUserAccountControl from(UserAccountControl userAccountControl) {
    return Optional.ofNullable(userAccountControl)
        .map(accountControl -> builder()
            .normalAccount(accountControl.isNormalAccount())
            .enabled(accountControl.isEnabled())
            .passwordExpirationEnabled(accountControl.isPasswordExpirationEnabled())
            .build())
        .orElseGet(DomainUserAccountControl::defaultAccountControl);
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
  class Builder extends ImmutableDomainUserAccountControl.Builder {

  }

}
