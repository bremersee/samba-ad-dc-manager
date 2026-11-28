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


package org.bremersee.samba.ad.dc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.ModelConstants;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.repository.OrganizationalUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ldaptive.dn.Dn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * The organizational unit service implementation test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class OrganizationalUnitServiceImplTest {

  private static final String BASE_DN = "DC=samdom,DC=example,DC=org";

  private OrganizationalUnitRepository repository;

  private OrganizationalUnitServiceImpl target;

  private OrganizationalUnit users;

  private OrganizationalUnit sales;

  private OrganizationalUnit marketing;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    properties.setBaseDn(BASE_DN);
    repository = mock(OrganizationalUnitRepository.class);
    target = new OrganizationalUnitServiceImpl(
        properties, SortMapper.defaultSortMapper(), repository);

    users = OrganizationalUnit.builder()
        .distinguishedName("CN=Users," + BASE_DN)
        .name("Users")
        .description("Default container for users")
        .systemOu(true)
        .build();
    sales = OrganizationalUnit.builder()
        .distinguishedName("OU=Sales," + BASE_DN)
        .name("Sales")
        .description("The sales team")
        .build();
    marketing = OrganizationalUnit.builder()
        .distinguishedName("OU=Marketing,OU=Sales," + BASE_DN)
        .name("Marketing")
        .build();
  }

  /**
   * Gets base.
   *
   * @param softly the softly
   */
  @Test
  void getBase(SoftAssertions softly) {
    OrganizationalUnit base = target.getBase();
    softly.assertThat(base.getDistinguishedName()).isEqualTo(BASE_DN);
    softly.assertThat(base.getName()).isEqualTo("Base");
    softly.assertThat(base.isSystemOu()).isTrue();
    softly.assertThat(base.getCreated()).isNotNull();
    softly.assertThat(base.getModified()).isNotNull();
  }

  /**
   * Contains.
   *
   * @param softly the softly
   */
  @Test
  void contains(SoftAssertions softly) {
    softly.assertThat(target.contains(sales, "sal")).isTrue();
    softly.assertThat(target.contains(sales, "team")).isTrue();
    softly.assertThat(target.contains(sales, "xyz")).isFalse();
    softly.assertThat(target.contains(marketing, "team")).isFalse();
  }

  /**
   * Gets organizational units with paging.
   *
   * @param softly the softly
   */
  @Test
  void getOrganizationalUnitsWithPaging(SoftAssertions softly) {
    doAnswer(invocation -> Stream.of(users, sales, marketing))
        .when(repository)
        .findAll();

    // short queries are ignored
    Page<OrganizationalUnit> actual = target.getOrganizationalUnits(
        PageRequest.of(0, 10, Sort.by("name")), "sa");
    softly.assertThat(actual.getContent())
        .containsExactly(marketing, sales, users);

    actual = target.getOrganizationalUnits(PageRequest.of(0, 10, Sort.by("name")), null);
    softly.assertThat(actual.getTotalElements())
        .isEqualTo(3L);

    actual = target.getOrganizationalUnits(PageRequest.of(0, 10, Sort.by("name")), "SALES");
    softly.assertThat(actual.getContent())
        .containsExactly(sales);

    actual = target.getOrganizationalUnits(PageRequest.of(0, 10, Sort.by("name")), "default");
    softly.assertThat(actual.getContent())
        .containsExactly(users);
  }

  /**
   * Gets organizational units.
   */
  @Test
  void getOrganizationalUnits() {
    doAnswer(invocation -> Stream.of(marketing, sales))
        .when(repository)
        .findCustomOus();
    assertThat(target.getOrganizationalUnits())
        .containsExactly(sales, marketing); // 'Sales' < 'Sales → Marketing'
  }

  /**
   * Gets organizational units with system ous and base.
   *
   * @param softly the softly
   */
  @Test
  void getOrganizationalUnitsWithSystemOusAndBase(SoftAssertions softly) {
    doAnswer(invocation -> Stream.of(users, marketing, sales))
        .when(repository)
        .findAll();
    softly.assertThat(target.getOrganizationalUnitsWithSystemOus())
        .containsExactly(sales, marketing, users);
    softly.assertThat(target.getOrganizationalUnitsWithSystemOusAndBase())
        .containsExactly(target.getBase(), sales, marketing, users);
  }

  /**
   * Gets organizational unit.
   *
   * @param softly the softly
   */
  @Test
  void getOrganizationalUnit(SoftAssertions softly) {
    Dn salesDn = new Dn(sales.getDistinguishedName());
    doReturn(Optional.of(sales)).when(repository).findOne(salesDn);
    doReturn(Optional.empty()).when(repository).findOne(new Dn(BASE_DN));

    softly.assertThat(target.getOrganizationalUnit(salesDn))
        .hasValue(sales);
    softly.assertThat(target.getOrganizationalUnit(new Dn(BASE_DN)))
        .hasValue(target.getBase());
    softly.assertThat(target.getOrganizationalUnit(new Dn("OU=Unknown," + BASE_DN)))
        .isEmpty();
    softly.assertThat(target.getOrganizationalUnit(null))
        .isEmpty();
    softly.assertThat(target.getOrganizationalUnit(new Dn()))
        .isEmpty();
  }

  /**
   * Organisation unit exists.
   *
   * @param softly the softly
   */
  @Test
  void organisationUnitExists(SoftAssertions softly) {
    Dn salesDn = new Dn(sales.getDistinguishedName());
    doReturn(true).when(repository).exists(salesDn);

    softly.assertThat(target.organisationUnitExists(null)).isFalse();
    softly.assertThat(target.organisationUnitExists(new Dn())).isFalse();
    softly.assertThat(target.organisationUnitExists(new Dn(BASE_DN.toLowerCase()))).isTrue();
    softly.assertThat(target.organisationUnitExists(salesDn)).isTrue();
    softly.assertThat(target.organisationUnitExists(new Dn("OU=Unknown," + BASE_DN))).isFalse();
    verify(repository, never()).exists(new Dn(BASE_DN.toLowerCase()));
  }

  /**
   * Has children.
   *
   * @param softly the softly
   */
  @Test
  void hasChildren(SoftAssertions softly) {
    Dn salesDn = new Dn(sales.getDistinguishedName());
    doReturn(true).when(repository).hasChildren(salesDn);
    softly.assertThat(target.hasChildren(salesDn)).isTrue();
    softly.assertThat(target.hasChildren(new Dn(marketing.getDistinguishedName()))).isFalse();
  }

  /**
   * Gets children.
   *
   * @param softly the softly
   */
  @Test
  void getChildren(SoftAssertions softly) {
    Dn salesDn = new Dn(sales.getDistinguishedName());
    DomainUser anna = DomainUser.builder()
        .distinguishedName("CN=anna,OU=Sales," + BASE_DN)
        .samAccountName("anna")
        .build();
    doAnswer(invocation -> Stream.<AdEntry>of(anna, marketing))
        .when(repository)
        .getChildren(salesDn);

    Page<AdEntry> actual = target.getChildren(
        salesDn, PageRequest.of(0, 10, Sort.by(ModelConstants.DISCRIMINATOR)));
    List<AdEntry> expected = Stream.<AdEntry>of(anna, marketing)
        .sorted(Comparator.comparingInt(e -> e.getAdEntryType().getSortOrder()))
        .toList();
    softly.assertThat(actual.getContent())
        .containsExactlyElementsOf(expected);

    actual = target.getChildren(
        salesDn, PageRequest.of(0, 10, Sort.by("distinguishedName")));
    softly.assertThat(actual.getContent())
        .containsExactly(anna, marketing);
  }

  /**
   * Add, update and delete.
   *
   * @param softly the softly
   */
  @Test
  void addUpdateAndDelete(SoftAssertions softly) {
    Dn salesDn = new Dn(sales.getDistinguishedName());
    doReturn(marketing).when(repository).add(any(), any());
    doReturn(marketing).when(repository).update(any(), any());
    doReturn(true).when(repository).delete(salesDn);

    softly.assertThat(target.add(marketing, salesDn)).isEqualTo(marketing);
    verify(repository).add(marketing, salesDn);
    softly.assertThat(target.update(marketing, null)).isEqualTo(marketing);
    verify(repository).update(marketing, null);
    softly.assertThat(target.delete(salesDn)).isTrue();
  }

}
