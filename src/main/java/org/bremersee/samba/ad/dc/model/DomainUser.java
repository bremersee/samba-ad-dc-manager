package org.bremersee.samba.ad.dc.model;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.util.ObjectUtils.isEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.springframework.lang.Nullable;

/**
 * The interface Domain user.
 */
@Schema(name = "DomainUser", description = "The domain user.", implementation = DomainUser.class)
@Value.Style(
    visibility = ImplementationVisibility.PUBLIC,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*",
    passAnnotations = {
        Schema.class
    })
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableDomainUser.class)
@JsonDeserialize(as = ImmutableDomainUser.class)
public interface DomainUser extends SamAccount, NisDomainMember {

  /**
   * With distinguished name domain user.
   *
   * @param distinguishedName the distinguished name
   * @return the domain user
   */
  @Override
  default DomainUser withDistinguishedName(String distinguishedName) {
    return builder()
        .from(this)
        .distinguishedName(requireNonNullElse(distinguishedName, ""))
        .build();
  }

  @Override
  default DomainUser withCreated(OffsetDateTime created) {
    return builder()
        .from(this)
        .created(created)
        .build();
  }

  @Override
  default DomainUser withModified(OffsetDateTime modified) {
    return builder()
        .from(this)
        .modified(modified)
        .build();
  }

  @Override
  default DomainUser withSid(Sid sid) {
    return builder()
        .from(this)
        .sid(sid)
        .build();
  }

  @Override
  default DomainUser withCriticalSystemObject(boolean criticalSystemObject) {
    return builder()
        .from(this)
        .criticalSystemObject(criticalSystemObject)
        .build();
  }

  @Override
  default DomainUser withPrimaryGroupId(Integer primaryGroupId) {
    return builder()
        .from(this)
        .primaryGroupId(primaryGroupId)
        .build();
  }

  @Override
  default DomainUser withMemberships(Iterable<String> memberships) {
    return builder()
        .from(this)
        .memberships(Objects.requireNonNullElseGet(memberships, List::of))
        .build();
  }

  /**
   * Gets account control.
   *
   * @return the account control
   */
  @Schema(description = "User's account control.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "accountControl", required = true)
  @Value.Default
  default DomainUserAccountControl getAccountControl() {
    return DomainUserAccountControl.defaultAccountControl();
  }

  /**
   * Gets account expires.
   *
   * @return the account expires
   */
  @Nullable
  OffsetDateTime getAccountExpires();

  /**
   * User's company.
   *
   * @return the company
   */
  @Nullable
  String getCompany();

  /**
   * User's department.
   *
   * @return the department
   */
  @Nullable
  String getDepartment();

  /**
   * A description of the user.
   *
   * @return the description
   */
  @Nullable
  String getDescription();

  /**
   * User's display name.
   *
   * @return the display name
   */
  @Nullable
  String getDisplayName();

  /**
   * With email.
   *
   * @param email the email
   * @return the domain user
   */
  default DomainUser withEmail(String email) {
    return builder()
        .from(this)
        .email(email)
        .build();
  }

  /**
   * User's email address.
   *
   * @return the email
   */
  @Nullable
  String getEmail();

  /**
   * User's first name.
   *
   * @return the first name
   */
  @Nullable
  String getFirstName();

  /**
   * User's Unix/RFC2307 GECOS field.
   *
   * @return the gecos
   */
  @Nullable
  String getGecos();

  /**
   * User's Unix/RFC2307 primary GID number.
   *
   * @return the gid number
   */
  @Nullable
  Integer getGidNumber();

  /**
   * User's home directory path.
   *
   * @return the home directory
   */
  @Nullable
  String getHomeDirectory();

  /**
   * User's home drive letter.
   *
   * @return the home drive
   */
  @Nullable
  String getHomeDrive();

  /**
   * User's initials.
   *
   * @return the initials
   */
  @Nullable
  String getInitials();

  /**
   * User's last logon time.
   *
   * @return the last logon
   */
  @Nullable
  OffsetDateTime getLastLogon();

  /**
   * User's last name.
   *
   * @return the last name
   */
  @Nullable
  String getLastName();

  /**
   * Gets locale.
   *
   * @return the locale
   */
  @Hidden
  @JsonIgnore
  @Value.Lazy
  default Locale getLocale() {
    return getLocale(Locale.GERMANY);
  }

  /**
   * Gets locale.
   *
   * @param defaultLocale the default locale
   * @return the locale
   */
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
   *
   * @return the login shell
   */
  @Nullable
  String getLoginShell();

  /**
   * User's logon count.
   *
   * @return the logon count
   */
  @Nullable
  Integer getLogonCount();

  /**
   * User's mobile phone number.
   *
   * @return the mobile
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
   * Timestamp of the last password change.
   *
   * @return the password last set
   */
  @Nullable
  OffsetDateTime getPasswordLastSet();

  /**
   * User's office location.
   *
   * @return the physical delivery office name
   */
  @Nullable
  String getPhysicalDeliveryOfficeName();

  /**
   * User's preferred language. ISO 639-1 language codes. The combinations like de-DE and en-US with
   * ISO-639 and ISO-3166 also work.
   *
   * @return the preferred language
   */
  @Schema(description = "User's preferred language. ISO 639-1 language codes. The combinations "
      + "like de-DE and en-US with ISO-639 and ISO-3166 also work.", defaultValue = "de-DE")
  @JsonProperty(value = "preferredLanguage", defaultValue = "de-DE")
  @Value.Default
  default String getPreferredLanguage() {
    return "de-DE";
  }

  @Schema(description = "The primary group ID of the user.")
  @JsonProperty(value = "primaryGroupId")
  @Nullable
  @Override
  Integer getPrimaryGroupId();

  /**
   * User's profile path.
   *
   * @return the profile path
   */
  @Nullable
  String getProfilePath();

  /**
   * User's logon script path.
   *
   * @return the script path
   */
  @Nullable
  String getScriptPath();

  /**
   * User's telephone number.
   *
   * @return the telephone number
   */
  @Nullable
  String getTelephoneNumber();

  /**
   * User's job title.
   *
   * @return the title
   */
  @Nullable
  String getTitle();

  /**
   * User's Unix/RFC2307 username.
   *
   * @return the uid
   */
  @Nullable
  String getUid();

  /**
   * User's Unix/RFC2307 numeric UID.
   *
   * @return the uid number
   */
  @Nullable
  Integer getUidNumber();

  /**
   * User's Unix/RFC2307 home directory.
   *
   * @return the unix home directory
   */
  @Nullable
  String getUnixHomeDirectory();

  /**
   * User's principal name.
   *
   * @return the user principal name
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
