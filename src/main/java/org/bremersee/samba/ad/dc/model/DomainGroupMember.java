package org.bremersee.samba.ad.dc.model;

import static java.util.Objects.requireNonNullElse;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.springframework.lang.NonNull;

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
@JsonSerialize(as = ImmutableDomainGroupMember.class)
@JsonDeserialize(as = ImmutableDomainGroupMember.class)
public interface DomainGroupMember extends SamAccount {

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

  default DomainGroupMember withMember(boolean isMember) {
    return builder()
        .from(this)
        .member(isMember)
        .build();
  }

  default DomainGroupMember withPrimaryMember(boolean isPrimaryMember) {
    return builder()
        .from(this)
        .primaryMember(isPrimaryMember)
        .build();
  }

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
