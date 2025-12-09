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
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.springframework.util.Assert;

/**
 * The abstract ldap mapper.
 *
 * @author Christian Bremer
 */
public class AdEntryLdapMapper<T extends AdEntry>
    implements LdaptiveEntryMapper<T> {

  @Getter(AccessLevel.PROTECTED)
  private final Supplier<T> destinationSupplier;

  @Getter(AccessLevel.PROTECTED)
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public AdEntryLdapMapper(Supplier<T> destinationSupplier) {
    this.destinationSupplier = destinationSupplier;
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
  public String mapDn(T domainObject) {
    Assert.hasText(domainObject.getDistinguishedName(), "DN of ldap entry is required.");
    return domainObject.getDistinguishedName();
  }

  @Override
  public T map(LdapEntry ldapEntry) {
    if (isEmpty(ldapEntry)) {
      return null;
    }
    T destination = getDestinationSupplier().get();
    map(ldapEntry, destination);
    return destination;
  }

  @Override
  public void map(LdapEntry source, T destination) {
    if (isNull(source) || isNull(destination)) {
      return;
    }
    destination.setDistinguishedName(source.getDn());
    AdConstants.WHEN_CREATED
        .getValue(source)
        .consume(destination::setCreated);
    AdConstants.WHEN_CHANGED
        .getValue(source)
        .consume(destination::setModified);
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(T source, LdapEntry destination) {
    return new AttributeModification[0];
  }

  protected List<AttributeModification> toNewList(AttributeModification[] modifications) {
    if (isEmpty(modifications)) {
      return new ArrayList<>();
    }
    return new ArrayList<>(Arrays.asList(modifications));
  }

}
