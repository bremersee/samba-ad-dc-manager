package org.bremersee.samba.ad.dc.samaccount.group.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.List;
import java.util.Optional;
import org.bremersee.samba.ad.dc.samaccount.common.model.NisDomainMember;
import org.bremersee.samba.ad.dc.samaccount.common.model.SamAccount;
import org.bremersee.samba.ad.dc.samaccount.common.model.Sid;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.springframework.lang.Nullable;

@Schema(description = "The domain group.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
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

  DomainGroup withDistinguishedName(String distinguishedName);

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
  @Schema(description = "The type of the domain group.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "groupType", required = true)
  @Value.Default
  default DomainGroupTypeContainer getGroupType() {
    return DomainGroupTypeContainer.defaultContainer();
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

  @Schema(description = "The primary group id of this domain group.",
      accessMode = AccessMode.READ_ONLY)
  @JsonProperty(value = "primaryGroupId", access = Access.READ_ONLY)
  @Value.Lazy
  @Override
  default Integer getPrimaryGroupId() {
    return Optional.ofNullable(getSid())
        .map(Sid::getSuffix)
        .orElse(null);
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
  class Builder extends ImmutableDomainGroup.Builder {

  }

}
