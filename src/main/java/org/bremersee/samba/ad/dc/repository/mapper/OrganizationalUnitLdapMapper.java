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

package org.bremersee.samba.ad.dc.repository.mapper;

import static java.util.Objects.isNull;
import static org.bremersee.ldaptive.LdaptiveEntryMapper.getAttributeValue;
import static org.bremersee.ldaptive.LdaptiveEntryMapper.setAttribute;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;

/**
 * The organizational unit ldap mapper.
 *
 * @author Christian Bremer
 */
public class OrganizationalUnitLdapMapper extends AdEntryLdapMapper<OrganizationalUnit> {

  @Getter(AccessLevel.PROTECTED)
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public OrganizationalUnitLdapMapper() {
    super(OrganizationalUnit::new);
    mappedAttributes = initMappedAttributesOfOrganizationalUnit();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfOrganizationalUnit() {
    Set<LdaptiveAttribute<?>> attributeNames = new LinkedHashSet<>(super.getMappedAttributes());
    attributeNames.add(AdConstants.DESCRIPTION);
    attributeNames.add(AdConstants.IS_CRITICAL_SYSTEM_OBJECT);
    attributeNames.add(AdConstants.NAME);
    return attributeNames;
  }

  @Override
  public void map(LdapEntry source, OrganizationalUnit destination) {
    if (isNull(source) || isNull(destination)) {
      return;
    }
    super.map(source, destination);

    String description = getAttributeValue(
        source, AdConstants.DESCRIPTION, null);
    destination.setDescription(description);

    boolean isCriticalSystemObject = getAttributeValue(
        source, AdConstants.IS_CRITICAL_SYSTEM_OBJECT, false);
    destination.setSystemOu(isCriticalSystemObject);

    String name = getAttributeValue(source, AdConstants.NAME, null);
    destination.setName(name);
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(
      OrganizationalUnit source,
      LdapEntry destination) {

    if (isNull(source) || isNull(destination)) {
      return new AttributeModification[0];
    }
    var modifications = toNewList(super.mapAndComputeModifications(source, destination));

    setAttribute(
        destination,
        AdConstants.DESCRIPTION,
        source.getDescription(),
        modifications);

    boolean isSystemOu = getAttributeValue(
        destination, AdConstants.IS_CRITICAL_SYSTEM_OBJECT, false);
    if (!isSystemOu) {
      setAttribute(
          destination,
          AdConstants.NAME,
          source.getName(),
          modifications);
    }

    return modifications.toArray(new AttributeModification[0]);
  }

}
