package org.bremersee.samba.ad.dc.model;

import static java.util.Objects.requireNonNullElse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Schema(
    name = ModelConstants.ORGANIZATIONAL_UNIT,
    description = "The organizational unit in an active directory.")
@Value.Style(
    visibility = ImplementationVisibility.PUBLIC,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonTypeName(ModelConstants.ORGANIZATIONAL_UNIT)
@JsonSerialize(as = ImmutableOrganizationalUnit.class)
@JsonDeserialize(as = ImmutableOrganizationalUnit.class)
public interface OrganizationalUnit extends AdEntry, NameProvider, Comparable<OrganizationalUnit> {

  @Hidden
  @JsonIgnore
  @Value.Lazy
  @Override
  default AdEntryType getAdEntryType() {
    return AdEntryType.ORGANIZATIONAL_UNIT;
  }

  @Override
  default OrganizationalUnit withDistinguishedName(String distinguishedName) {
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
  default OrganizationalUnit withCreated(OffsetDateTime created) {
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
  default OrganizationalUnit withModified(OffsetDateTime modified) {
    return builder()
        .from(this)
        .modified(modified)
        .build();
  }

  @Nullable
  String getDescription();

  @Schema(description = "The name of this organizational unit.",
      requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "name", required = true)
  @Override
  String getName();

  @Hidden
  @JsonIgnore
  @Value.Lazy
  @Override
  default String getNameTree() {
    String nameTree = Optional.ofNullable(getDn())
        .map(dn -> new ArrayList<>(dn.getRDns()))
        .map(list -> {
          Collections.reverse(list);
          return list;
        })
        .orElseGet(ArrayList::new)
        .stream()
        .filter(rdn -> !rdn.getNameValue().hasName("dc"))
        .map(rdn -> rdn.getNameValue().getStringValue())
        .collect(Collectors.joining(" → "));
    if (nameTree.isEmpty()) {
      return getName();
    }
    return nameTree;
  }

  @Schema(description = "Determines whether this organizational unit is a system one or not.",
      defaultValue = "false")
  @JsonProperty(value = "systemOu", defaultValue = "false")
  @Value.Default
  default boolean isSystemOu() {
    return false;
  }

  @Override
  default int compareTo(@NonNull OrganizationalUnit o) {
    String s0 = Objects.requireNonNullElse(getDistinguishedName(), "");
    String s1 = Objects.requireNonNullElse(o.getDistinguishedName(), "");
    return s0.compareToIgnoreCase(s1);
  }

  /**
   * Gets the immutable builder.
   *
   * @return the builder
   */
  static ImmutableOrganizationalUnit.Builder builder() {
    return ImmutableOrganizationalUnit.builder();
  }

}
