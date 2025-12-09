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
import static org.bremersee.ldaptive.LdaptiveEntryMapper.getAttributeValue;
import static org.bremersee.ldaptive.LdaptiveEntryMapper.getAttributeValuesAsList;
import static org.bremersee.ldaptive.LdaptiveEntryMapper.setAttribute;
import static org.bremersee.ldaptive.LdaptiveEntryMapper.setAttributes;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainGroupTypeContainer;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.ldaptive.dn.Dn;

/**
 * The domain group ldap mapper.
 *
 * @author Christian Bremer
 */
@Slf4j
public class DomainGroupLdapMapper extends SamAccountLdapMapper<DomainGroup> {

  private final Supplier<Boolean> rfc2307EnabledSupplier;

  @Getter(AccessLevel.PROTECTED)
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public DomainGroupLdapMapper(Supplier<Boolean> rfc2307EnabledSupplier) {
    super(DomainGroup::new);
    this.rfc2307EnabledSupplier = rfc2307EnabledSupplier;
    mappedAttributes = initMappedAttributesOfDomainGroup();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfDomainGroup() {
    Set<LdaptiveAttribute<?>> attributeNames = new LinkedHashSet<>(super.getMappedAttributes());
    attributeNames.add(AdConstants.GROUP_TYPE);
    attributeNames.add(AdConstants.DESCRIPTION);
    attributeNames.add(AdConstants.GID_NUMBER);
    attributeNames.add(AdConstants.MAIL);
    attributeNames.add(AdConstants.GROUP_MEMBER);
    attributeNames.add(AdConstants.NIS_DOMAIN);
    return attributeNames;
  }

  @Override
  public void map(LdapEntry source, DomainGroup destination) {

    if (isNull(source) || isNull(destination)) {
      return;
    }
    super.map(source, destination);

    Integer groupType = getAttributeValue(source, AdConstants.GROUP_TYPE, null);
    destination.setGroupType(new DomainGroupTypeContainer(groupType));

    String description = getAttributeValue(source, AdConstants.DESCRIPTION, null);
    destination.setDescription(description);

    Integer gidNumber = getAttributeValue(source, AdConstants.GID_NUMBER, null);
    destination.setGidNumber(gidNumber);

    String mail = getAttributeValue(source, AdConstants.MAIL, null);
    destination.setEmail(mail);

    List<Dn> members = getAttributeValuesAsList(source, AdConstants.GROUP_MEMBER);
    destination.setMembers(members.stream().map(Dn::format).toList());

    String nisDomain = getAttributeValue(source, AdConstants.NIS_DOMAIN, null);
    destination.setNisDomain(nisDomain);
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(
      DomainGroup source,
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
    setAttribute(
        destination,
        AdConstants.MAIL,
        source.getEmail(),
        modifications);
    setAttributes(
        destination,
        AdConstants.GROUP_MEMBER,
        source.getMembers().stream().map(Dn::new).toList(),
        modifications);

    boolean isCriticalSystemObject = getAttributeValue(
        destination, AdConstants.IS_CRITICAL_SYSTEM_OBJECT, false);
    if (!isCriticalSystemObject) {
      // TODO verify
      // NOT_ALLOWED_ON_RDN, diagnosticMessage=00002016: Modify of 'name' not permitted, must use 'rename' operation instead
      setAttribute(
          destination,
          AdConstants.NAME,
          source.getSamAccountName(),
          modifications);
    }

    if (Boolean.TRUE.equals(rfc2307EnabledSupplier.get())) {
      setAttribute(
          destination,
          AdConstants.GID_NUMBER,
          source.getGidNumber(),
          modifications);
      setAttribute(
          destination,
          AdConstants.NIS_DOMAIN,
          source.getNisDomain(),
          modifications);
      setAttribute(
          destination,
          AdConstants.NIS_NAME,
          source.getSamAccountName(),
          modifications);
    }

    return modifications.toArray(new AttributeModification[0]);
  }

}

