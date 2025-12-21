package org.bremersee.samba.ad.dc.model;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.immutables.value.Value;
import org.ldaptive.dn.Dn;
import org.springframework.lang.Nullable;

/**
 * The active directory base entry.
 *
 * @author Christian Bremer
 */
@Schema(description = "Active directory base entry.")
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
   * Gets distinguished name normalized.
   *
   * @return the distinguished name normalized
   */
  @Hidden
  @JsonIgnore
  @Value.Lazy
  default String getDistinguishedNameNormalized() {
    return isEmpty(getDn()) ? null : getDn().format();
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
   * Gets parent distinguished name normalized.
   *
   * @return the parent distinguished name
   */
  @Hidden
  @JsonIgnore
  @Value.Lazy
  default String getParentDistinguishedNameNormalized() {
    return Optional.ofNullable(getDn())
        .map(Dn::getParent)
        .map(Dn::format)
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

}
