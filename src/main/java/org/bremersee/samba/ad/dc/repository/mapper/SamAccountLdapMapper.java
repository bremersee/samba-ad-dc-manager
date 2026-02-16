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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveEntryImmutableMapper;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.SamAccount;
import org.bremersee.samba.ad.dc.model.SamAccountIntermediate;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.springframework.util.Assert;

public class SamAccountLdapMapper extends LdaptiveEntryImmutableMapper<SamAccount> {

  private final AdEntryLdapMapper adEntryLdapMapper;

  @Getter
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public SamAccountLdapMapper() {
    adEntryLdapMapper = new AdEntryLdapMapper();
    mappedAttributes = initMappedAttributesOfSamAccount();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfSamAccount() {
    var attributeNames = new LinkedHashSet<>(adEntryLdapMapper.getMappedAttributes());
    attributeNames.add(AdConstants.SAM_ACCOUNT_NAME);
    attributeNames.add(AdConstants.OBJECT_SID);
    attributeNames.add(AdConstants.IS_CRITICAL_SYSTEM_OBJECT);
    attributeNames.add(AdConstants.PRIMARY_GROUP_ID);
    attributeNames.add(AdConstants.MEMBER_OF_GROUP);
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
  public String mapDn(SamAccount domainObject) {
    Assert.hasText(domainObject.getDistinguishedName(),
        "Distinguished name of sam account is required.");
    return domainObject.getDistinguishedName();
  }

  @Override
  public SamAccount map(LdapEntry source) {
    if (isEmpty(source)) {
      return null;
    }
    var builder = SamAccountIntermediate.builder().from(adEntryLdapMapper.map(source));
    AdConstants.SAM_ACCOUNT_NAME
        .getValue(source)
        .ifPresent(builder::samAccountName);
    AdConstants.OBJECT_SID
        .getValue(source)
        .ifPresent(builder::sid);
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT
        .getValue(source, false)
        .ifPresent(builder::criticalSystemObject);
    AdConstants.PRIMARY_GROUP_ID
        .getValue(source)
        .ifPresent(builder::primaryGroupId);
    builder.memberships(AdConstants.MEMBER_OF_GROUP
        .getValues(source)
        .map(dn -> dn.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER))
        .toList());
    return builder.build();
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(
      SamAccount source,
      LdapEntry destination) {

    if (isEmpty(source) || isEmpty(destination)) {
      return new AttributeModification[0];
    }
    var modifications = new ArrayList<>(Arrays.asList(adEntryLdapMapper
        .mapAndComputeModifications(source, destination)));

    boolean isCriticalSystemObject = AdConstants.IS_CRITICAL_SYSTEM_OBJECT
        .getValue(destination, source.isCriticalSystemObject())
        .orElse(false);
    if (!isCriticalSystemObject) {
      AdConstants.SAM_ACCOUNT_NAME
          .setValue(destination, source.getSamAccountName())
          .ifPresent(modifications::add);
      if (!isEmpty(source.getPrimaryGroupId()) && !isEmpty(source.getSid())) {
        AdConstants.PRIMARY_GROUP_ID
            .setValue(destination, source.getPrimaryGroupId())
            .ifPresent(modifications::add);
      }
    }
    return modifications.toArray(AttributeModification[]::new);
  }

}
