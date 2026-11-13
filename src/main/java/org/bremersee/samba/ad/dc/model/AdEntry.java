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
import org.jspecify.annotations.Nullable;
import org.ldaptive.dn.Dn;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

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
        @DiscriminatorMapping(
            value = ModelConstants.DNS_ENTRY,
            schema = DnsEntry.class),
        @DiscriminatorMapping(
            value = ModelConstants.DNS_ZONE,
            schema = DnsZone.class),
        @DiscriminatorMapping(
            value = ModelConstants.ORGANIZATIONAL_UNIT,
            schema = OrganizationalUnit.class),

        @DiscriminatorMapping(
            value = ModelConstants.SAM_ACCOUNT,
            schema = SamAccount.class),
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
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = ModelConstants.DISCRIMINATOR, visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DnsEntry.class, name = ModelConstants.DNS_ENTRY),
    @JsonSubTypes.Type(value = DnsZone.class, name = ModelConstants.DNS_ZONE),
    @JsonSubTypes.Type(
        value = OrganizationalUnit.class,
        name = ModelConstants.ORGANIZATIONAL_UNIT),

    @JsonSubTypes.Type(value = SamAccount.class, name = ModelConstants.SAM_ACCOUNT),
    @JsonSubTypes.Type(value = DomainComputer.class, name = ModelConstants.COMPUTER),
    @JsonSubTypes.Type(value = DomainGroup.class, name = ModelConstants.GROUP),
    @JsonSubTypes.Type(
        value = DomainGroupMember.class,
        name = ModelConstants.GROUP_MEMBER),
    @JsonSubTypes.Type(value = ImmutableDomainUser.class, name = ModelConstants.USER)
})


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(as = ImmutableAdEntryIntermediate.class)
@JsonDeserialize(as = ImmutableAdEntryIntermediate.class)
public interface AdEntry extends Serializable, DistinguishedNameProvider {

  /**
   * Gets ad entry type.
   *
   * @return the ad entry type
   */
  @Hidden
  @JsonIgnore
  @Value.Lazy
  default AdEntryType getAdEntryType() {
    return AdEntryType.AD_ENTRY;
  }

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
