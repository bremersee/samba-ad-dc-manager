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

/**
 * The domain group member.
 *
 * @author Christian Bremer
 */
@Schema(
    name = ModelConstants.GROUP_MEMBER,
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

  /**
   * Gets member type.
   *
   * @return the member type
   */
  @Schema(description = "The type of the member.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "memberType", required = true)
  DomainGroupMemberType getMemberType();

  /**
   * Gets display name.
   *
   * @return the display name
   */
  @Schema(description = "The display name of the member.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "displayName", required = true)
  String getDisplayName();

  /**
   * Is member boolean.
   *
   * @return the boolean
   */
  @Schema(description = "Determines whether this sam account is a member of the requested group "
      + "or not.", defaultValue = "false")
  @JsonProperty(value = "member", defaultValue = "false")
  @Value.Default
  default boolean isMember() {
    return false;
  }

  /**
   * Is primary member boolean.
   *
   * @return the boolean
   */
  @Schema(description = "Determines whether the requested group is the primary group if this sam "
      + "account or not.", defaultValue = "false")
  @JsonProperty(value = "primaryMember", defaultValue = "false")
  @Value.Default
  default boolean isPrimaryMember() {
    return false;
  }

  /**
   * With member domain group member.
   *
   * @param isMember the is member
   * @return the domain group member
   */
  @NotNull
  default DomainGroupMember withMember(boolean isMember) {
    return builder()
        .from(this)
        .member(isMember)
        .build();
  }

  /**
   * With primary member domain group member.
   *
   * @param isPrimaryMember the is primary member
   * @return the domain group member
   */
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
