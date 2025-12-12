package org.bremersee.samba.ad.dc.common.model;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bremersee.samba.ad.dc.common.DnTool;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.ldaptive.dn.Dn;
import org.springframework.lang.Nullable;

/**
 * The active directory base entry.
 *
 * @author Christian Bremer
 */
@Schema(description = "Active directory base entry.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableAdEntry.class)
@JsonDeserialize(as = ImmutableAdEntry.class)
public interface AdEntry extends DistinguishedNameProvider {

  /**
   * The distinguished name in the active directory.
   *
   * @return the distinguished name
   */
  @Schema(description = "The distinguished name.")
  @Nullable
  @Override
  String getDistinguishedName();

  /**
   * Gets distinguished name unformatted.
   *
   * @return the distinguished name unformatted
   */
  @Hidden
  @JsonIgnore
  @Value.Lazy
  default String getDistinguishedNameUnformatted() {
    return isEmpty(getDn()) ? null : getDn().format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER);
  }

  /**
   * Gets parent distinguished name.
   *
   * @return the parent distinguished name
   */
  @Hidden
  @JsonIgnore
  @Value.Lazy
  default String getParentDistinguishedName() {
    return Optional.ofNullable(getDn())
        .map(Dn::getParent)
        .map(dn -> dn.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER))
        .orElse(null);
  }

  /**
   * Gets name tree.
   *
   * @return the name tree
   */
  @Hidden
  @JsonIgnore
  @Value.Lazy
  default String getNameTree() { // ou is reverse
    return Stream.ofNullable(getDn())
        .map(Dn::getRDns)
        .flatMap(Collection::stream)
        .filter(rdn -> !rdn.getNameValue().hasName("dc"))
        .map(rdn -> rdn.getNameValue().getStringValue())
        .collect(Collectors.joining(" → "));
  }

  /**
   * Gets the creation date.
   *
   * @return the creation date
   */
  @Schema(description = "The creation date.")
  @Nullable
  OffsetDateTime getCreated();

  /**
   * The last modification date.
   *
   * @return the last modification date
   */
  @Schema(description = "The last modification date.")
  @Nullable
  OffsetDateTime getModified();

  /**
   * Gets dn.
   *
   * @return the dn
   */
  @Hidden
  @JsonIgnore
  @Value.Lazy
  default Dn getDn() {
    if (DnTool.isValidDn(getDistinguishedName())) {
      return new Dn(getDistinguishedName());
    }
    return null;
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
  class Builder extends ImmutableAdEntry.Builder {

  }

}
