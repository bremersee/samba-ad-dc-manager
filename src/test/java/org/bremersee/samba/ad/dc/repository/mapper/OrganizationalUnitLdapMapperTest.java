/*
 * Copyright 2024-2026 the original author or authors.
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
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;

/**
 * The organizational unit ldap mapper test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class OrganizationalUnitLdapMapperTest {

  private OrganizationalUnitLdapMapper target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    properties.setBaseDn("dc=samdom,dc=example,dc=org");
    target = new OrganizationalUnitLdapMapper(properties);
  }

  /**
   * Gets object classes.
   */
  @Test
  void getObjectClasses() {
    String[] expected = new String[]{
        AdConstants.OBJECT_CLASS_OU,
        "top"
    };
    String[] actual = target.getObjectClasses();
    assertThat(actual).containsExactlyInAnyOrder(expected);
  }

  /**
   * Gets mapped attribute names.
   */
  @Test
  void getMappedAttributeNames() {
    String[] actual = target.getMappedAttributeNames();
    assertThat(actual)
        .containsExactlyInAnyOrder(
            AdConstants.OBJECT_CLASS.getName(),
            AdConstants.DN.getName(),
            AdConstants.WHEN_CREATED.getName(),
            AdConstants.WHEN_CHANGED.getName(),
            AdConstants.DESCRIPTION.getName(),
            AdConstants.IS_CRITICAL_SYSTEM_OBJECT.getName(),
            AdConstants.NAME.getName()
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
    OrganizationalUnit entry = OrganizationalUnit.builder()
        .distinguishedName(expected)
        .name("ou")
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
    AdConstants.OBJECT_CLASS.setValue(source, AdConstants.OBJECT_CLASS_OU);
    AdConstants.DESCRIPTION.setValue(source, "description");
    AdConstants.NAME.setValue(source, "name");

    OrganizationalUnit expected = OrganizationalUnit.builder()
        .distinguishedName(source.getDn())
        .created(dateTime.minusMinutes(1L))
        .modified(dateTime)
        .description("description")
        .name("name")
        .build();

    OrganizationalUnit actual = target.map(source);

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
    LdapEntry destination = new LdapEntry();
    destination.setDn("DC=samdom,DC=example,DC=org");
    OffsetDateTime dateTime = OffsetDateTime.parse("2026-02-15T22:51:45Z");
    AdConstants.WHEN_CREATED.setValue(destination, dateTime.minusMinutes(1L));
    AdConstants.WHEN_CHANGED.setValue(destination, dateTime);
    AdConstants.OBJECT_CLASS.setValue(destination, AdConstants.OBJECT_CLASS_OU);
    AdConstants.DESCRIPTION.setValue(destination, "description");
    AdConstants.NAME.setValue(destination, "name");

    OrganizationalUnit source = OrganizationalUnit.builder()
        .distinguishedName(destination.getDn())
        .created(dateTime.minusMinutes(1L))
        .modified(dateTime)
        .description("new description")
        .name("name")
        .build();

    AttributeModification[] actual = target.mapAndComputeModifications(source, destination);
    assertThat(actual)
        .hasSize(1);
  }

  /**
   * Map and compute modifications with null.
   */
  @Test
  void mapAndComputeModificationsWithNull() {
    AttributeModification[] actual = target.mapAndComputeModifications(null, null);
    assertThat(actual)
        .isEmpty();
  }

  /**
   * Can map.
   *
   * @param softly the softly
   */
  @Test
  void canMap(SoftAssertions softly) {
    LdapEntry entry = new LdapEntry();
    AdConstants.OBJECT_CLASS.setValue(entry, AdConstants.OBJECT_CLASS_OU);
    boolean actual = target.canMap(entry);
    softly
        .assertThat(actual)
        .isTrue();

    AdConstants.OBJECT_CLASS.setValue(entry, AdConstants.OBJECT_CLASS_CONTAINER);
    entry.setDn("CN=Users,DC=samdom,DC=example,DC=org");
    actual = target.canMap(entry);
    softly
        .assertThat(actual)
        .isTrue();

    entry.setDn("CN=Computers,DC=samdom,DC=example,DC=org");
    actual = target.canMap(entry);
    softly
        .assertThat(actual)
        .isTrue();
  }

  /**
   * Can not map.
   */
  @Test
  void canNotMap() {
    LdapEntry entry = new LdapEntry();
    AdConstants.OBJECT_CLASS.setValue(entry, AdConstants.OBJECT_CLASS_USER);

    boolean actual = target.canMap(entry);
    assertThat(actual)
        .isFalse();
  }

}