package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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

  @NotNull
  DomainGroup withDistinguishedName(@NotNull String distinguishedName);

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

  /**
   * The members of the domain group.
   */
  @Schema(description = "The members of the domain group.")
  @Value.Default
  default List<String> getMembers() {
    return List.of();
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

  @Schema(description = "Groups have no primary group id. It is always null.",
      accessMode = AccessMode.READ_ONLY)
  @JsonProperty(value = "primaryGroupId", access = Access.READ_ONLY)
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
