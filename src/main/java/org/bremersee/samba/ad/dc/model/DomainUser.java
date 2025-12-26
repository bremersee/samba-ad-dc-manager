package org.bremersee.samba.ad.dc.model;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Locale;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.springframework.lang.Nullable;

@Schema(description = "The domain user.")
@Value.Style(
    visibility = ImplementationVisibility.PUBLIC,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableDomainUser.class)
@JsonDeserialize(as = ImmutableDomainUser.class)
public interface DomainUser extends SamAccount, NisDomainMember {

  @NotNull
  default DomainUser withDistinguishedName(@NotNull String distinguishedName) {
    return builder().distinguishedName(distinguishedName).build();
  }

  @Schema(description = "User's account control.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "accountControl", required = true)
  @Value.Default
  default DomainUserAccountControl getAccountControl() {
    return DomainUserAccountControl.defaultAccountControl();
  }

  @Nullable
  OffsetDateTime getAccountExpires();

  /**
   * User's company.
   */
  @Nullable
  String getCompany();

  /**
   * User's department.
   */
  @Nullable
  String getDepartment();

  /**
   * A description of the user.
   */
  @Nullable
  String getDescription();

  /**
   * User's display name.
   */
  @Nullable
  String getDisplayName();

  /**
   * User's email address.
   */
  @Nullable
  String getEmail();

  /**
   * User's first name.
   */
  @Nullable
  String getFirstName();

  /**
   * User's Unix/RFC2307 GECOS field.
   */
  @Nullable
  String getGecos();

  /**
   * User's Unix/RFC2307 primary GID number.
   */
  @Nullable
  Integer getGidNumber();

  /**
   * User's home directory path.
   */
  @Nullable
  String getHomeDirectory();

  /**
   * User's home drive letter.
   */
  @Nullable
  String getHomeDrive();

  /**
   * User's initials.
   */
  @Nullable
  String getInitials();

  /**
   * User's last logon time.
   */
  @Nullable
  OffsetDateTime getLastLogon();

  /**
   * User's last name.
   */
  @Nullable
  String getLastName();

  @Hidden
  @JsonIgnore
  @Value.Lazy
  default Locale getLocale() {
    return getLocale(Locale.GERMANY);
  }

  default Locale getLocale(Locale defaultLocale) {
    if (isEmpty(getPreferredLanguage())) {
      return defaultLocale;
    }
    Locale locale = Locale.forLanguageTag(getPreferredLanguage());
    if (isEmpty(locale) || locale.getLanguage().isEmpty()) {
      return defaultLocale;
    }
    return locale;
  }

  /**
   * User's Unix/RFC2307 login shell.
   */
  @Nullable
  String getLoginShell();

  /**
   * User's logon count.
   */
  @Nullable
  Integer getLogonCount();

  /**
   * User's mobile phone number.
   */
  @Nullable
  String getMobile();

  @Hidden
  @JsonIgnore
  @Value.Lazy
  @Override
  default String getName() {
    if (!isEmpty(getFirstName()) && !isEmpty(getLastName())) {
      return getFirstName() + " " + getLastName();
    }
    if (!isEmpty(getDisplayName())) {
      return getDisplayName();
    }
    return getSamAccountName();
  }

  /**
   * User's Unix/RFC2307 NIS domain.
   */
  @Nullable
  @Override
  String getNisDomain();

  /**
   * User's password.
   */
  @Nullable
  String getPassword();

  /**
   * Timestamp of the last password change.
   */
  @Nullable
  OffsetDateTime getPasswordLastSet();

  /**
   * User's office location.
   */
  @Nullable
  String getPhysicalDeliveryOfficeName();

  /**
   * User's preferred language. ISO 639-1 language codes. The combinations like de-DE and en-US with
   * ISO-639 and ISO-3166 also work.
   */
  @Schema(description = "User's preferred language. ISO 639-1 language codes. The combinations "
      + "like de-DE and en-US with ISO-639 and ISO-3166 also work.", defaultValue = "de-DE")
  @JsonProperty(value = "preferredLanguage", defaultValue = "de-DE")
  @Value.Default
  default String getPreferredLanguage() {
    return "de-DE";
  }

  /**
   * User's profile path.
   */
  @Nullable
  String getProfilePath();

  /**
   * User's logon script path.
   */
  @Nullable
  String getScriptPath();

  /**
   * User's telephone number.
   */
  @Nullable
  String getTelephoneNumber();

  /**
   * User's job title.
   */
  @Nullable
  String getTitle();

  /**
   * User's Unix/RFC2307 username.
   */
  @Nullable
  String getUid();

  /**
   * User's Unix/RFC2307 numeric UID.
   */
  @Nullable
  Integer getUidNumber();

  /**
   * User's Unix/RFC2307 home directory.
   */
  @Nullable
  String getUnixHomeDirectory();

  /**
   * User's principal name.
   */
  @Nullable
  String getUserPrincipalName();

  /**
   * Gets the immutable builder.
   *
   * @return the builder
   */
  static ImmutableDomainUser.Builder builder() {
    return ImmutableDomainUser.builder();
  }

}
