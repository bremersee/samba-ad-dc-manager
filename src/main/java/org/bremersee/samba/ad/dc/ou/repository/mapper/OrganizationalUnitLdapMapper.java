/*
 * Copyright 2024 the original author or authors.
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

package org.bremersee.samba.ad.dc.ou.repository.mapper;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveEntryImmutableMapper;
import org.bremersee.samba.ad.dc.repository.mapper.AdEntryLdapMapper;
import org.bremersee.samba.ad.dc.ou.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * The organizational unit ldap mapper.
 *
 * @author Christian Bremer
 */
@Component
public class OrganizationalUnitLdapMapper extends LdaptiveEntryImmutableMapper<OrganizationalUnit> {

  private final AdEntryLdapMapper adEntryLdapMapper;

  @Getter
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public OrganizationalUnitLdapMapper() {
    adEntryLdapMapper = new AdEntryLdapMapper();
    mappedAttributes = initMappedAttributesOfOrganizationalUnit();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfOrganizationalUnit() {
    var attributeNames = new LinkedHashSet<>(adEntryLdapMapper.getMappedAttributes());
    attributeNames.add(AdConstants.DESCRIPTION);
    attributeNames.add(AdConstants.IS_CRITICAL_SYSTEM_OBJECT);
    attributeNames.add(AdConstants.NAME);
    return attributeNames;
  }

  @Override
  public String[] getObjectClasses() {
    return new String[0];
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
  public String mapDn(OrganizationalUnit domainObject) {
    Assert.hasText(domainObject.getDistinguishedName(), "DN of ldap entry is required.");
    return domainObject.getDistinguishedName();
  }

  @Override
  public OrganizationalUnit map(LdapEntry source) {
    if (isEmpty(source)) {
      return null;
    }
    OrganizationalUnit.Builder builder = OrganizationalUnit.builder()
        .from(adEntryLdapMapper.map(source));
    AdConstants.DESCRIPTION
        .getValue(source)
        .ifPresent(builder::description);
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT
        .getValue(source, false)
        .ifPresent(builder::systemOu);
    AdConstants.NAME
        .getValue(source)
        .ifPresent(builder::name);
    return builder.build();
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(
      OrganizationalUnit source,
      LdapEntry destination) {

    if (isNull(source) || isNull(destination)) {
      return new AttributeModification[0];
    }
    var modifications = new ArrayList<>(Arrays.asList(adEntryLdapMapper
        .mapAndComputeModifications(source, destination)));
    AdConstants.DESCRIPTION
        .setValue(destination, source.getName())
        .ifPresent(modifications::add);
    boolean isSystemOu = AdConstants.IS_CRITICAL_SYSTEM_OBJECT
        .getValue(destination, source.isSystemOu())
        .orElse(false);
    if (!isSystemOu) {
      AdConstants.NAME
          .setValue(destination, source.getName())
          .ifPresent(modifications::add);
    }
    return modifications.toArray(AttributeModification[]::new);
  }

}
