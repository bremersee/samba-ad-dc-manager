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

package org.bremersee.samba.ad.dc.repository.mapper;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveEntryImmutableMapper;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class DomainComputerLdapMapper extends LdaptiveEntryImmutableMapper<DomainComputer>
    implements AdEntryLdapMapperDelegate<DomainComputer> {

  private final SamAccountLdapMapper samAccountLdapMapper;

  /**
   * --- GETTER ---
   * Gets the unmodifiable set of mapped attributes.
   *
   * @return the unmodifiable set of mapped attributes
   */
  @Getter
  @SuppressWarnings("JavadocDeclaration")
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public DomainComputerLdapMapper() {
    samAccountLdapMapper = new SamAccountLdapMapper();
    mappedAttributes = initMappedAttributesOfDomainComputer();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfDomainComputer() {
    var attributeNames = new LinkedHashSet<>(samAccountLdapMapper.getMappedAttributes());
    attributeNames.add(AdConstants.NAME);
    attributeNames.add(AdConstants.COMPUTER_DNS_HOST_NAME);
    attributeNames.add(AdConstants.COMPUTER_NETWORK_ADDRESS);
    attributeNames.add(AdConstants.COMPUTER_OPERATING_SYSTEM);
    attributeNames.add(AdConstants.COMPUTER_OPERATING_SYSTEM_VERSION);
    attributeNames.add(AdConstants.DESCRIPTION);
    attributeNames.add(AdConstants.COMPUTER_SERVICE_PRINCIPAL_NAME);
    return Collections.unmodifiableSet(attributeNames);
  }

  @Override
  public String[] getObjectClasses() {
    return new String[]{
        AdConstants.OBJECT_CLASS_COMPUTER,
        "organizationalPerson",
        "person",
        "top",
        AdConstants.OBJECT_CLASS_USER
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
  public String mapDn(DomainComputer domainObject) {
    Assert.hasText(domainObject.getDistinguishedName(),
        "Distinguished name of domain computer required.");
    return domainObject.getDistinguishedName();
  }

  @Override
  public DomainComputer map(LdapEntry source) {
    if (isEmpty(source)) {
      return null;
    }
    var builder = DomainComputer.builder()
        .from(samAccountLdapMapper.map(source));
    AdConstants.NAME
        .getValue(source)
        .ifPresent(builder::name);
    AdConstants.COMPUTER_DNS_HOST_NAME
        .getValue(source)
        .ifPresent(builder::dnsHostName);
    builder.networkAddresses(AdConstants.COMPUTER_NETWORK_ADDRESS
        .getValues(source)
        .toList());
    AdConstants.COMPUTER_OPERATING_SYSTEM
        .getValue(source)
        .ifPresent(builder::operatingSystem);
    AdConstants.COMPUTER_OPERATING_SYSTEM_VERSION
        .getValue(source)
        .ifPresent(builder::operatingSystemVersion);
    AdConstants.DESCRIPTION
        .getValue(source)
        .ifPresent(builder::description);
    builder.servicePrincipalNames(AdConstants.COMPUTER_SERVICE_PRINCIPAL_NAME
        .getValues(source)
        .toList());
    return builder.build();
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(
      DomainComputer source,
      LdapEntry destination) {

    if (isNull(source) || isNull(destination)) {
      return new AttributeModification[0];
    }
    var modifications = new ArrayList<>(Arrays.asList(samAccountLdapMapper
        .mapAndComputeModifications(source, destination)));
    AdConstants.DESCRIPTION
        .setValue(destination, source.getDescription())
        .ifPresent(modifications::add);
    return modifications.toArray(new AttributeModification[0]);
  }

  @Override
  public boolean canMap(LdapEntry ldapEntry) {
    if (isEmpty(ldapEntry)) {
      return false;
    }
    return AdConstants.OBJECT_CLASS.getValues(ldapEntry)
        .anyMatch(AdConstants.OBJECT_CLASS_COMPUTER::equalsIgnoreCase);
  }
}
