package org.bremersee.samba.ad.dc.model;

import static java.util.Objects.requireNonNullElse;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * The base of a 'SamAccount' like 'User', 'Group' and 'Computer' with immutable builder.
 *
 * @author Christian Bremer
 */
/*
@Schema(
    name = "SamAccount",
    description = "Active directory base entry.The base of a 'SamAccount' like 'User', 'Group' and 'Computer'.",
    discriminatorProperty = ModelConstants.DISCRIMINATOR,
    //implementation = SamAccount.class

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

 */
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
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
@JsonSerialize(as = ImmutableSamAccountIntermediate.class)
@JsonDeserialize(as = ImmutableSamAccountIntermediate.class)
public interface SamAccountIntermediate extends SamAccount {

  @Override
  default SamAccountIntermediate withDistinguishedName(String distinguishedName) {
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
  default SamAccountIntermediate withCreated(OffsetDateTime created) {
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
  default SamAccountIntermediate withModified(OffsetDateTime modified) {
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
  default SamAccountIntermediate withSid(Sid sid) {
    return builder()
        .from(this)
        .sid(sid)
        .build();
  }

  @Override
  default SamAccountIntermediate withCriticalSystemObject(boolean criticalSystemObject) {
    return builder()
        .from(this)
        .criticalSystemObject(criticalSystemObject)
        .build();
  }

  @Override
  default SamAccountIntermediate withPrimaryGroupId(Integer primaryGroupId) {
    return builder()
        .from(this)
        .primaryGroupId(primaryGroupId)
        .build();
  }

  @Override
  default SamAccountIntermediate withMemberships(Iterable<String> memberships) {
    return builder()
        .from(this)
        .memberships(Objects.requireNonNullElseGet(memberships, List::of))
        .build();
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
  class Builder extends ImmutableSamAccountIntermediate.Builder {

  }

}
