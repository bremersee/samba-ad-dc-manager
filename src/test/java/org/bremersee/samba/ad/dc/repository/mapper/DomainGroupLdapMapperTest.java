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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import java.time.OffsetDateTime;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.bremersee.samba.ad.dc.model.AdEntryIntermediate;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainGroupType;
import org.bremersee.samba.ad.dc.model.SamAccount;
import org.bremersee.samba.ad.dc.model.SamAccountIntermediate;
import org.bremersee.samba.ad.dc.model.Sid;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.bremersee.samba.ad.dc.repository.DomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.ldaptive.dn.Dn;

/**
 * The domain group ldap mapper test.
 *
 * @author Christian Bremer
 */
class DomainGroupLdapMapperTest {

  private DomainRepository domainRepository;

  private DomainGroupLdapMapper target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    domainRepository = mock(DomainRepository.class);
    lenient()
        .doReturn(true)
        .when(domainRepository)
        .isRfc2307Enabled();
    target = new DomainGroupLdapMapper(domainRepository);
  }

  /**
   * Gets object classes.
   */
  @Test
  void getObjectClasses() {
    String[] actual = target.getObjectClasses();
    assertThat(actual)
        .containsExactlyInAnyOrder(
            AdConstants.OBJECT_CLASS_GROUP,
            "top"
        );
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
            AdConstants.MEMBER_OF_GROUP.getName(),
            AdConstants.GROUP_TYPE.getName(),
            AdConstants.DESCRIPTION.getName(),
            AdConstants.GID_NUMBER.getName(),
            AdConstants.MAIL.getName(),
            AdConstants.GROUP_MEMBER.getName(),
            AdConstants.NIS_DOMAIN.getName()
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
    DomainGroup entry = DomainGroup.builder()
        .distinguishedName(expected)
        .samAccountName("contacts")
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
    source.setDn("CN=Contacts,CN=Users,DC=samdom,DC=example,DC=org");
    AdConstants.OBJECT_CLASS.setValue(source, AdConstants.OBJECT_CLASS_GROUP);
    OffsetDateTime dateTime = OffsetDateTime.parse("2026-02-15T22:51:45Z");
    AdConstants.WHEN_CREATED.setValue(source, dateTime.minusMinutes(1L));
    AdConstants.WHEN_CHANGED.setValue(source, dateTime);
    AdConstants.SAM_ACCOUNT_NAME.setValue(source, "Contacts");
    AdConstants.OBJECT_SID.setValue(source, Sid.builder()
        .value("S-1-5-21-1111111111-111111111-1111111111-1080")
        .build());
    AdConstants.GROUP_TYPE.setValue(source, DomainGroupType.defaultGroupType().getValue());
    AdConstants.DESCRIPTION.setValue(source, "Private");
    AdConstants.GID_NUMBER.setValue(source, 20000);
    AdConstants.MAIL.setValue(source, "contacts@example.org");
    AdConstants.GROUP_MEMBER
        .setValue(source, new Dn("CN=junit,CN=Users,DC=samdom,DC=example,DC=org"));
    AdConstants.NIS_DOMAIN.setValue(source, "samdom");

    AdEntry adEntry = AdEntryIntermediate.builder()
        .distinguishedName(source.getDn())
        .created(dateTime.minusMinutes(1L))
        .modified(dateTime)
        .build();
    SamAccount samAccount = SamAccountIntermediate.builder()
        .from(adEntry)
        .samAccountName("Contacts")
        .sid(Sid.builder()
            .value("S-1-5-21-1111111111-111111111-1111111111-1080")
            .build())
        .build();
    DomainGroup expected = DomainGroup.builder()
        .from(samAccount)
        .groupType(DomainGroupType.defaultGroupType())
        .description("Private")
        .gidNumber(20000)
        .email("contacts@example.org")
        .addMember("CN=junit,CN=Users,DC=samdom,DC=example,DC=org")
        .nisDomain("samdom")
        .build();

    DomainGroup actual = target.map(source);

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
    destination.setDn("CN=Contacts,CN=Users,DC=samdom,DC=example,DC=org");
    OffsetDateTime dateTime = OffsetDateTime.parse("2026-02-15T22:51:45Z");
    AdConstants.WHEN_CREATED.setValue(destination, dateTime.minusMinutes(1L));
    AdConstants.WHEN_CHANGED.setValue(destination, dateTime);
    AdConstants.SAM_ACCOUNT_NAME.setValue(destination, "Contacts");
    AdConstants.OBJECT_SID.setValue(destination, Sid.builder()
        .value("S-1-5-21-1111111111-111111111-1111111111-1080")
        .build());
    AdConstants.GROUP_TYPE.setValue(destination, DomainGroupType.defaultGroupType().getValue());
    AdConstants.DESCRIPTION.setValue(destination, "Private");
    AdConstants.GID_NUMBER.setValue(destination, 20000);
    AdConstants.MAIL.setValue(destination, "contacts@example.org");
    AdConstants.GROUP_MEMBER
        .setValue(destination, new Dn("CN=junit,CN=Users,DC=samdom,DC=example,DC=org"));
    AdConstants.NIS_DOMAIN.setValue(destination, "samdom");

    AdEntry adEntry = AdEntryIntermediate.builder()
        .distinguishedName(destination.getDn())
        .created(dateTime.minusMinutes(1L))
        .modified(dateTime)
        .build();
    SamAccount samAccount = SamAccountIntermediate.builder()
        .from(adEntry)
        .samAccountName("Contacts")
        .sid(Sid.builder()
            .value("S-1-5-21-1111111111-111111111-1111111111-1080")
            .build())
        .primaryGroupId(200)
        .build();
    DomainGroup source = DomainGroup.builder()
        .from(samAccount)
        .groupType(DomainGroupType.defaultGroupType())
        .description("Private and business")
        .gidNumber(20000)
        .email("contacts@example.org")
        .addMember("CN=junit,CN=Users,DC=samdom,DC=example,DC=org")
        .addMember("CN=chef,CN=Users,DC=samdom,DC=example,DC=org")
        .nisDomain("samdom")
        .build();

    AttributeModification[] actual = target.mapAndComputeModifications(source, destination);
    assertThat(actual)
        .hasSize(3); // nis name is set to samAccount
  }

  /**
   * Map and compute modifications with no rfc 2307.
   */
  @Test
  void mapAndComputeModificationsWithNoRfc2307() {
    reset(domainRepository);
    doReturn(false)
        .when(domainRepository)
        .isRfc2307Enabled();
    LdapEntry destination = new LdapEntry();
    destination.setDn("CN=Contacts,CN=Users,DC=samdom,DC=example,DC=org");
    OffsetDateTime dateTime = OffsetDateTime.parse("2026-02-15T22:51:45Z");
    AdConstants.WHEN_CREATED.setValue(destination, dateTime.minusMinutes(1L));
    AdConstants.WHEN_CHANGED.setValue(destination, dateTime);
    AdConstants.SAM_ACCOUNT_NAME.setValue(destination, "Contacts");
    AdConstants.OBJECT_SID.setValue(destination, Sid.builder()
        .value("S-1-5-21-1111111111-111111111-1111111111-1080")
        .build());
    AdConstants.GROUP_TYPE.setValue(destination, DomainGroupType.defaultGroupType().getValue());
    AdConstants.DESCRIPTION.setValue(destination, "Private");
    AdConstants.MAIL.setValue(destination, "contacts@example.org");
    AdConstants.GROUP_MEMBER
        .setValue(destination, new Dn("CN=junit,CN=Users,DC=samdom,DC=example,DC=org"));

    AdEntry adEntry = AdEntryIntermediate.builder()
        .distinguishedName(destination.getDn())
        .created(dateTime.minusMinutes(1L))
        .modified(dateTime)
        .build();
    SamAccount samAccount = SamAccountIntermediate.builder()
        .from(adEntry)
        .samAccountName("Contacts")
        .sid(Sid.builder()
            .value("S-1-5-21-1111111111-111111111-1111111111-1080")
            .build())
        .primaryGroupId(200)
        .build();
    DomainGroup source = DomainGroup.builder()
        .from(samAccount)
        .groupType(DomainGroupType.defaultGroupType())
        .description("Private and business")
        .gidNumber(20000)
        .email("contacts@example.org")
        .addMember("CN=junit,CN=Users,DC=samdom,DC=example,DC=org")
        .addMember("CN=chef,CN=Users,DC=samdom,DC=example,DC=org")
        .nisDomain("samdom")
        .build();

    AttributeModification[] actual = target.mapAndComputeModifications(source, destination);
    assertThat(actual)
        .hasSize(2);
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
    AttributeModification[] actual = target.mapAndComputeModifications(mock(DomainGroup.class),
        null);
    assertThat(actual)
        .isEmpty();
  }

  /**
   * Can map.
   */
  @Test
  void canMap() {
    LdapEntry entry = new LdapEntry();
    AdConstants.OBJECT_CLASS.setValue(entry, AdConstants.OBJECT_CLASS_GROUP);

    boolean actual = target.canMap(entry);
    assertThat(actual)
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

  /**
   * Can not map null.
   */
  @Test
  void canNotMapNull() {
    boolean actual = target.canMap(null);
    assertThat(actual)
        .isFalse();
  }

}