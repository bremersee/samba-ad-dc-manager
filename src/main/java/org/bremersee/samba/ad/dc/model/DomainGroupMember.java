package org.bremersee.samba.ad.dc.model;

import static java.util.Objects.requireNonNullElse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Schema(
    name = "DomainGroupMember",
    description = "The domain group.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonTypeName(ModelConstants.GROUP_MEMBER)
@JsonSerialize(as = ImmutableDomainGroupMember.class)
@JsonDeserialize(as = ImmutableDomainGroupMember.class)
public interface DomainGroupMember extends SamAccount {

  @Hidden
  @JsonIgnore
  @Value.Lazy
  @Override
  default AdEntryType getAdEntryType() {
    return AdEntryType.GROUP_MEMBER;
  }

  @Override
  default DomainGroupMember withDistinguishedName(String distinguishedName) {
    return builder()
        .from(this)
        .distinguishedName(requireNonNullElse(distinguishedName, ""))
        .build();
  }

  @Schema(description = "The creation date.")
  @Nullable
  @Override
  OffsetDateTime getCreated();

  @Override
  default DomainGroupMember withCreated(OffsetDateTime created) {
    return builder()
        .from(this)
        .created(created)
        .build();
  }

  @Schema(description = "The last modification date.")
  @Nullable
  @Override
  OffsetDateTime getModified();

  @Override
  default DomainGroupMember withModified(OffsetDateTime modified) {
    return builder()
        .from(this)
        .modified(modified)
        .build();
  }

  @Schema(description = "The SID of the sam account.")
  @Nullable
  @Override
  Sid getSid();

  @Override
  default DomainGroupMember withSid(Sid sid) {
    return builder()
        .from(this)
        .sid(sid)
        .build();
  }

  @Override
  default DomainGroupMember withCriticalSystemObject(boolean criticalSystemObject) {
    return builder()
        .from(this)
        .criticalSystemObject(criticalSystemObject)
        .build();
  }

  @Override
  default DomainGroupMember withPrimaryGroupId(Integer primaryGroupId) {
    return builder()
        .from(this)
        .primaryGroupId(primaryGroupId)
        .build();
  }

  @Override
  default DomainGroupMember withMemberships(Iterable<String> memberships) {
    return builder()
        .from(this)
        .memberships(Objects.requireNonNullElseGet(memberships, List::of))
        .build();
  }

  @Schema(description = "The type of the member.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "memberType", required = true)
  DomainGroupMemberType getMemberType();

  @Schema(description = "The display name of the member.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "displayName", required = true)
  String getDisplayName();

  @Schema(description = "Determines whether this sam account is a member of the requested group "
      + "or not.", defaultValue = "false")
  @JsonProperty(value = "member", defaultValue = "false")
  @Value.Default
  default boolean isMember() {
    return false;
  }

  @Schema(description = "Determines whether the requested group is the primary group if this sam "
      + "account or not.", defaultValue = "false")
  @JsonProperty(value = "primaryMember", defaultValue = "false")
  @Value.Default
  default boolean isPrimaryMember() {
    return false;
  }

  @NotNull
  default DomainGroupMember withMember(boolean isMember) {
    return builder()
        .from(this)
        .member(isMember)
        .build();
  }

  @NotNull
  default DomainGroupMember withPrimaryMember(boolean isPrimaryMember) {
    return builder()
        .from(this)
        .primaryMember(isPrimaryMember)
        .build();
  }

  @Schema(description = "The primary group ID of the member.")
  @JsonProperty(value = "primaryGroupId")
  @Nullable
  @Override
  Integer getPrimaryGroupId();

  @Override
  default int compareTo(@NonNull SamAccount o) {
    if (o instanceof DomainGroupMember other) {
      String s0 = requireNonNullElse(getDisplayName(), "");
      String s1 = requireNonNullElse(other.getDisplayName(), "");
      int c = s0.compareToIgnoreCase(s1);
      if (c != 0) {
        return c;
      }
    }
    return SamAccount.super.compareTo(o);
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
  class Builder extends ImmutableDomainGroupMember.Builder {

  }

}
