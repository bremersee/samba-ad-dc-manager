package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
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
@Schema(
    name = "AdEntry",
    description = "Active directory base entry.",
    discriminatorProperty = ModelConstants.DISCRIMINATOR,
    discriminatorMapping = {
        @DiscriminatorMapping(value = ModelConstants.DNS_ENTRY, schema = ImmutableDnsEntry.class),
        @DiscriminatorMapping(value = ModelConstants.DNS_ZONE, schema = ImmutableDnsZone.class),
        @DiscriminatorMapping(
            value = ModelConstants.COMPUTER,
            schema = ImmutableDomainComputer.class),
        @DiscriminatorMapping(value = ModelConstants.GROUP, schema = ImmutableDomainGroup.class),
        @DiscriminatorMapping(
            value = ModelConstants.GROUP_MEMBER,
            schema = ImmutableDomainGroupMember.class),
        @DiscriminatorMapping(value = ModelConstants.USER, schema = ImmutableDomainUser.class),
        @DiscriminatorMapping(
            value = ModelConstants.ORGANIZATIONAL_UNIT,
            schema = ImmutableOrganizationalUnit.class)
    })
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = ModelConstants.DISCRIMINATOR, visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ImmutableDnsEntry.class, name = ModelConstants.DNS_ENTRY),
    @JsonSubTypes.Type(value = ImmutableDnsZone.class, name = ModelConstants.DNS_ZONE),
    @JsonSubTypes.Type(value = ImmutableDomainComputer.class, name = ModelConstants.COMPUTER),
    @JsonSubTypes.Type(value = ImmutableDomainGroup.class, name = ModelConstants.GROUP),
    @JsonSubTypes.Type(
        value = ImmutableDomainGroupMember.class,
        name = ModelConstants.GROUP_MEMBER),
    @JsonSubTypes.Type(value = ImmutableDomainUser.class, name = ModelConstants.USER),
    @JsonSubTypes.Type(
        value = ImmutableOrganizationalUnit.class,
        name = ModelConstants.ORGANIZATIONAL_UNIT)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public interface AdEntry extends Serializable, DistinguishedNameProvider {

  /**
   * The distinguished name in the active directory.
   *
   * @return the distinguished name
   */
  @Schema(description = "The distinguished name.")
  @Value.Default
  @Override
  default String getDistinguishedName() {
    return "";
  }

  /**
   * With distinguished name.
   *
   * @param distinguishedName the distinguished name
   * @return the ad entry
   */
  AdEntry withDistinguishedName(String distinguishedName);

  /**
   * Gets distinguished name normalized.
   *
   * @return the distinguished name normalized
   */
  @Hidden
  @JsonIgnore
  @Value.Lazy
  default String getDistinguishedNameNormalized() {
    return Optional.ofNullable(getDn())
        .map(Dn::format)
        .orElse("");
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
        .orElse("");
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
        .orElse("");
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
   * With created.
   *
   * @param created the created
   * @return the ad entry
   */
  AdEntry withCreated(OffsetDateTime created);

  /**
   * The last modification date.
   *
   * @return the last modification date
   */
  @Schema(description = "The last modification date.")
  @Nullable
  OffsetDateTime getModified();

  /**
   * With modified.
   *
   * @param modified the modified
   * @return the ad entry
   */
  AdEntry withModified(OffsetDateTime modified);

  /**
   * Gets dn.
   *
   * @return the dn
   */
  @Hidden
  @JsonIgnore
  @Value.Lazy
  default Dn getDn() {
    return Optional.ofNullable(getDistinguishedName())
        .map(Dn::new)
        .orElseGet(() -> new Dn(""));
  }

}
