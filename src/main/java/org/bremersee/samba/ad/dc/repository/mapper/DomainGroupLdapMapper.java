/*
 * Copyright 2019-2020 the original author or authors.
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

package org.bremersee.samba.ad.dc.repository.mapper;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveEntryImmutableMapper;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainGroupType;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.ldaptive.dn.Dn;
import org.springframework.util.Assert;

/**
 * The domain group ldap mapper.
 *
 * @author Christian Bremer
 */
@Slf4j
public class DomainGroupLdapMapper extends LdaptiveEntryImmutableMapper<DomainGroup> {

  private final SamAccountLdapMapper samAccountLdapMapper;

  private final Supplier<Boolean> rfc2307EnabledSupplier;

  @Getter(AccessLevel.PROTECTED)
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public DomainGroupLdapMapper(Supplier<Boolean> rfc2307EnabledSupplier) {
    samAccountLdapMapper = new SamAccountLdapMapper();
    this.rfc2307EnabledSupplier = rfc2307EnabledSupplier;
    mappedAttributes = initMappedAttributesOfDomainGroup();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfDomainGroup() {
    var attributeNames = new LinkedHashSet<>(samAccountLdapMapper.getMappedAttributes());
    attributeNames.add(AdConstants.GROUP_TYPE);
    attributeNames.add(AdConstants.DESCRIPTION);
    attributeNames.add(AdConstants.GID_NUMBER);
    attributeNames.add(AdConstants.MAIL);
    attributeNames.add(AdConstants.GROUP_MEMBER);
    attributeNames.add(AdConstants.NIS_DOMAIN);
    return attributeNames;
  }

  @Override
  public String[] getObjectClasses() {
    return new String[]{
        "group",
        "top"
    };
  }

  @Override
  public String[] getMappedAttributeNames() {
    return getMappedAttributes().stream()
        .map(LdaptiveAttribute::getName)
        .toArray(String[]::new);
  }

  @Override
  public String[] getBinaryAttributeNames() {
    return getMappedAttributes().stream()
        .filter(LdaptiveAttribute::isBinary)
        .map(LdaptiveAttribute::getName)
        .toArray(String[]::new);
  }

  @Override
  public String mapDn(DomainGroup domainObject) {
    Assert.hasText(domainObject.getDistinguishedName(),
        "Distinguished name of domain group is required.");
    return domainObject.getDistinguishedName();
  }

  @Override
  public DomainGroup map(LdapEntry source) {
    if (isEmpty(source)) {
      return null;
    }
    var builder = DomainGroup.builder()
        .from(samAccountLdapMapper.map(source));
    AdConstants.GROUP_TYPE
        .getValue(source)
        .ifPresent(groupTypeValue -> builder
            .groupType(DomainGroupType.from(groupTypeValue)));
    AdConstants.DESCRIPTION
        .getValue(source)
        .ifPresent(builder::description);
    AdConstants.GID_NUMBER
        .getValue(source)
        .ifPresent(builder::gidNumber);
    AdConstants.MAIL
        .getValue(source)
        .ifPresent(builder::email);
    builder.members(AdConstants.GROUP_MEMBER
        .getValues(source)
        .map(dn -> dn.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER))
        .toList());
    AdConstants.NIS_DOMAIN
        .getValue(source)
        .ifPresent(builder::nisDomain);
    return builder.build();
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(
      DomainGroup source,
      LdapEntry destination) {

    if (isNull(source) || isNull(destination)) {
      return new AttributeModification[0];
    }
    var modifications = new ArrayList<>(Arrays.asList(samAccountLdapMapper
        .mapAndComputeModifications(source, destination)));
    AdConstants.DESCRIPTION
        .setValue(destination, source.getDescription())
        .ifPresent(modifications::add);
    AdConstants.MAIL
        .setValue(destination, source.getEmail())
        .ifPresent(modifications::add);
    AdConstants.GROUP_MEMBER
        .setValues(destination, source.getMembers().stream().map(Dn::new).toList())
        .ifPresent(modifications::add);

    if (Boolean.TRUE.equals(rfc2307EnabledSupplier.get())) {
      AdConstants.GID_NUMBER
          .setValue(destination, source.getGidNumber())
          .ifPresent(modifications::add);
      AdConstants.NIS_DOMAIN
          .setValue(destination, source.getNisDomain())
          .ifPresent(modifications::add);
      AdConstants.NIS_NAME
          .setValue(destination, source.getSamAccountName())
          .ifPresent(modifications::add);
    }

    return modifications.toArray(AttributeModification[]::new);
  }

}

