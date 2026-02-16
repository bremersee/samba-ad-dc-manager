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

import java.time.OffsetDateTime;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.bremersee.samba.ad.dc.model.AdEntryIntermediate;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.junit.jupiter.api.Test;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;

/**
 * The ad entry ldap mapper test.
 *
 * @author Christian Bremer
 */
class AdEntryLdapMapperTest {

  private static final AdEntryLdapMapper target = new AdEntryLdapMapper();

  /**
   * Gets object classes.
   */
  @Test
  void getObjectClasses() {
    String[] actual = target.getObjectClasses();
    assertThat(actual)
        .isEmpty();
  }

  /**
   * Gets mapped attribute names.
   */
  @Test
  void getMappedAttributeNames() {
    String[] actual = target.getMappedAttributeNames();
    assertThat(actual)
        .containsExactlyInAnyOrder(
            AdConstants.DN.getName(),
            AdConstants.WHEN_CREATED.getName(),
            AdConstants.WHEN_CHANGED.getName()
        );
  }

  /**
   * Gets binary attribute names.
   */
  @Test
  void getBinaryAttributeNames() {
    String[] actual = target.getBinaryAttributeNames();
    assertThat(actual)
        .isEmpty();
  }

  /**
   * Map dn.
   */
  @Test
  void mapDn() {
    String expected = "DC=samdom,DC=example,DC=org";
    AdEntry entry = AdEntryIntermediate.builder()
        .distinguishedName(expected)
        .build();
    String actual = target.mapDn(entry);
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Map.
   */
  @Test
  void map() {
    LdapEntry source = new LdapEntry();
    source.setDn("DC=samdom,DC=example,DC=org");
    OffsetDateTime dateTime = OffsetDateTime.parse("2026-02-15T22:51:45Z");
    AdConstants.WHEN_CREATED.setValue(source, dateTime.minusMinutes(1L));
    AdConstants.WHEN_CHANGED.setValue(source, dateTime);

    AdEntry expected = AdEntryIntermediate.builder()
        .distinguishedName(source.getDn())
        .created(dateTime.minusMinutes(1L))
        .modified(dateTime)
        .build();

    AdEntry actual = target.map(source);

    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Map null.
   */
  @Test
  void mapNull() {
    assertThat(target.map(null))
        .isNull();
  }

  /**
   * Map and compute modifications.
   */
  @Test
  void mapAndComputeModifications() {
    OffsetDateTime dateTime = OffsetDateTime.parse("2026-02-15T22:51:45Z");

    AdEntry source = AdEntryIntermediate.builder()
        .distinguishedName("DC=samdom,DC=example,DC=org")
        .created(dateTime.minusMinutes(1L))
        .modified(dateTime)
        .build();
    LdapEntry destination = new LdapEntry();
    destination.setDn(source.getDistinguishedName());

    AttributeModification[] actual = target.mapAndComputeModifications(source, destination);
    assertThat(actual)
        .isEmpty();
  }

}