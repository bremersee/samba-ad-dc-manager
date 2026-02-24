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
import org.bremersee.ldaptive.transcoder.UserAccountControl;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.bremersee.samba.ad.dc.model.AdEntryIntermediate;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.DomainUserAccountControl;
import org.bremersee.samba.ad.dc.model.SamAccount;
import org.bremersee.samba.ad.dc.model.SamAccountIntermediate;
import org.bremersee.samba.ad.dc.model.Sid;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.bremersee.samba.ad.dc.repository.DomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;

/**
 * The domain user ldap mapper test.
 *
 * @author Christian Bremer
 */
class DomainUserLdapMapperTest {

  private static final OffsetDateTime DATE_TIME = OffsetDateTime.parse("2026-02-15T22:51:45Z");

  private DomainRepository domainRepository;

  private DomainUserLdapMapper target;

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
    target = new DomainUserLdapMapper(domainRepository);
  }

  /**
   * Gets object classes.
   */
  @Test
  void getObjectClasses() {
    String[] actual = target.getObjectClasses();
    assertThat(actual)
        .containsExactlyInAnyOrder(
            "organizationalPerson",
            "person",
            "top",
            AdConstants.OBJECT_CLASS_USER
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
            AdConstants.USER_ACCOUNT_EXPIRES.getName(),
            AdConstants.USER_COMPANY.getName(),
            AdConstants.USER_DEPARTMENT.getName(),
            AdConstants.DESCRIPTION.getName(),
            AdConstants.USER_DISPLAY_NAME.getName(),
            AdConstants.USER_GECOS.getName(),
            AdConstants.GID_NUMBER.getName(),
            AdConstants.USER_GIVEN_NAME.getName(),
            AdConstants.USER_HOME_DIRECTORY.getName(),
            AdConstants.USER_HOME_DRIVE.getName(),
            AdConstants.USER_INITIALS.getName(),
            AdConstants.USER_LAST_LOGON.getName(),
            AdConstants.USER_LOGIN_SHELL.getName(),
            AdConstants.USER_LOGON_COUNT.getName(),
            AdConstants.MAIL.getName(),
            AdConstants.USER_MOBILE.getName(),
            AdConstants.NIS_DOMAIN.getName(),
            AdConstants.USER_OFFICE_NAME.getName(),
            AdConstants.USER_PREFERRED_LANGUAGE.getName(),
            AdConstants.USER_PROFILE_PATH.getName(),
            AdConstants.USER_PWD_LAST_SET.getName(),
            AdConstants.USER_SCRIPT_PATH.getName(),
            AdConstants.USER_SN.getName(),
            AdConstants.USER_TELEPHONE_NUMBER.getName(),
            AdConstants.USER_TITLE.getName(),
            AdConstants.USER_UID.getName(),
            AdConstants.USER_UID_NUMBER.getName(),
            AdConstants.NIS_NAME.getName(),
            AdConstants.USER_UNIX_HOME_DIRECTORY.getName(),
            AdConstants.USER_PRINCIPAL_NAME.getName(),
            AdConstants.USER_USER_ACCOUNT_CONTROL.getName()
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
    DomainUser entry = DomainUser.builder()
        .distinguishedName(expected)
        .samAccountName("user")
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
    LdapEntry source = createLdapEntry();
    DomainUser expected = createDomainUser();
    DomainUser actual = target.map(source);
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
    LdapEntry destination = createLdapEntry();
    DomainUser source = DomainUser.builder()
        .from(createDomainUser())
        .description("New description")
        .uid("unix")
        .build();
    AttributeModification[] actual = target.mapAndComputeModifications(source, destination);
    assertThat(actual)
        .hasSize(3); // nis name is set to uid
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
    LdapEntry destination = createLdapEntry();
    DomainUser source = DomainUser.builder()
        .from(createDomainUser())
        .description("New description")
        .uid("unix")
        .build();
    AttributeModification[] actual = target.mapAndComputeModifications(source, destination);
    assertThat(actual)
        .hasSize(1);
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
   * Can map.
   */
  @Test
  void canMap() {
    LdapEntry entry = new LdapEntry();
    AdConstants.OBJECT_CLASS.setValue(entry, AdConstants.OBJECT_CLASS_USER);

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
    AdConstants.OBJECT_CLASS.setValue(entry, AdConstants.OBJECT_CLASS_GROUP);

    boolean actual = target.canMap(entry);
    assertThat(actual)
        .isFalse();
  }

  private static LdapEntry createLdapEntry() {
    LdapEntry entry = new LdapEntry();
    entry.setDn("CN=User,CN=Users,DC=samdom,DC=example,DC=org");
    AdConstants.OBJECT_CLASS.setValue(entry, AdConstants.OBJECT_CLASS_USER);
    AdConstants.WHEN_CREATED.setValue(entry, DATE_TIME.minusMinutes(1L));
    AdConstants.WHEN_CHANGED.setValue(entry, DATE_TIME);
    AdConstants.SAM_ACCOUNT_NAME.setValue(entry, "user");
    AdConstants.OBJECT_SID.setValue(entry, Sid.builder()
        .value("S-1-5-21-1111111111-111111111-1111111111-1080")
        .build());

    AdConstants.USER_ACCOUNT_EXPIRES.setValue(entry, DATE_TIME.plusDays(1L));
    AdConstants.USER_COMPANY.setValue(entry, "company");
    AdConstants.USER_DEPARTMENT.setValue(entry, "department");
    AdConstants.DESCRIPTION.setValue(entry, "Private");
    AdConstants.USER_DISPLAY_NAME.setValue(entry, "JUnit User");
    AdConstants.USER_GECOS.setValue(entry, "gecos");
    AdConstants.GID_NUMBER.setValue(entry, 20000);
    AdConstants.USER_GIVEN_NAME.setValue(entry, "JUnit");
    AdConstants.USER_HOME_DIRECTORY.setValue(entry, "homeDirectory");
    AdConstants.USER_HOME_DRIVE.setValue(entry, "H");
    AdConstants.USER_INITIALS.setValue(entry, "initials");
    AdConstants.USER_LAST_LOGON.setValue(entry, DATE_TIME.minusMinutes(1L));
    AdConstants.USER_LOGIN_SHELL.setValue(entry, "/bin/bash");
    AdConstants.USER_LOGON_COUNT.setValue(entry, 2);
    AdConstants.MAIL.setValue(entry, "user@example.org");
    AdConstants.USER_MOBILE.setValue(entry, "mobile");
    AdConstants.NIS_DOMAIN.setValue(entry, "samdom");
    AdConstants.USER_OFFICE_NAME.setValue(entry, "officeName");
    AdConstants.USER_PREFERRED_LANGUAGE.setValue(entry, "en");
    AdConstants.USER_PROFILE_PATH.setValue(entry, "profilePath");
    AdConstants.USER_PWD_LAST_SET.setValue(entry, DATE_TIME.minusMinutes(2L));
    AdConstants.USER_SCRIPT_PATH.setValue(entry, "scriptPath");
    AdConstants.USER_SN.setValue(entry, "User");
    AdConstants.USER_TELEPHONE_NUMBER.setValue(entry, "telephoneNumber");
    AdConstants.USER_TITLE.setValue(entry, "title");
    AdConstants.USER_UID.setValue(entry, "uid");
    AdConstants.USER_UID_NUMBER.setValue(entry, 123);
    AdConstants.USER_UNIX_HOME_DIRECTORY.setValue(entry, "homeDirectory");
    AdConstants.USER_PRINCIPAL_NAME.setValue(entry, "userPrincipalName");
    AdConstants.USER_USER_ACCOUNT_CONTROL.setValue(entry, new UserAccountControl());
    return entry;
  }

  private static DomainUser createDomainUser() {
    AdEntry adEntry = AdEntryIntermediate.builder()
        .distinguishedName("CN=User,CN=Users,DC=samdom,DC=example,DC=org")
        .created(DATE_TIME.minusMinutes(1L))
        .modified(DATE_TIME)
        .build();
    SamAccount samAccount = SamAccountIntermediate.builder()
        .from(adEntry)
        .samAccountName("user")
        .sid(Sid.builder()
            .value("S-1-5-21-1111111111-111111111-1111111111-1080")
            .build())
        .build();
    return DomainUser.builder()
        .from(samAccount)
        .accountExpires(DATE_TIME.plusDays(1L))
        .company("company")
        .department("department")
        .description("Private")
        .displayName("JUnit User")
        .gecos("gecos")
        .gidNumber(20000)
        .firstName("JUnit")
        .homeDirectory("homeDirectory")
        .homeDrive("H")
        .initials("initials")
        .lastLogon(DATE_TIME.minusMinutes(1L))
        .loginShell("/bin/bash")
        .logonCount(2)
        .email("user@example.org")
        .mobile("mobile")
        .nisDomain("samdom")
        .physicalDeliveryOfficeName("officeName")
        .preferredLanguage("en")
        .profilePath("profilePath")
        .passwordLastSet(DATE_TIME.minusMinutes(2L))
        .scriptPath("scriptPath")
        .lastName("User")
        .telephoneNumber("telephoneNumber")
        .title("title")
        .uid("uid")
        .uidNumber(123)
        .unixHomeDirectory("homeDirectory")
        .userPrincipalName("userPrincipalName")
        .accountControl(DomainUserAccountControl.defaultAccountControl())
        .build();
  }

}