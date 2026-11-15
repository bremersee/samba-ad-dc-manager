/*
 * Copyright 2025-2026 the original author or authors.
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

package org.bremersee.samba.ad.dc.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.repository.mapper.DelegatingGenericLdapMapper;
import org.bremersee.samba.ad.dc.repository.mapper.DomainComputerLdapMapper;
import org.bremersee.samba.ad.dc.repository.mapper.DomainGroupLdapMapper;
import org.bremersee.samba.ad.dc.repository.mapper.DomainUserLdapMapper;
import org.bremersee.samba.ad.dc.repository.mapper.GenericLdapMapper;
import org.bremersee.samba.ad.dc.repository.mapper.OrganizationalUnitLdapMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.dn.Dn;

/**
 * The organizational unit repository implementation test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class OrganizationalUnitRepositoryImplTest {

  private LdaptiveOperations ldapOperations;

  private OrganizationalUnitLdapMapper ouLdapMapper;

  private OrganizationalUnitRepositoryImpl target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    properties.getGroup().setDefaultOu("CN=Users");
    properties.getUser().setDefaultOu("CN=Users");
    properties.setBaseDn("dc=samdom,dc=example,dc=org");
    ldapOperations = mock(LdaptiveOperations.class);
    DomainRepository domainRepository = mock(DomainRepository.class);
    lenient()
        .doReturn(true)
        .when(domainRepository)
        .isRfc2307Enabled();

    ouLdapMapper = new OrganizationalUnitLdapMapper(properties);
    GenericLdapMapper genericLdapMapper = new DelegatingGenericLdapMapper(List.of(
        new DomainComputerLdapMapper(),
        new DomainGroupLdapMapper(domainRepository),
        new DomainUserLdapMapper(domainRepository),
        ouLdapMapper
    ));
    target = spy(new OrganizationalUnitRepositoryImpl(
        properties,
        ldapOperations,
        ouLdapMapper,
        genericLdapMapper));
  }

  /**
   * Gets default ou.
   */
  @Test
  void getDefaultOu() {
    Dn expected = new Dn("dc=samdom,dc=example,dc=org");
    Dn actual = target.getDefaultOu();
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Gets object class value.
   */
  @Test
  void getObjectClassValue() {
    assertThat(target.getObjectClassValue())
        .isEqualTo(AdConstants.OBJECT_CLASS_OU);
  }

  /**
   * Gets binary attributes.
   */
  @Test
  void getBinaryAttributes() {
    assertThat(target.getBinaryAttributes())
        .containsExactlyInAnyOrder(ouLdapMapper.getBinaryAttributeNames());
  }

  /**
   * Gets return attributes.
   */
  @Test
  void getReturnAttributes() {
    assertThat(target.getReturnAttributes())
        .containsExactlyInAnyOrder(ouLdapMapper.getMappedAttributeNames());
  }

  /**
   * Find custom ous.
   */
  @Test
  void findCustomOus() {
    OrganizationalUnit first = OrganizationalUnit.builder()
        .distinguishedName("ou=first,dc=samdom,dc=example,dc=org")
        .name("first")
        .build();
    OrganizationalUnit second = OrganizationalUnit.builder()
        .distinguishedName("ou=second,dc=samdom,dc=example,dc=org")
        .name("second")
        .build();
    List<OrganizationalUnit> expected = List.of(first, second);
    doReturn(expected.stream())
        .when(ldapOperations)
        .findAll(any(), any());

    List<OrganizationalUnit> actual = target.findCustomOus().toList();
    assertThat(actual)
        .containsExactlyInAnyOrderElementsOf(expected);
  }

  /**
   * Find all.
   */
  @Test
  void findAll() {
    OrganizationalUnit computers = OrganizationalUnit.builder()
        .distinguishedName("CN=Computers,dc=samdom,dc=example,dc=org")
        .name("Computers")
        .build();
    OrganizationalUnit users = OrganizationalUnit.builder()
        .distinguishedName("CN=Users,dc=samdom,dc=example,dc=org")
        .name("Users")
        .build();
    doAnswer(invocation -> {
      SearchRequest sr = invocation.getArgument(0);
      String baseDn = sr.getBaseDn().toLowerCase();
      OrganizationalUnit ou = baseDn.contains("cn=computers") ? computers : users;
      return Optional.of(ou);
    })
        .when(ldapOperations)
        .findOne(any(), any());

    OrganizationalUnit first = OrganizationalUnit.builder()
        .distinguishedName("ou=first,dc=samdom,dc=example,dc=org")
        .name("first")
        .build();
    OrganizationalUnit second = OrganizationalUnit.builder()
        .distinguishedName("ou=second,dc=samdom,dc=example,dc=org")
        .name("second")
        .build();
    List<OrganizationalUnit> custom = List.of(first, second);
    doReturn(custom.stream())
        .when(ldapOperations)
        .findAll(any(), any());

    List<OrganizationalUnit> expected = List.of(computers, users, first, second);
    List<OrganizationalUnit> actual = target.findAll().toList();
    assertThat(actual)
        .containsExactlyInAnyOrderElementsOf(expected);
  }

  /**
   * Find one.
   *
   * @param softly the softly
   */
  @Test
  void findOne(SoftAssertions softly) {
    OrganizationalUnit expected = OrganizationalUnit.builder()
        .distinguishedName("ou=first,dc=samdom,dc=example,dc=org")
        .name("first")
        .build();
    doReturn(Optional.of(expected))
        .when(ldapOperations)
        .findOne(any(), any());
    Dn dn = new Dn("ou=first,dc=samdom,dc=example,dc=org");
    Optional<OrganizationalUnit> actual = target.findOne(dn);
    softly
        .assertThat(actual)
        .hasValue(expected);

    dn = new Dn("");
    actual = target.findOne(dn);
    softly
        .assertThat(actual)
        .isEmpty();
  }

  /**
   * Exists.
   */
  @Test
  void exists() {
    OrganizationalUnit expected = OrganizationalUnit.builder()
        .distinguishedName("ou=first,dc=samdom,dc=example,dc=org")
        .name("first")
        .build();
    doReturn(Optional.of(expected))
        .when(ldapOperations)
        .findOne(any(), any());
    Dn dn = new Dn("ou=first,dc=samdom,dc=example,dc=org");
    boolean actual = target.exists(dn);
    assertThat(actual)
        .isTrue();
  }

  /**
   * Has children.
   */
  @Test
  void hasChildren() {
    doReturn(List.of(mock(LdapEntry.class)))
        .when(ldapOperations)
        .findAll(any());
    Dn dn = new Dn("ou=first,dc=samdom,dc=example,dc=org");
    boolean actual = target.hasChildren(dn);
    assertThat(actual)
        .isTrue();
  }

  /**
   * Has no children.
   */
  @Test
  void hasNoChildren() {
    doReturn(List.of())
        .when(ldapOperations)
        .findAll(any());
    Dn dn = new Dn("ou=first,dc=samdom,dc=example,dc=org");
    boolean actual = target.hasChildren(dn);
    assertThat(actual)
        .isFalse();
  }

  /**
   * Gets children.
   */
  @Test
  void getChildren() {
    LdapEntry ldapEntry = new LdapEntry();
    ldapEntry.setDn("ou=second,ou=first,dc=samdom,dc=example,dc=org");
    AdConstants.DN.setValue(ldapEntry, new Dn("ou=second,ou=first,dc=samdom,dc=example,dc=org"));
    AdConstants.NAME.setValue(ldapEntry, "second");
    AdConstants.OBJECT_CLASS.setValue(ldapEntry, AdConstants.OBJECT_CLASS_OU);
    doReturn(List.of(ldapEntry))
        .when(ldapOperations)
        .findAll(any());

    OrganizationalUnit expected = OrganizationalUnit.builder()
        .distinguishedName("ou=second,ou=first,dc=samdom,dc=example,dc=org")
        .name("second")
        .build();

    Dn dn = new Dn("ou=first,dc=samdom,dc=example,dc=org");
    List<AdEntry> actual = target.getChildren(dn).toList();
    assertThat(actual)
        .containsExactly(expected);
  }

  /**
   * Add.
   */
  @Test
  void add() {
    OrganizationalUnit organizationalUnit = OrganizationalUnit.builder()
        .name("first")
        .build();
    Dn parentOu = new Dn("dc=samdom,dc=example,dc=org");

    LdapEntry parentEntry = new LdapEntry();
    parentEntry.setDn(parentOu.format());
    AdConstants.DN.setValue(parentEntry, parentOu);
    doReturn(Optional.of(parentEntry))
        .when(ldapOperations)
        .findOne(any());

    doReturn(false)
        .when(target)
        .exists(new Dn("OU=first,dc=samdom,dc=example,dc=org"));

    OrganizationalUnit expected = OrganizationalUnit.builder()
        .from(organizationalUnit)
        .distinguishedName("OU=first,dc=samdom,dc=example,dc=org")
        .build();
    doReturn(expected)
        .when(ldapOperations)
        .save(any(), any());

    OrganizationalUnit actual = target.add(organizationalUnit, parentOu);

    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Update system ou.
   */
  @Test
  void updateSystemOu() {
    OrganizationalUnit organizationalUnit = OrganizationalUnit.builder()
        .distinguishedName("CN=Users,dc=samdom,dc=example,dc=org")
        .name("Users")
        .systemOu(true)
        .description("Old Description")
        .build();
    doReturn(Optional.of(organizationalUnit))
        .when(target)
        .findOne(any());
    OrganizationalUnit expected = OrganizationalUnit.builder()
        .from(organizationalUnit)
        .description("New Description")
        .build();
    doReturn(expected)
        .when(ldapOperations)
        .save(any(), any());
    OrganizationalUnit actual = target.update(expected, null);
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Update with same parent.
   */
  @Test
  void updateWithSameParent() {
    OrganizationalUnit existing = OrganizationalUnit.builder()
        .distinguishedName("OU=first,dc=samdom,dc=example,dc=org")
        .name("first")
        .systemOu(false)
        .description("Old Description")
        .build();
    doReturn(Optional.of(existing))
        .when(target)
        .findOne(any());
    doReturn(false)
        .when(target)
        .exists(any());
    OrganizationalUnit requested = OrganizationalUnit.builder()
        .from(existing)
        .name("second")
        .description("New Description")
        .build();
    OrganizationalUnit expected = OrganizationalUnit.builder()
        .from(requested)
        .distinguishedName("OU=second,dc=samdom,dc=example,dc=org")
        .build();
    doReturn(expected)
        .when(ldapOperations)
        .save(any(), any());
    OrganizationalUnit actual = target.update(requested, null);
    assertThat(actual)
        .isEqualTo(expected);
    verify(ldapOperations).modifyDn(any());
  }

  /**
   * Update with different parent.
   */
  @Test
  void updateWithDifferentParent() {
    OrganizationalUnit existing = OrganizationalUnit.builder()
        .distinguishedName("OU=first,dc=samdom,dc=example,dc=org")
        .name("first")
        .systemOu(false)
        .description("Old Description")
        .build();
    doReturn(Optional.of(existing))
        .when(target)
        .findOne(any());
    doReturn(false)
        .when(target)
        .exists(any());
    OrganizationalUnit requested = OrganizationalUnit.builder()
        .from(existing)
        .name("second")
        .description("New Description")
        .build();
    OrganizationalUnit expected = OrganizationalUnit.builder()
        .from(requested)
        .distinguishedName("OU=second,OU=junit,dc=samdom,dc=example,dc=org")
        .build();
    doReturn(expected)
        .when(ldapOperations)
        .save(any(), any());
    Dn dn = new Dn("OU=junit,dc=samdom,dc=example,dc=org");
    LdapEntry parentEntry = new LdapEntry();
    parentEntry.setDn("OU=junit,dc=samdom,dc=example,dc=org");
    AdConstants.DN.setValue(parentEntry, dn);
    doReturn(Optional.of(parentEntry))
        .when(ldapOperations)
        .findOne(any());
    OrganizationalUnit actual = target.update(requested, dn);
    assertThat(actual)
        .isEqualTo(expected);
    verify(ldapOperations).modifyDn(any());
  }

  /**
   * Delete returns true.
   */
  @Test
  void deleteReturnsTrue() {
    OrganizationalUnit organizationalUnit = OrganizationalUnit.builder()
        .distinguishedName("OU=first,dc=samdom,dc=example,dc=org")
        .name("first")
        .build();
    doReturn(Optional.of(organizationalUnit))
        .when(target)
        .findOne(any());
    Dn dn = new Dn("OU=first,dc=samdom,dc=example,dc=org");
    boolean actual = target.delete(dn);
    assertThat(actual)
        .isTrue();
    verify(ldapOperations).delete(any());
  }

  /**
   * Delete returns false.
   */
  @Test
  void deleteReturnsFalse() {
    OrganizationalUnit organizationalUnit = OrganizationalUnit.builder()
        .distinguishedName("OU=first,dc=samdom,dc=example,dc=org")
        .name("first")
        .systemOu(true)
        .build();
    doReturn(Optional.of(organizationalUnit))
        .when(target)
        .findOne(any());
    Dn dn = new Dn("OU=first,dc=samdom,dc=example,dc=org");
    boolean actual = target.delete(dn);
    assertThat(actual)
        .isFalse();
    verify(ldapOperations, never()).delete(any());
  }

}