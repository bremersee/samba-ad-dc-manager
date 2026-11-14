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

/**
 * The organizational unit.
 *
 * @author Christian Bremer
 */
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

  /**
   * Gets description.
   *
   * @return the description
   */
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

  /**
   * Is system ou boolean.
   *
   * @return the boolean
   */
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
