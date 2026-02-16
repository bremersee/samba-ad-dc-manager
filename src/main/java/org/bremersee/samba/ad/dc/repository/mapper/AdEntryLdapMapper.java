/*
 * Copyright 2019-2026 the original author or authors.
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

import java.util.Set;
import lombok.Getter;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveEntryImmutableMapper;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.bremersee.samba.ad.dc.model.AdEntryIntermediate;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.springframework.util.Assert;

/**
 * The base active directory ldap entry mapper.
 *
 * @author Christian Bremer
 */
public class AdEntryLdapMapper extends LdaptiveEntryImmutableMapper<AdEntry> {

  /**
   * --- GETTER ---
   * Gets the unmodifiable set of mapped attributes.
   *
   * @return the unmodifiable set of mapped attributes
   */
  @Getter
  @SuppressWarnings("JavadocDeclaration")
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  /**
   * Instantiates a new base active directory ldap entry mapper.
   */
  public AdEntryLdapMapper() {
    mappedAttributes = Set.of(
        AdConstants.DN,
        AdConstants.WHEN_CREATED,
        AdConstants.WHEN_CHANGED
    );
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
  public String mapDn(AdEntry domainObject) {
    Assert.hasText(domainObject.getDistinguishedName(),
        "Distinguished name of ad entry is required.");
    return domainObject.getDistinguishedName();
  }

  @Override
  public AdEntry map(LdapEntry source) {
    if (isEmpty(source)) {
      return null;
    }
    var builder = AdEntryIntermediate.builder();
    builder.distinguishedName(source.getDn());
    AdConstants.WHEN_CREATED
        .getValue(source)
        .ifPresent(builder::created);
    AdConstants.WHEN_CHANGED
        .getValue(source)
        .ifPresent(builder::modified);
    return builder.build();
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(AdEntry source, LdapEntry destination) {
    return new AttributeModification[0];
  }

}
