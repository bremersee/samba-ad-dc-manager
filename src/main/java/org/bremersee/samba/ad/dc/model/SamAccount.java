package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.List;
import java.util.Objects;
import org.bremersee.samba.ad.dc.common.model.AdEntry;
import org.bremersee.samba.ad.dc.common.model.NameProvider;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * The base of a 'SamAccount' like 'User', 'Group' and 'Computer'.
 *
 * @author Christian Bremer
 */
@Schema(description = "The base of a 'SamAccount' like 'User', 'Group' and 'Computer'.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableSamAccount.class)
@JsonDeserialize(as = ImmutableSamAccount.class)
public interface SamAccount extends AdEntry, NameProvider, Comparable<SamAccount> {

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
   * Gets primary group id.
   *
   * @return the primary group id
   */
  @Nullable
  Integer getPrimaryGroupId();

  /**
   * Gets memberships.
   *
   * @return the memberships
   */
  @Value.Default
  default List<String> getMemberships() {
    return List.of();
  }

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
  class Builder extends ImmutableSamAccount.Builder {

  }

}
