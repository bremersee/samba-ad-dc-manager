/*
 * Copyright 2026 the original author or authors.
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

import static java.util.Objects.requireNonNullElseGet;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.ldaptive.LdapEntry;
import org.springframework.stereotype.Component;

/**
 * The delegating generic ldap mapper.
 *
 * @author Christian Bremner
 */
@Component
public class DelegatingGenericLdapMapper implements GenericLdapMapper {

  private final List<AdEntryLdapMapperDelegate<?>> delegates;

  private final String[] mappedAttributeNames;

  private final String[] binaryAttributeNames;

  /**
   * Instantiates a new delegating generic ldap mapper.
   *
   * @param delegates the delegates
   */
  public DelegatingGenericLdapMapper(List<AdEntryLdapMapperDelegate<?>> delegates) {
    this.delegates = requireNonNullElseGet(delegates, List::of);
    this.mappedAttributeNames = this.delegates.stream()
        .flatMap(delegate -> Arrays.stream(delegate.getBinaryAttributeNames()))
        .distinct()
        .toArray(String[]::new);
    this.binaryAttributeNames = this.delegates.stream()
        .flatMap(delegate -> Arrays.stream(delegate.getBinaryAttributeNames()))
        .distinct()
        .toArray(String[]::new);
  }

  @Override
  public String[] getMappedAttributeNames() {
    return mappedAttributeNames;
  }

  @Override
  public String[] getBinaryAttributeNames() {
    return binaryAttributeNames;
  }

  @Override
  public Optional<AdEntry> map(LdapEntry ldapEntry) {
    return delegates.stream()
        .filter(delegate -> delegate.canMap(ldapEntry))
        .findFirst()
        .map(delegate -> delegate.map(ldapEntry));
  }

}
