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
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.repository.mapper.DomainUserLdapMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.dn.Dn;

/**
 * The domain user repository implementation test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class DomainUserRepositoryImplTest {

  private LdaptiveOperations ldapOperations;

  private DomainRepository domainRepository;

  private DomainUserLdapMapper domainUserLdapMapper;

  private DomainUserRepositoryImpl target;

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
    domainRepository = mock(DomainRepository.class);
    lenient()
        .doReturn(true)
        .when(domainRepository)
        .isRfc2307Enabled();
    domainUserLdapMapper = new DomainUserLdapMapper(domainRepository);
    target = spy(new DomainUserRepositoryImpl(
        properties,
        ldapOperations,
        domainRepository,
        domainUserLdapMapper));
  }

  /**
   * Gets default ou.
   */
  @Test
  void getDefaultOu() {
    Dn expected = new Dn("CN=Users");
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
        .isEqualTo(AdConstants.OBJECT_CLASS_USER);
  }

  /**
   * Gets binary attributes.
   */
  @Test
  void getBinaryAttributes() {
    assertThat(target.getBinaryAttributes())
        .containsExactlyInAnyOrder(domainUserLdapMapper.getBinaryAttributeNames());
  }

  /**
   * Gets return attributes.
   */
  @Test
  void getReturnAttributes() {
    assertThat(target.getReturnAttributes())
        .containsExactlyInAnyOrder(domainUserLdapMapper.getMappedAttributeNames());
  }

  /**
   * Find all.
   *
   * @param softly the softly
   */
  @Test
  void findAll(SoftAssertions softly) {
    List<DomainUser> expected = List.of(DomainUser.builder()
        .distinguishedName("CN=user,CN=Users,DC=samdom,DC=example,DC=org")
        .samAccountName("user")
        .build());
    doAnswer(invocationOnMock -> expected.stream())
        .when(ldapOperations)
        .findAll(any(), any());
    List<DomainUser> actual = target
        .findAll("user", new Dn("CN=Users"), TreeSearchScope.ONELEVEL).toList();
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
    DomainUser expected = DomainUser.builder()
        .distinguishedName("CN=user,CN=Users,DC=samdom,DC=example,DC=org")
        .samAccountName("user")
        .build();
    doReturn(Optional.of(expected))
        .when(ldapOperations)
        .findOne(any(), any());
    Optional<DomainUser> actual = target.findOne("user", null, null);
    softly
        .assertThat(actual)
        .hasValue(expected);

    actual = target.findOne("user", new Dn("CN=Users"), TreeSearchScope.ONELEVEL);
    softly
        .assertThat(actual)
        .hasValue(expected);
  }

  /**
   * Find one by principal name.
   *
   * @param softly the softly
   */
  @Test
  void findOneByPrincipalName(SoftAssertions softly) {
    DomainUser expected = DomainUser.builder()
        .distinguishedName("CN=user,CN=Users,DC=samdom,DC=example,DC=org")
        .samAccountName("user")
        .userPrincipalName("user@samdom.example.org")
        .build();
    doReturn(Optional.of(expected))
        .when(ldapOperations)
        .findOne(any(), any());
    Optional<DomainUser> actual = target.findOneByPrincipalName("user@samdom.example.org");
    softly
        .assertThat(actual)
        .hasValue(expected);

    actual = target.findOneByPrincipalName(null);
    softly
        .assertThat(actual)
        .isEmpty();
  }

  /**
   * Find one by uid.
   *
   * @param softly the softly
   */
  @Test
  void findOneByUid(SoftAssertions softly) {
    DomainUser expected = DomainUser.builder()
        .distinguishedName("CN=user,CN=Users,DC=samdom,DC=example,DC=org")
        .samAccountName("user")
        .uid("junit")
        .build();
    doReturn(Optional.of(expected))
        .when(ldapOperations)
        .findOne(any(), any());
    Optional<DomainUser> actual = target.findOneByUid("junit");
    softly
        .assertThat(actual)
        .hasValue(expected);

    actual = target.findOneByUid(null);
    softly
        .assertThat(actual)
        .isEmpty();
  }

  /**
   * Find one by uid number.
   *
   * @param softly the softly
   */
  @Test
  void findOneByUidNumber(SoftAssertions softly) {
    DomainUser expected = DomainUser.builder()
        .distinguishedName("CN=user,CN=Users,DC=samdom,DC=example,DC=org")
        .samAccountName("user")
        .uidNumber(9876)
        .build();
    doReturn(Optional.of(expected))
        .when(ldapOperations)
        .findOne(any(), any());
    Optional<DomainUser> actual = target.findOneByUidNumber(9876);
    softly
        .assertThat(actual)
        .hasValue(expected);

    actual = target.findOneByUidNumber(null);
    softly
        .assertThat(actual)
        .isEmpty();
  }

  /**
   * Exists by principal name.
   *
   * @param softly the softly
   */
  @Test
  void existsByPrincipalName(SoftAssertions softly) {
    doReturn(Optional.of(mock(LdapEntry.class)))
        .when(ldapOperations)
        .findOne(any());
    boolean actual = target.existsByPrincipalName("user@samdom.example.org");
    softly
        .assertThat(actual)
        .isTrue();

    actual = target.existsByPrincipalName(null);
    softly
        .assertThat(actual)
        .isFalse();
  }

  /**
   * Exists by uid.
   *
   * @param softly the softly
   */
  @Test
  void existsByUid(SoftAssertions softly) {
    doReturn(Optional.of(mock(LdapEntry.class)))
        .when(ldapOperations)
        .findOne(any());
    boolean actual = target.existsByUid("junit");
    softly
        .assertThat(actual)
        .isTrue();

    actual = target.existsByUid(null);
    softly
        .assertThat(actual)
        .isFalse();
  }

  /**
   * Exists by uid number.
   *
   * @param softly the softly
   */
  @Test
  void existsByUidNumber(SoftAssertions softly) {
    doReturn(Optional.of(mock(LdapEntry.class)))
        .when(ldapOperations)
        .findOne(any());
    boolean actual = target.existsByUidNumber(9876);
    softly
        .assertThat(actual)
        .isTrue();

    actual = target.existsByUidNumber(null);
    softly
        .assertThat(actual)
        .isFalse();
  }

  /**
   * Add.
   */
  @Test
  void add() {
    DomainInfo domainInfo = DomainInfo.builder()
        .domain("samdom.example.org")
        .build();
    doReturn(domainInfo)
        .when(domainRepository)
        .getDomainInfo();

    PasswordInformation passwordInformation = PasswordInformation.builder().build();
    doReturn(passwordInformation)
        .when(domainRepository)
        .getPasswordInformation();

    doReturn(ldapOperations)
        .when(ldapOperations)
        .copy(any());

    String pwd = "LongAndComplexWith123And!%$";
    DomainUser domainUser = DomainUser.builder()
        .samAccountName("user")
        .build();
    String newParentDn = "CN=Users,DC=samdom,DC=example,DC=org";
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
    DomainUser expected = DomainUser.builder()
        .from(domainUser)
        .distinguishedName("CN=user,CN=Users,DC=samdom,DC=example,DC=org")
        .build();
    doReturn(expected)
        .when(ldapOperations)
        .save(any(), any());
    DomainUser actual = target.add(domainUser, pwd, new Dn("CN=Users"), true);
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Update.
   */
  @Test
  void update() {
    DomainUser existing = DomainUser.builder()
        .distinguishedName("CN=user,CN=Users,DC=samdom,DC=example,DC=org")
        .samAccountName("user")
        .email("user@samdom.example.org")
        .build();
    doReturn(Optional.of(existing))
        .when(target)
        .findOne("user", null, null);
    String newParentDn = "CN=Users,DC=samdom,DC=example,DC=org";
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

    DomainUser user = DomainUser.builder()
        .from(existing)
        .distinguishedName("CN=user,CN=Users,DC=samdom,DC=example,DC=org")
        .samAccountName("newuser")
        .email("newuser@samdom.example.org")
        .build();

    DomainUser expected = DomainUser.builder()
        .from(user)
        .distinguishedName("CN=user,CN=Users,DC=samdom,DC=example,DC=org")
        .build();
    doReturn(expected)
        .when(ldapOperations)
        .save(any(), any());

    Dn ou = new Dn("CN=Users");
    DomainUser actual = target.update("user", user, ou);

    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Save password.
   */
  @Test
  void savePassword() {
    LdapEntry ldapEntry = new LdapEntry();
    ldapEntry.setDn("CN=user,CN=Users,DC=samdom,DC=example,DC=org");
    doReturn(Optional.of(ldapEntry))
        .when(ldapOperations)
        .findOne(any());

    doReturn(ldapOperations)
        .when(ldapOperations)
        .copy(any());

    assertThatNoException().isThrownBy(() -> target
        .savePassword("user", "LongAndComplexWith123And!%$"));

    verify(ldapOperations)
        .modify(any());
  }

  /**
   * Delete.
   */
  @Test
  void delete() {
    DomainUser existing = DomainUser.builder()
        .distinguishedName("CN=user,CN=Users,DC=samdom,DC=example,DC=org")
        .samAccountName("user")
        .build();
    doReturn(Optional.of(existing))
        .when(target)
        .findOne("user", null, null);

    boolean actual = target.delete("user");
    assertThat(actual)
        .isTrue();
    verify(ldapOperations)
        .delete(any());
  }
}