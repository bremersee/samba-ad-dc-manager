package org.bremersee.samba.ad.dc.model;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.util.ObjectUtils.isEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.springframework.lang.Nullable;

@Schema(
    name = "DomainComputer",
    description = "The domain computer.",
    implementation = DomainComputer.class)
@Value.Style(
    visibility = ImplementationVisibility.PUBLIC,
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
@JsonSerialize(as = ImmutableDomainComputer.class)
@JsonDeserialize(as = ImmutableDomainComputer.class)
public interface DomainComputer extends SamAccount {

  @Override
  default DomainComputer withDistinguishedName(String distinguishedName) {
    return builder()
        .from(this)
        .distinguishedName(requireNonNullElse(distinguishedName, ""))
        .build();
  }

  @Override
  default DomainComputer withCreated(OffsetDateTime created) {
    return builder()
        .from(this)
        .created(created)
        .build();
  }

  @Override
  default DomainComputer withModified(OffsetDateTime modified) {
    return builder()
        .from(this)
        .modified(modified)
        .build();
  }

  @Override
  default DomainComputer withSid(Sid sid) {
    return builder()
        .from(this)
        .sid(sid)
        .build();
  }

  @Override
  default DomainComputer withMemberships(Iterable<String> memberships) {
    return builder()
        .from(this)
        .memberships(Objects.requireNonNullElseGet(memberships, List::of))
        .build();
  }

  @Override
  default DomainComputer withCriticalSystemObject(boolean criticalSystemObject) {
    return builder()
        .from(this)
        .criticalSystemObject(criticalSystemObject)
        .build();
  }

  @Override
  default DomainComputer withPrimaryGroupId(Integer primaryGroupId) {
    return builder()
        .from(this)
        .primaryGroupId(primaryGroupId)
        .build();
  }

  @Schema(description = "The name of the computer.")
  @JsonProperty(value = "name")
  @Nullable
  @Override
  String getName();

  default DomainComputer withName(String name) {
    return builder()
        .from(this)
        .name(name)
        .build();
  }

  @Schema(description = "The primary group ID of the computer.")
  @JsonProperty(value = "primaryGroupId")
  @Nullable
  @Override
  Integer getPrimaryGroupId();

  @Schema(description = "The dns host name of the computer.")
  @Nullable
  String getDnsHostName();

  @Schema(description = "The network addresses of the computer.")
  @Value.Default
  default List<String> getNetworkAddresses() {
    return List.of();
  }

  @Schema(description = "The operating system of the computer.")
  @Nullable
  String getOperatingSystem();

  @Schema(description = "The version operating system.")
  @Nullable
  String getOperatingSystemVersion();

  @Schema(description = "The description of the computer.")
  @Nullable
  String getDescription();

  @Schema(description = "The service principal names of the computer.")
  @Value.Default
  default List<String> getServicePrincipalNames() {
    return List.of();
  }

  @Hidden
  @JsonIgnore
  @Value.Lazy
  default String getSamAccountNameWithoutTrailingDollarSign() {
    String tmpName = getSamAccountName();
    if (isEmpty(tmpName)) {
      return tmpName;
    }
    return tmpName.substring(0, tmpName.length() - 1);
  }

  /**
   * Gets the immutable builder.
   *
   * @return the builder
   */
  static ImmutableDomainComputer.Builder builder() {
    return ImmutableDomainComputer.builder();
  }

}
