/*
 * Copyright 2025-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import java.io.Serializable;
import java.util.Locale;
import java.util.regex.Pattern;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.springframework.context.MessageSource;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * The password information.
 *
 * @author Christian Bremer
 */
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
public interface PasswordInformation extends Serializable {

  /**
   * The constant SIMPLE_PASSWORD_REGEX.
   */
  String SIMPLE_PASSWORD_REGEX = "^(?=.{%d,%d}$).*";

  /**
   * The constant COMPLEX_PASSWORD_REGEX.
   */
  String COMPLEX_PASSWORD_REGEX = "(?=^.{%d,%d}$)"
      + "((?=.*\\d)(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[^A-Za-z0-9])(?=.*[a-z])"
      + "|(?=.*[^A-Za-z0-9])(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[A-Z])(?=.*[^A-Za-z0-9]))^.*";

  /**
   * Gets password complexity.
   *
   * @return the password complexity
   */
  @Schema(description = "The password complexity.", defaultValue = "on")
  @JsonProperty(value = "passwordComplexity", defaultValue = "on")
  @Value.Default
  default PasswordComplexity getPasswordComplexity() {
    return PasswordComplexity.ON;
  }

  /**
   * Is store plaintext passwords boolean.
   *
   * @return the boolean
   */
  @Schema(description = "Store plaintext passwords where account have 'store passwords with "
      + "reversible encryption' set (on | off | default). Default is 'off'.",
      defaultValue = "false")
  @JsonProperty(value = "storePlaintextPasswords", defaultValue = "false")
  @Value.Default
  default boolean isStorePlaintextPasswords() {
    return false;
  }

  /**
   * Gets password history length.
   *
   * @return the password history length
   */
  @Schema(description = "The password history length. Default is 24.", defaultValue = "24")
  @JsonProperty(value = "passwordHistoryLength", defaultValue = "24")
  @Value.Default
  default int getPasswordHistoryLength() {
    return 24;
  }

  /**
   * Gets minimum password length.
   *
   * @return the minimum password length
   */
  @Schema(description = "The minimum password length. Default is 7.", defaultValue = "7")
  @JsonProperty(value = "minimumPasswordLength", defaultValue = "7")
  @Value.Default
  default int getMinimumPasswordLength() {
    return 7;
  }

  /**
   * Gets maximum password length.
   *
   * @return the maximum password length
   */
  @Schema(description = "The maximum password length. Default is 75.", defaultValue = "75")
  @JsonProperty(value = "maximumPasswordLength", defaultValue = "75")
  @Value.Default
  default int getMaximumPasswordLength() {
    return 75;
  }

  /**
   * Gets minimum password age in days.
   *
   * @return the minimum password age in days
   */
  @Schema(description = "The minimum password age in days. Default is 1.", defaultValue = "1")
  @JsonProperty(value = "minimumPasswordAgeInDays", defaultValue = "1")
  @Value.Default
  default int getMinimumPasswordAgeInDays() {
    return 1;
  }

  /**
   * Gets maximum password age in days.
   *
   * @return the maximum password age in days
   */
  @Schema(description = "The maximum password age in days. Default is 42.", defaultValue = "42")
  @JsonProperty(value = "maximumPasswordAgeInDays", defaultValue = "42")
  @Value.Default
  default int getMaximumPasswordAgeInDays() {
    return 42;
  }

  /**
   * Gets account lockout duration in minutes.
   *
   * @return the account lockout duration in minutes
   */
  @Schema(description = "The the length of time an account is locked out after exceeding the "
      + "limit on bad password attempts. Default is 30.", defaultValue = "30")
  @JsonProperty(value = "accountLockoutDurationInMinutes", defaultValue = "30")
  @Value.Default
  default int getAccountLockoutDurationInMinutes() {
    return 30;
  }

  /**
   * Gets account lockout threshold.
   *
   * @return the account lockout threshold
   */
  @Schema(description = "The number of bad password attempts allowed before locking out the "
      + "account. Default is 0 (never lock out).", defaultValue = "0")
  @JsonProperty(value = "accountLockoutThreshold", defaultValue = "0")
  @Value.Default
  default int getAccountLockoutThreshold() {
    return 0;
  }

  /**
   * Gets reset account lockout after.
   *
   * @return the reset account lockout after
   */
  @Schema(description = "After this time is elapsed, the recorded number of attempts restarts "
      + "from zero. Default is 30.", defaultValue = "30")
  @JsonProperty(value = "resetAccountLockoutAfter", defaultValue = "30")
  @Value.Default
  default int getResetAccountLockoutAfter() {
    return 30;
  }

  /**
   * Gets simple password regex template.
   *
   * @return the simple password regex template
   */
  @Hidden
  @JsonIgnore
  @Value.Default
  default String getSimplePasswordRegexTemplate() {
    return SIMPLE_PASSWORD_REGEX;
  }

  /**
   * Gets complex password regex template.
   *
   * @return the complex password regex template
   */
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

  /**
   * Gets password description.
   *
   * @param messageSource the message source
   * @param locale the locale
   * @return the password description
   */
  default String getPasswordDescription(MessageSource messageSource, Locale locale) {
    return messageSource.getMessage(
        getPasswordComplexity().getI18nCode(),
        new Object[]{getMinimumPasswordLength(), getMaximumPasswordLength()},
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
   *
   * @author Christian Bremer
   */
  class Builder extends ImmutablePasswordInformation.Builder {

    /**
     * Instantiates a new builder.
     */
    public Builder() {
      super();
    }
  }

}
