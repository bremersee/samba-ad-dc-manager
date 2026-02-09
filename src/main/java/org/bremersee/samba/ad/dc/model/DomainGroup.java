package org.bremersee.samba.ad.dc.model;

import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.springframework.lang.Nullable;

@Schema(description = "The domain group.")
@Value.Style(
    visibility = ImplementationVisibility.PUBLIC,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableDomainGroup.class)
@JsonDeserialize(as = ImmutableDomainGroup.class)
public interface DomainGroup extends SamAccount, NisDomainMember {

  @Override
  default DomainGroup withDistinguishedName(String distinguishedName) {
    return builder()
        .from(this)
        .distinguishedName(requireNonNullElse(distinguishedName, ""))
        .build();
  }

  @Override
  default DomainGroup withCreated(OffsetDateTime created) {
    return builder()
        .from(this)
        .created(created)
        .build();
  }

  @Override
  default DomainGroup withModified(OffsetDateTime modified) {
    return builder()
        .from(this)
        .modified(modified)
        .build();
  }

  @Override
  default DomainGroup withSid(Sid sid) {
    return builder()
        .from(this)
        .sid(sid)
        .build();
  }

  @Override
  default DomainGroup withCriticalSystemObject(boolean criticalSystemObject) {
    return builder()
        .from(this)
        .criticalSystemObject(criticalSystemObject)
        .build();
  }

  @Override
  default DomainGroup withPrimaryGroupId(Integer primaryGroupId) {
    return this;
  }

  @Override
  default DomainGroup withMemberships(Iterable<String> memberships) {
    return builder()
        .from(this)
        .memberships(Objects.requireNonNullElseGet(memberships, List::of))
        .build();
  }

  /**
   * The description of the domain group.
   */
  @Schema(description = "The description of the domain group.")
  @Nullable
  String getDescription();

  /**
   * The email address of the domain group.
   */
  @Schema(description = "The email address of the domain group.")
  @Nullable
  String getEmail();

  /**
   * Group's Unix/RFC2307 GID number.
   */
  @Schema(description = "Group's Unix/RFC2307 GID number.")
  @Nullable
  Integer getGidNumber();

  /**
   * The type of the domain group.
   */
  @Schema(description = "The type of the domain group.")
  @JsonProperty(value = "groupType")
  @Value.Default
  default DomainGroupType getGroupType() {
    return DomainGroupType.defaultGroupType();
  }

  default DomainGroup withGroupType(DomainGroupType groupType) {
    return builder()
        .from(this)
        .groupType(requireNonNullElseGet(groupType, DomainGroupType::defaultGroupType))
        .build();
  }

  /**
   * The members of the domain group.
   */
  @Schema(description = "The members of the domain group.")
  @Value.Default
  default List<String> getMembers() {
    return List.of();
  }

  default DomainGroup withMembers(Iterable<String> members) {
    return builder()
        .from(this)
        .members(requireNonNullElseGet(members, List::of))
        .build();
  }

  /**
   * Group's Unix/RFC2307 NIS domain.
   */
  @Schema(description = "Group's Unix/RFC2307 NIS domain.")
  @Nullable
  String getNisDomain();

  @Schema(description = "The group id of this domain group.",
      accessMode = AccessMode.READ_ONLY)
  @JsonProperty(value = "groupId", access = Access.READ_ONLY)
  @Value.Lazy
  @Nullable
  default Integer getGroupId() {
    return Optional.ofNullable(getSid())
        .map(Sid::getSuffix)
        .orElse(null);
  }

  @Hidden
  @JsonIgnore
  @Value.Lazy
  @Nullable
  @Override
  default Integer getPrimaryGroupId() {
    // Groups have no primary group ID.
    return null;
  }

  /**
   * Gets the immutable builder.
   *
   * @return the builder
   */
  static ImmutableDomainGroup.Builder builder() {
    return ImmutableDomainGroup.builder();
  }

}
