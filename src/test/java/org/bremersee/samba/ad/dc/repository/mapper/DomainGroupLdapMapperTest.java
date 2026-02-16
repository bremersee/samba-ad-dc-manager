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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.time.Month;
import java.time.OffsetDateTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.bremersee.samba.ad.dc.repository.DomainRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.mockito.Mockito;

/**
 * The domain group ldap mapper test.
 *
 * @author Christian Bremer
 */
@Disabled
@ExtendWith(SoftAssertionsExtension.class)
class DomainGroupLdapMapperTest {

  private static DomainGroupLdapMapper mapper;

  /**
   * Init.
   */
  @BeforeAll
  static void init() {
    DomainRepository domainRepository = mock(DomainRepository.class);
    lenient()
        .doReturn(true)
        .when(domainRepository.isRfc2307Enabled());
    mapper = new DomainGroupLdapMapper(domainRepository);
  }

  /**
   * Gets object classes.
   */
  @Test
  void getObjectClasses() {
    assertThat(mapper.getObjectClasses())
        .isEmpty();
  }

  /**
   * Map ldap entry.
   *
   * @param softly the soft assertions
   */
  @Test
  void map(SoftAssertions softly) {
    softly.assertThat(mapper.map(null)).isNull();

    DomainGroup destination = DomainGroup.builder().build();
    mapper.map(null, destination);
    //softly.assertThat(destination)
    //    .isEqualTo(DomainGroup.builder().build());

    LdapEntry source = new LdapEntry();
    source.setDn("cn=somename,cn=users,dc=example,dc=org");
    source.addAttributes(
        new LdapAttribute(AdConstants.WHEN_CREATED.getName(), "20170520150034.000Z"),
        new LdapAttribute(AdConstants.WHEN_CHANGED.getName(), "20180621160135.000Z"),
        new LdapAttribute(AdConstants.SAM_ACCOUNT_NAME.getName(), "somename"),
        new LdapAttribute(
            "member",
            "cn=member1,cn=users,dc=example,dc=org", "cn=member2,cn=users,dc=example,dc=org")
    );

    destination = mapper.map(source);
    softly.assertThat(destination)
        .extracting(DomainGroup::getDistinguishedName)
        .isEqualTo("cn=somename,cn=users,dc=example,dc=org");

    softly.assertThat(destination)
        .extracting(DomainGroup::getCreated)
        .extracting(OffsetDateTime::getYear)
        .isEqualTo(2017);
    softly.assertThat(destination)
        .extracting(DomainGroup::getCreated)
        .extracting(OffsetDateTime::getMonth)
        .isEqualTo(Month.MAY);
    softly.assertThat(destination)
        .extracting(DomainGroup::getCreated)
        .extracting(OffsetDateTime::getDayOfMonth)
        .isEqualTo(20);
    softly.assertThat(destination)
        .extracting(DomainGroup::getCreated)
        .extracting(OffsetDateTime::getHour)
        .isEqualTo(15);
    softly.assertThat(destination)
        .extracting(DomainGroup::getCreated)
        .extracting(OffsetDateTime::getMinute)
        .isEqualTo(0);
    softly.assertThat(destination)
        .extracting(DomainGroup::getCreated)
        .extracting(OffsetDateTime::getSecond)
        .isEqualTo(34);

    softly.assertThat(destination)
        .extracting(DomainGroup::getModified)
        .extracting(OffsetDateTime::getYear)
        .isEqualTo(2018);
    softly.assertThat(destination)
        .extracting(DomainGroup::getModified)
        .extracting(OffsetDateTime::getMonth)
        .isEqualTo(Month.JUNE);
    softly.assertThat(destination)
        .extracting(DomainGroup::getModified)
        .extracting(OffsetDateTime::getDayOfMonth)
        .isEqualTo(21);
    softly.assertThat(destination)
        .extracting(DomainGroup::getModified)
        .extracting(OffsetDateTime::getHour)
        .isEqualTo(16);
    softly.assertThat(destination)
        .extracting(DomainGroup::getModified)
        .extracting(OffsetDateTime::getMinute)
        .isEqualTo(1);
    softly.assertThat(destination)
        .extracting(DomainGroup::getModified)
        .extracting(OffsetDateTime::getSecond)
        .isEqualTo(35);

    softly.assertThat(destination)
        .extracting(DomainGroup::getSamAccountName)
        .isEqualTo("somename");
    softly.assertThat(destination)
        .extracting(DomainGroup::getMembers)
        .isEqualTo(List.of("member1", "member2"));
  }

  /**
   * Map and compute modifications.
   *
   * @param softly the soft assertions
   */
  @Test
  void mapAndComputeModifications(SoftAssertions softly) {
    DomainGroup source = DomainGroup.builder()
        .samAccountName("somename")
        .addMembers("member1", "member2")
        .build();

    LdapEntry destination = new LdapEntry();
    AttributeModification[] modifications = mapper.mapAndComputeModifications(source, destination);
    softly.assertThat(modifications)
        .hasSize(3); // plus 'sAMAccountName'
    softly.assertThat(destination.getAttribute("name").getStringValue())
        .isEqualTo("somename");
    softly.assertThat(destination.getAttribute("sAMAccountName").getStringValue())
        .isEqualTo("somename");
    softly.assertThat(destination.getAttribute("member").getStringValues())
        .containsExactlyInAnyOrder(
            "cn=member1,cn=users,dc=example,dc=org",
            "cn=member2,cn=users,dc=example,dc=org");

    source.getMembers().remove(0);
    mapper.mapAndComputeModifications(source, destination);
    softly.assertThat(destination.getAttribute("member").getStringValues())
        .containsExactlyInAnyOrder(
            "cn=member2,cn=users,dc=example,dc=org");
  }
}