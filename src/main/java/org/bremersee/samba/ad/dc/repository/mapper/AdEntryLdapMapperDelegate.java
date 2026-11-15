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

import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.ldaptive.LdapEntry;

/**
 * The interface ad entry ldap mapper delegate.
 *
 * @param <T> the type parameter
 * @author Christian Bremer
 */
public interface AdEntryLdapMapperDelegate<T extends AdEntry> extends LdaptiveEntryMapper<T> {

  /**
   * Determines whether this mapper can map the given ldap entry or not.
   *
   * @param ldapEntry the ldap entry
   * @return {@code true} if the map can map the ldap entry, otherwise {@code false}
   */
  boolean canMap(LdapEntry ldapEntry);

}
