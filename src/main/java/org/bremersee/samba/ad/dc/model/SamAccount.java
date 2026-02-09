package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import org.immutables.value.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * The base of a 'SamAccount' like 'User', 'Group' and 'Computer'.
 *
 * @author Christian Bremer
 */
@Schema(description = "The base of a 'SamAccount' like 'User', 'Group' and 'Computer'.")
public interface SamAccount extends AdEntry, NameProvider, Comparable<SamAccount> {

  @Override
  SamAccount withDistinguishedName(String distinguishedName);

  @Override
  SamAccount withCreated(OffsetDateTime created);

  @Override
  SamAccount withModified(OffsetDateTime modified);

  /**
   * Gets sam account name.
   *
   * @return the sam account name
   */
  @Schema(description = "The unique name of the sam account.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "samAccountName", required = true)
  String getSamAccountName();

  /**
   * Gets sid.
   *
   * @return the sid
   */
  @Schema(description = "The SID of the sam account.")
  @Nullable
  Sid getSid();

  /**
   * With sid.
   *
   * @param sid the sid
   * @return the sam account
   */
  SamAccount withSid(Sid sid);

  /**
   * Determines whether this sam account is a critical system object or not.
   *
   * @return the {@code true}, if this sam account is a critical system object, otherwise
   *     {@code false}
   */
  @Schema(description = "Determines whether this sam account is a critical system object or not.",
      defaultValue = "false")
  @JsonProperty(value = "criticalSystemObject", defaultValue = "false")
  @Value.Default
  default boolean isCriticalSystemObject() {
    return false;
  }

  /**
   * With critical system object.
   *
   * @param criticalSystemObject the critical system object
   * @return the sam account
   */
  SamAccount withCriticalSystemObject(boolean criticalSystemObject);

  /**
   * Gets primary group id.
   *
   * @return the primary group id
   */
  @Nullable
  Integer getPrimaryGroupId();

  /**
   * With primary group id.
   *
   * @param primaryGroupId the primary group id
   * @return the sam account
   */
  SamAccount withPrimaryGroupId(Integer primaryGroupId);

  /**
   * Gets memberships.
   *
   * @return the memberships
   */
  @Value.Default
  default List<String> getMemberships() {
    return List.of();
  }

  /**
   * With memberships.
   *
   * @param memberships the memberships
   * @return the sam account
   */
  SamAccount withMemberships(Iterable<String> memberships);

  @Hidden
  @JsonIgnore
  @Value.Lazy
  @Override
  default String getName() {
    return getSamAccountName();
  }

  @Override
  default int compareTo(@NonNull SamAccount o) {
    String s1 = Objects.requireNonNullElse(getSamAccountName(), "");
    String s2 = Objects.requireNonNullElse(o.getSamAccountName(), "");
    return s1.compareToIgnoreCase(s2);
  }

}
