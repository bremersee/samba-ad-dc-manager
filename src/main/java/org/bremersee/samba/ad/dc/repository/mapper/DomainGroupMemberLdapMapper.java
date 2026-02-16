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

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveEntryImmutableMapper;
import org.bremersee.samba.ad.dc.model.DomainGroupMember;
import org.bremersee.samba.ad.dc.model.DomainGroupMemberType;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.dn.Dn;
import org.springframework.util.Assert;

public class DomainGroupMemberLdapMapper extends LdaptiveEntryImmutableMapper<DomainGroupMember> {

  private final SamAccountLdapMapper samAccountLdapMapper;

  @Getter
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public DomainGroupMemberLdapMapper() {
    samAccountLdapMapper = new SamAccountLdapMapper();
    mappedAttributes = initMappedAttributesOfDomainGroupMember();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfDomainGroupMember() {
    var attributeNames = new LinkedHashSet<>(samAccountLdapMapper.getMappedAttributes());
    attributeNames.add(AdConstants.OBJECT_CLASS);
    attributeNames.add(AdConstants.USER_GIVEN_NAME);
    attributeNames.add(AdConstants.USER_SN);
    attributeNames.add(AdConstants.USER_DISPLAY_NAME);
    attributeNames.add(AdConstants.NAME);
    return Collections.unmodifiableSet(attributeNames);
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
  public String mapDn(DomainGroupMember domainObject) {
    Assert.hasText(domainObject.getDistinguishedName(),
        "Distinguished name of domain group member is required.");
    return domainObject.getDistinguishedName();
  }

  @Override
  public DomainGroupMember map(LdapEntry source) {
    if (isEmpty(source)) {
      return null;
    }
    var builder = DomainGroupMember.builder()
        .from(samAccountLdapMapper.map(source));
    builder.memberType(Optional
        .ofNullable(source.getAttribute(AdConstants.OBJECT_CLASS.getName()))
        .map(LdapAttribute::getStringValues)
        .map(DomainGroupMemberType::fromObjectClasses)
        .orElse(DomainGroupMemberType.UNKNOWN));
    builder.displayName(getMemberDisplayName(source));
    return builder.build();
  }

  private String getMemberDisplayName(LdapEntry member) {
    return Optional.ofNullable(
            member.getAttribute(AdConstants.USER_GIVEN_NAME.getName()))
        .map(LdapAttribute::getStringValue)
        .flatMap(firstName -> Optional
            .ofNullable(member.getAttribute(AdConstants.USER_SN.getName()))
            .map(LdapAttribute::getStringValue)
            .map(lastName -> firstName + " " + lastName))
        .or(() -> Optional.ofNullable(
                member.getAttribute(AdConstants.USER_DISPLAY_NAME.getName()))
            .map(LdapAttribute::getStringValue))
        .or(() -> Optional.ofNullable(member.getAttribute(AdConstants.NAME.getName()))
            .map(LdapAttribute::getStringValue))
        .orElseGet(() -> new Dn(member.getDn()).getRDn().getNameValue().getStringValue());
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(
      DomainGroupMember source, LdapEntry destination) {
    return new AttributeModification[0];
  }

}
