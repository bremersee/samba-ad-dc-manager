package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bremersee.samba.ad.dc.common.model.AdEntry;
import org.bremersee.samba.ad.dc.common.model.NameProvider;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.RDn;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Schema(description = "The organizational unit in an active directory.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableOrganizationalUnit.class)
@JsonDeserialize(as = ImmutableOrganizationalUnit.class)
public interface OrganizationalUnit extends AdEntry, NameProvider, Comparable<OrganizationalUnit> {

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
    String nameTree = Stream.ofNullable(getDn())
        .map(Dn::getRDns)
        .flatMap(rdnList -> {
          List<RDn> rdns = new ArrayList<>(getDn().getRDns());
          Collections.reverse(rdns);
          return rdns.stream();
        })
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
  static Builder builder() {
    return new Builder();
  }

  /**
   * The immutable builder.
   */
  class Builder extends ImmutableOrganizationalUnit.Builder {

  }

}
