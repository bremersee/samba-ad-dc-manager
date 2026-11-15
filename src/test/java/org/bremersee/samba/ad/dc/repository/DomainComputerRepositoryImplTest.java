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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.repository.mapper.DomainComputerLdapMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.dn.Dn;

/**
 * The domain computer repository implementation test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class DomainComputerRepositoryImplTest {

  private LdaptiveOperations ldapOperations;

  private LdaptiveEntryMapper<DomainComputer> domainComputerLdapMapper;

  private DomainComputerRepositoryImpl target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    properties.setBaseDn("dc=samdom,dc=example,dc=org");
    properties.getComputer().setDefaultOu("CN=Computers");
    ldapOperations = mock(LdaptiveOperations.class);
    domainComputerLdapMapper = new DomainComputerLdapMapper();
    target = new DomainComputerRepositoryImpl(
        properties,
        ldapOperations,
        domainComputerLdapMapper);
  }

  /**
   * Gets default ou.
   */
  @Test
  void getDefaultOu() {
    Dn expected = new Dn("CN=Computers");
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
        .isEqualTo(AdConstants.OBJECT_CLASS_COMPUTER);
  }

  /**
   * Gets binary attributes.
   */
  @Test
  void getBinaryAttributes() {
    assertThat(target.getBinaryAttributes())
        .containsExactlyInAnyOrder(domainComputerLdapMapper.getBinaryAttributeNames());
  }

  /**
   * Gets return attributes.
   */
  @Test
  void getReturnAttributes() {
    assertThat(target.getReturnAttributes())
        .containsExactlyInAnyOrder(domainComputerLdapMapper.getMappedAttributeNames());
  }

  /**
   * Find all.
   *
   * @param softly the softly
   */
  @Test
  void findAll(SoftAssertions softly) {
    List<DomainComputer> expected = List.of(DomainComputer.builder()
        .distinguishedName("CN=dc,CN=Computers,DC=samdom,DC=example,DC=org")
        .samAccountName("dc$")
        .build());
    doAnswer(invocationOnMock -> expected.stream())
        .when(ldapOperations)
        .findAll(any(), any());
    List<DomainComputer> actual = target
        .findAll("dc", new Dn("CN=Computers"), TreeSearchScope.ONELEVEL).toList();
    softly
        .assertThat(actual)
        .containsExactlyInAnyOrderElementsOf(expected);
    actual = target.findAll("", null, null).toList();
    softly
        .assertThat(actual)
        .containsExactlyInAnyOrderElementsOf(expected);
  }

  /**
   * Find one.
   *
   * @param softly the softly
   */
  @Test
  void findOne(SoftAssertions softly) {
    DomainComputer expected = DomainComputer.builder()
        .distinguishedName("CN=dc,CN=Computers,DC=samdom,DC=example,DC=org")
        .samAccountName("dc$")
        .build();
    doReturn(Optional.of(expected))
        .when(ldapOperations)
        .findOne(any(), any());
    Optional<DomainComputer> actual = target.findOne("dc", null, null);
    softly
        .assertThat(actual)
        .hasValue(expected);
    target.findOne("dc", new Dn("CN=Computers"), TreeSearchScope.ONELEVEL);
    softly
        .assertThat(actual)
        .hasValue(expected);
  }

  /**
   * Update.
   */
  @Test
  void update() {
    DomainComputer existing = DomainComputer.builder()
        .distinguishedName("CN=dc,CN=Computers,DC=samdom,DC=example,DC=org")
        .samAccountName("dc$")
        .build();
    doReturn(Optional.of(existing))
        .when(ldapOperations)
        .findOne(any(), any());
    String newParentDn = "CN=Domain Controllers,DC=samdom,DC=example,DC=org";
    doAnswer(
        invocationOnMock -> {
          SearchRequest searchRequest = invocationOnMock.getArgument(0);
          if (DnTool.isSameDn(newParentDn, searchRequest.getBaseDn())) {
            LdapEntry entry = new LdapEntry();
            entry.setDn(newParentDn);
            AdConstants.DN.setValue(entry, new Dn(newParentDn));
            return Optional.of(entry);
          }
          return Optional.empty();
        })
        .when(ldapOperations)
        .findOne(any());
    DomainComputer expected = DomainComputer.builder()
        .from(existing)
        .distinguishedName("CN=dc," + newParentDn)
        .description("description")
        .build();
    doReturn(expected)
        .when(ldapOperations)
        .save(any(), any());
    DomainComputer modified = DomainComputer.builder()
        .from(existing)
        .description("description")
        .build();
    DomainComputer actual = target.update(modified, new Dn("CN=Domain Controllers"));
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Update not found.
   */
  @Test
  void updateNotFound() {
    doReturn(Optional.empty())
        .when(ldapOperations)
        .findOne(any(), any());
    DomainComputer modified = DomainComputer.builder()
        .distinguishedName("CN=dc,CN=Domain Controllers,DC=samdom,DC=example,DC=org")
        .samAccountName("dc$")
        .description("description")
        .build();
    assertThatExceptionOfType(ServiceException.class)
        .isThrownBy(() -> target.update(modified, null));
  }

  /**
   * Update already exists.
   */
  @Test
  void updateAlreadyExists() {
    DomainComputer existing = DomainComputer.builder()
        .distinguishedName("CN=dc,CN=Computers,DC=samdom,DC=example,DC=org")
        .samAccountName("dc$")
        .build();
    doReturn(Optional.of(existing))
        .when(ldapOperations)
        .findOne(any(), any());
    String newDn = "CN=dc,CN=Domain Controllers,DC=samdom,DC=example,DC=org";
    LdapEntry entry = new LdapEntry();
    entry.setDn(newDn);
    AdConstants.DN.setValue(entry, new Dn(newDn));
    AdConstants.OBJECT_CLASS.setValue(entry, AdConstants.OBJECT_CLASS_COMPUTER);
    doReturn(Optional.of(entry))
        .when(ldapOperations)
        .findOne(any());
    DomainComputer modified = DomainComputer.builder()
        .from(existing)
        .description("description")
        .build();
    Dn newOu = new Dn("CN=Domain Controllers");
    assertThatExceptionOfType(ServiceException.class)
        .isThrownBy(() -> target.update(modified, newOu));
  }

  /**
   * Delete returns true.
   */
  @Test
  void deleteReturnsTrue() {
    DomainComputer existing = DomainComputer.builder()
        .distinguishedName("CN=dc,CN=Computers,DC=samdom,DC=example,DC=org")
        .samAccountName("dc$")
        .build();
    doReturn(Optional.of(existing))
        .when(ldapOperations)
        .findOne(any(), any());
    boolean actual = target.delete("dc$");
    assertThat(actual).isTrue();
    verify(ldapOperations).delete(any());
  }

  /**
   * Delete returns false.
   */
  @Test
  void deleteReturnsFalse() {
    doReturn(Optional.empty())
        .when(ldapOperations)
        .findOne(any(), any());
    boolean actual = target.delete("dc$");
    assertThat(actual).isFalse();
  }
}