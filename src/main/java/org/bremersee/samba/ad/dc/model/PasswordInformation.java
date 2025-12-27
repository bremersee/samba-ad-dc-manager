package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import java.util.Locale;
import java.util.regex.Pattern;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.springframework.context.MessageSource;

@Schema(description = "The password information of an active directory.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutablePasswordInformation.class)
@JsonDeserialize(as = ImmutablePasswordInformation.class)
public interface PasswordInformation {

  String SIMPLE_PASSWORD_REGEX = "^(?=.{%d,%d}$).*";

  String COMPLEX_PASSWORD_REGEX = "(?=^.{%d,%d}$)"
      + "((?=.*\\d)(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[^A-Za-z0-9])(?=.*[a-z])"
      + "|(?=.*[^A-Za-z0-9])(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[A-Z])(?=.*[^A-Za-z0-9]))^.*";

  @Schema(description = "The password complexity.", defaultValue = "on")
  @JsonProperty(value = "passwordComplexity", defaultValue = "on")
  @Value.Default
  default PasswordComplexity getPasswordComplexity() {
    return PasswordComplexity.ON;
  }

  @Schema(description = "Store plaintext passwords where account have 'store passwords with "
      + "reversible encryption' set (on | off | default). Default is 'off'.",
      defaultValue = "false")
  @JsonProperty(value = "storePlaintextPasswords", defaultValue = "false")
  @Value.Default
  default boolean isStorePlaintextPasswords() {
    return false;
  }

  @Schema(description = "The password history length. Default is 24.", defaultValue = "24")
  @JsonProperty(value = "passwordHistoryLength", defaultValue = "24")
  @Value.Default
  default int getPasswordHistoryLength() {
    return 24;
  }

  @Schema(description = "The minimum password length. Default is 7.", defaultValue = "7")
  @JsonProperty(value = "minimumPasswordLength", defaultValue = "7")
  @Value.Default
  default int getMinimumPasswordLength() {
    return 7;
  }

  @Schema(description = "The maximum password length. Default is 75.", defaultValue = "75")
  @JsonProperty(value = "maximumPasswordLength", defaultValue = "75")
  @Value.Default
  default int getMaximumPasswordLength() {
    return 75;
  }

  @Schema(description = "The minimum password age in days. Default is 1.", defaultValue = "1")
  @JsonProperty(value = "minimumPasswordAgeInDays", defaultValue = "1")
  @Value.Default
  default int getMinimumPasswordAgeInDays() {
    return 1;
  }

  @Schema(description = "The maximum password age in days. Default is 42.", defaultValue = "42")
  @JsonProperty(value = "maximumPasswordAgeInDays", defaultValue = "42")
  @Value.Default
  default int getMaximumPasswordAgeInDays() {
    return 42;
  }

  @Schema(description = "The the length of time an account is locked out after exceeding the "
      + "limit on bad password attempts. Default is 30.", defaultValue = "30")
  @JsonProperty(value = "accountLockoutDurationInMinutes", defaultValue = "30")
  @Value.Default
  default int getAccountLockoutDurationInMinutes() {
    return 30;
  }

  @Schema(description = "The number of bad password attempts allowed before locking out the "
      + "account. Default is 0 (never lock out).", defaultValue = "0")
  @JsonProperty(value = "accountLockoutThreshold", defaultValue = "0")
  @Value.Default
  default int getAccountLockoutThreshold() {
    return 0;
  }

  @Schema(description = "After this time is elapsed, the recorded number of attempts restarts "
      + "from zero. Default is 30.", defaultValue = "30")
  @JsonProperty(value = "resetAccountLockoutAfter", defaultValue = "30")
  @Value.Default
  default int getResetAccountLockoutAfter() {
    return 30;
  }

  @Hidden
  @JsonIgnore
  @Value.Default
  default String getSimplePasswordRegexTemplate() {
    return SIMPLE_PASSWORD_REGEX;
  }

  @Hidden
  @JsonIgnore
  @Value.Default
  default String getComplexPasswordRegexTemplate() {
    return COMPLEX_PASSWORD_REGEX;
  }

  /**
   * Gets password regex.
   *
   * @return the password regex
   */
  @Schema(description = "The password regex.", accessMode = AccessMode.READ_ONLY)
  @JsonProperty(value = "passwordRegex", access = Access.READ_ONLY)
  @Value.Lazy
  default String getPasswordRegex() {
    int minLength = getMinimumPasswordLength();
    int maxLength = getMaximumPasswordLength();
    String template;
    if (PasswordComplexity.OFF == getPasswordComplexity()) {
      template = getSimplePasswordRegexTemplate();
    } else {
      template = getComplexPasswordRegexTemplate();
    }
    if (containsPlaceholderForMinAndMaxLength(template)) {
      return String.format(template, minLength, maxLength);
    }
    return template;
  }

  /**
   * Gets password pattern.
   *
   * @return the password pattern
   */
  @Hidden
  @JsonIgnore
  @Value.Lazy
  default Pattern getPasswordPattern() {
    return Pattern.compile(getPasswordRegex());
  }

  default String getPasswordDescription(MessageSource messageSource, Locale locale) {
    return messageSource.getMessage(
        getPasswordComplexity().getI18nCode(),
        new Object[] { getMinimumPasswordLength(), getMaximumPasswordLength() },
        getPasswordComplexity().getDefaultDescription(),
        locale);
  }

  private static boolean containsPlaceholderForMinAndMaxLength(String passwordRegexTemplate) {
    return containsPlaceholderForMinAndMaxLength(passwordRegexTemplate, "%d")
        || containsPlaceholderForMinAndMaxLength(passwordRegexTemplate, "%s");
  }

  private static boolean containsPlaceholderForMinAndMaxLength(
      String passwordRegexTemplate, String placeholder) {
    int index = passwordRegexTemplate.indexOf(placeholder);
    if (index < 0) {
      return false;
    }
    int from = index + placeholder.length();
    index = passwordRegexTemplate.indexOf(placeholder, from);
    return index > 0;
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
  class Builder extends ImmutablePasswordInformation.Builder {

  }

}
