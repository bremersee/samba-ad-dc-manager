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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ldaptive.LdapEntry;

/**
 * The delegating generic ldap mapper test.
 *
 * @author Christian Bremer
 */
class DelegatingGenericLdapMapperTest {

  private AdEntryLdapMapperDelegate<AdEntry> canNotMapDelegate;

  private AdEntryLdapMapperDelegate<AdEntry> canMapDelegate;

  private DelegatingGenericLdapMapper target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    //noinspection unchecked
    canNotMapDelegate = mock(AdEntryLdapMapperDelegate.class);
    lenient()
        .doReturn(false)
        .when(canNotMapDelegate)
        .canMap(any());
    lenient()
        .doReturn(new String[0])
        .when(canNotMapDelegate)
        .getMappedAttributeNames();
    lenient()
        .doReturn(new String[0])
        .when(canNotMapDelegate)
        .getBinaryAttributeNames();
    //noinspection unchecked
    canMapDelegate = mock(AdEntryLdapMapperDelegate.class);
    lenient()
        .doReturn(true)
        .when(canMapDelegate)
        .canMap(any());
    lenient()
        .doReturn(new String[]{AdConstants.OBJECT_CLASS.getName()})
        .when(canMapDelegate)
        .getMappedAttributeNames();
    lenient()
        .doReturn(new String[]{AdConstants.OBJECT_SID.getName()})
        .when(canMapDelegate)
        .getBinaryAttributeNames();
    target = new DelegatingGenericLdapMapper(List.of(canNotMapDelegate, canMapDelegate));
  }

  /**
   * Gets mapped attribute names.
   */
  @Test
  void getMappedAttributeNames() {
    String[] actual = target.getMappedAttributeNames();
    assertThat(actual)
        .containsExactly(AdConstants.OBJECT_CLASS.getName());
  }

  /**
   * Gets binary attribute names.
   */
  @Test
  void getBinaryAttributeNames() {
    String[] actual = target.getBinaryAttributeNames();
    assertThat(actual)
        .containsExactly(AdConstants.OBJECT_SID.getName());
  }

  /**
   * Map.
   */
  @Test
  void map() {
    LdapEntry entry = mock(LdapEntry.class);
    AdEntry adEntry = mock(AdEntry.class);
    doReturn(adEntry)
        .when(canMapDelegate)
        .map(entry);
    Optional<AdEntry> actual = target.map(entry);
    assertThat(actual)
        .hasValue(adEntry);
    verify(canNotMapDelegate, never()).map(any());
  }
}