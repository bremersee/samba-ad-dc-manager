package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import org.immutables.value.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * The base of a 'SamAccount' like 'User', 'Group' and 'Computer'.
 *
 * @author Christian Bremer
 */
//@Schema(implementation = SamAccountIntermediate.class)

@Schema(
    name = "SamAccount",
    description = "Active directory base entry.The base of a 'SamAccount' like 'User', 'Group' and 'Computer'.",
    discriminatorProperty = ModelConstants.DISCRIMINATOR,
    discriminatorMapping = {
        @DiscriminatorMapping(
            value = ModelConstants.COMPUTER,
            schema = DomainComputer.class),
        @DiscriminatorMapping(
            value = ModelConstants.GROUP,
            schema = DomainGroup.class),
        @DiscriminatorMapping(
            value = ModelConstants.GROUP_MEMBER,
            schema = DomainGroupMember.class),
        @DiscriminatorMapping(
            value = ModelConstants.USER,
            schema = DomainUser.class)
    }
)

@JsonSubTypes({
    @JsonSubTypes.Type(value = DomainComputer.class, name = ModelConstants.COMPUTER),
    @JsonSubTypes.Type(value = DomainGroup.class, name = ModelConstants.GROUP),
    @JsonSubTypes.Type(
        value = DomainGroupMember.class,
        name = ModelConstants.GROUP_MEMBER),
    @JsonSubTypes.Type(value = ImmutableDomainUser.class, name = ModelConstants.USER)
})
@JsonSerialize(as = ImmutableSamAccountIntermediate.class)
@JsonDeserialize(as = ImmutableSamAccountIntermediate.class)
public interface SamAccount extends AdEntry, NameProvider, Comparable<SamAccount> {

  @Hidden
  @JsonIgnore
  @Value.Lazy
  @Override
  default AdEntryType getAdEntryType() {
    return AdEntryType.SAM_ACCOUNT;
  }

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
