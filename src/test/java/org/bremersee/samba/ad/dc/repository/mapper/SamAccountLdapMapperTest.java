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
import static org.mockito.Mockito.mock;

import java.time.OffsetDateTime;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.bremersee.samba.ad.dc.model.AdEntryIntermediate;
import org.bremersee.samba.ad.dc.model.SamAccount;
import org.bremersee.samba.ad.dc.model.SamAccountIntermediate;
import org.bremersee.samba.ad.dc.model.Sid;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.junit.jupiter.api.Test;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.ldaptive.dn.Dn;

/**
 * The sam account ldap mapper test.
 *
 * @author Christian Bremer
 */
class SamAccountLdapMapperTest {

  private static final SamAccountLdapMapper target = new SamAccountLdapMapper();

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
            AdConstants.OBJECT_CLASS.getName(),
            AdConstants.DN.getName(),
            AdConstants.WHEN_CREATED.getName(),
            AdConstants.WHEN_CHANGED.getName(),
            AdConstants.SAM_ACCOUNT_NAME.getName(),
            AdConstants.OBJECT_SID.getName(),
            AdConstants.IS_CRITICAL_SYSTEM_OBJECT.getName(),
            AdConstants.PRIMARY_GROUP_ID.getName(),
            AdConstants.MEMBER_OF_GROUP.getName()
        );
  }

  /**
   * Gets binary attribute names.
   */
  @Test
  void getBinaryAttributeNames() {
    String[] actual = target.getBinaryAttributeNames();
    assertThat(actual)
        .containsExactlyInAnyOrder(
            AdConstants.OBJECT_SID.getName()
        );
  }

  /**
   * Map dn.
   */
  @Test
  void mapDn() {
    String expected = "DC=samdom,DC=example,DC=org";
    SamAccount entry = SamAccountIntermediate.builder()
        .distinguishedName(expected)
        .samAccountName("eva")
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
    source.setDn("CN=Administrator,CN=Users,DC=samdom,DC=example,DC=org");
    OffsetDateTime dateTime = OffsetDateTime.parse("2026-02-15T22:51:45Z");
    AdConstants.WHEN_CREATED.setValue(source, dateTime.minusMinutes(1L));
    AdConstants.WHEN_CHANGED.setValue(source, dateTime);
    AdConstants.SAM_ACCOUNT_NAME.setValue(source, "Administrator");
    AdConstants.OBJECT_SID.setValue(source, Sid.builder()
        .value("S-1-5-21-1111111111-111111111-1111111111-500")
        .build());
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(source, true);
    AdConstants.PRIMARY_GROUP_ID.setValue(source, 100);
    AdConstants.MEMBER_OF_GROUP
        .setValue(source, new Dn("CN=Domain Users,CN=Users,DC=samdom,DC=example,DC=org"));

    AdEntry adEntry = AdEntryIntermediate.builder()
        .distinguishedName(source.getDn())
        .created(dateTime.minusMinutes(1L))
        .modified(dateTime)
        .build();
    SamAccount expected = SamAccountIntermediate.builder()
        .from(adEntry)
        .samAccountName("Administrator")
        .sid(Sid.builder()
            .value("S-1-5-21-1111111111-111111111-1111111111-500")
            .build())
        .criticalSystemObject(true)
        .primaryGroupId(100)
        .addMembership("CN=Domain Users,CN=Users,DC=samdom,DC=example,DC=org")
        .build();

    SamAccount actual = target.map(source);

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
    destination.setDn("CN=Administrator,CN=Users,DC=samdom,DC=example,DC=org");
    OffsetDateTime dateTime = OffsetDateTime.parse("2026-02-15T22:51:45Z");
    AdConstants.WHEN_CREATED.setValue(destination, dateTime.minusMinutes(1L));
    AdConstants.WHEN_CHANGED.setValue(destination, dateTime);
    AdConstants.SAM_ACCOUNT_NAME.setValue(destination, "Administrator");
    AdConstants.OBJECT_SID.setValue(destination, Sid.builder()
        .value("S-1-5-21-1111111111-111111111-1111111111-500")
        .build());
    AdConstants.PRIMARY_GROUP_ID.setValue(destination, 100);
    AdConstants.MEMBER_OF_GROUP
        .setValue(destination, new Dn("CN=Domain Users,CN=Users,DC=samdom,DC=example,DC=org"));

    AdEntry adEntry = AdEntryIntermediate.builder()
        .distinguishedName(destination.getDn())
        .created(dateTime.minusMinutes(1L))
        .modified(dateTime)
        .build();
    SamAccount source = SamAccountIntermediate.builder()
        .from(adEntry)
        .samAccountName("Administrator")
        .sid(Sid.builder()
            .value("S-1-5-21-1111111111-111111111-1111111111-500")
            .build())
        .primaryGroupId(200)
        .addMembership("CN=Domain Users,CN=Users,DC=samdom,DC=example,DC=org")
        .build();

    AttributeModification[] actual = target.mapAndComputeModifications(source, destination);
    assertThat(actual)
        .hasSize(1);
  }

  /**
   * Map and compute modifications with critical system object.
   */
  @Test
  void mapAndComputeModificationsWithCriticalSystemObject() {
    LdapEntry destination = new LdapEntry();
    destination.setDn("CN=Administrator,CN=Users,DC=samdom,DC=example,DC=org");
    OffsetDateTime dateTime = OffsetDateTime.parse("2026-02-15T22:51:45Z");
    AdConstants.WHEN_CREATED.setValue(destination, dateTime.minusMinutes(1L));
    AdConstants.WHEN_CHANGED.setValue(destination, dateTime);
    AdConstants.SAM_ACCOUNT_NAME.setValue(destination, "Administrator");
    AdConstants.OBJECT_SID.setValue(destination, Sid.builder()
        .value("S-1-5-21-1111111111-111111111-1111111111-500")
        .build());
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(destination, true);
    AdConstants.PRIMARY_GROUP_ID.setValue(destination, 100);
    AdConstants.MEMBER_OF_GROUP
        .setValue(destination, new Dn("CN=Domain Users,CN=Users,DC=samdom,DC=example,DC=org"));

    AdEntry adEntry = AdEntryIntermediate.builder()
        .distinguishedName(destination.getDn())
        .created(dateTime.minusMinutes(1L))
        .modified(dateTime)
        .build();
    SamAccount source = SamAccountIntermediate.builder()
        .from(adEntry)
        .samAccountName("Administrator")
        .sid(Sid.builder()
            .value("S-1-5-21-1111111111-111111111-1111111111-500")
            .build())
        .primaryGroupId(200)
        .addMembership("CN=Domain Users,CN=Users,DC=samdom,DC=example,DC=org")
        .build();

    AttributeModification[] actual = target.mapAndComputeModifications(source, destination);
    assertThat(actual)
        .isEmpty();
  }

  /**
   * Map and compute modifications with no source.
   */
  @Test
  void mapAndComputeModificationsWithNoSource() {
    AttributeModification[] actual = target.mapAndComputeModifications(null, new LdapEntry());
    assertThat(actual)
        .isEmpty();
  }

  /**
   * Map and compute modifications with no destination.
   */
  @Test
  void mapAndComputeModificationsWithNoDestination() {
    AttributeModification[] actual = target.mapAndComputeModifications(mock(SamAccount.class),
        null);
    assertThat(actual)
        .isEmpty();
  }
}