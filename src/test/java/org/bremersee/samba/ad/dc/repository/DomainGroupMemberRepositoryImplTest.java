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

package org.bremersee.samba.ad.dc.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainGroupMember;
import org.bremersee.samba.ad.dc.model.DomainGroupMemberType;
import org.bremersee.samba.ad.dc.model.SamAccount;
import org.bremersee.samba.ad.dc.model.SamAccountIntermediate;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.repository.mapper.DomainGroupMemberLdapMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ldaptive.dn.Dn;

/**
 * The domain group member repository implementation test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class DomainGroupMemberRepositoryImplTest {

  private static final DomainGroupMemberLdapMapper MAPPER = new DomainGroupMemberLdapMapper();

  private LdaptiveOperations ldapOperations;

  private DomainGroupRepository domainGroupRepository;

  private DomainGroupMemberRepositoryImpl target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    properties.getGroup().setDefaultOu("CN=Users");
    properties.setBaseDn("dc=samdom,dc=example,dc=org");
    ldapOperations = mock(LdaptiveOperations.class);
    domainGroupRepository = mock(DomainGroupRepository.class);
    target = spy(new DomainGroupMemberRepositoryImpl(
        properties,
        ldapOperations,
        domainGroupRepository));
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
        .isEqualTo(AdConstants.OBJECT_CLASS_GROUP);
  }

  /**
   * Gets binary attributes.
   */
  @Test
  void getBinaryAttributes() {
    assertThat(target.getBinaryAttributes())
        .containsExactlyInAnyOrder(MAPPER.getBinaryAttributeNames());
  }

  /**
   * Gets return attributes.
   */
  @Test
  void getReturnAttributes() {
    assertThat(target.getReturnAttributes())
        .containsExactlyInAnyOrder(MAPPER.getMappedAttributeNames());
  }

  /**
   * Gets memberships.
   */
  @Test
  void getMemberships() {
    String samAccountName = "junit";
    Dn ou = new Dn("CN=Users");
    TreeSearchScope treeSearchScope = TreeSearchScope.ONELEVEL;
    SamAccount samAccount = SamAccountIntermediate.builder()
        .samAccountName(samAccountName)
        .memberships(List.of("CN=group,CN=Users,dc=samdom,dc=example,dc=org"))
        .primaryGroupId(100)
        .build();
    doReturn(Optional.of(samAccount))
        .when(target)
        .findSamAccount(samAccountName, ou, treeSearchScope);
    DomainGroup domainGroup = DomainGroup.builder()
        .samAccountName("group")
        .build();
    doReturn(Optional.of(domainGroup))
        .when(domainGroupRepository)
        .findOne("CN=group,CN=Users,dc=samdom,dc=example,dc=org", null, null);
    DomainGroup primaryDomainGroup = DomainGroup.builder()
        .samAccountName("primaryGroup")
        .build();
    doReturn(Optional.of(primaryDomainGroup))
        .when(domainGroupRepository)
        .findOneByPrimaryGroupId(100);
    List<DomainGroup> expected = List.of(domainGroup, primaryDomainGroup);
    List<DomainGroup> actual = target.getMemberships(samAccountName, ou, treeSearchScope).toList();
    assertThat(actual)
        .containsExactlyInAnyOrderElementsOf(expected);
  }

  /**
   * Find sam account by dn.
   */
  @Test
  void findSamAccountByDn() {
    String dn = "CN=junit,CN=Users,dc=samdom,dc=example,dc=org";
    DomainGroupMember expected = DomainGroupMember.builder()
        .samAccountName("junit")
        .distinguishedName(dn)
        .memberType(DomainGroupMemberType.USER)
        .displayName("junit")
        .build();
    doReturn(Optional.of(expected))
        .when(ldapOperations)
        .findOne(any(), any());
    Optional<SamAccount> actual = target.findSamAccount(dn, null, null);
    assertThat(actual)
        .hasValue(expected);
  }

  /**
   * Find sam account by name.
   */
  @Test
  void findSamAccountByName() {
    String samAccountName = "junit";
    String dn = "CN=junit,CN=Users,dc=samdom,dc=example,dc=org";
    DomainGroupMember expected = DomainGroupMember.builder()
        .samAccountName(samAccountName)
        .distinguishedName(dn)
        .memberType(DomainGroupMemberType.USER)
        .displayName("junit")
        .build();
    doReturn(Optional.of(expected))
        .when(ldapOperations)
        .findOne(any(), any());
    Optional<SamAccount> actual = target.findSamAccount(samAccountName, null, null);
    assertThat(actual)
        .hasValue(expected);
  }

  /**
   * Find sam account by name and ou and scope.
   */
  @Test
  void findSamAccountByNameAndOuAndScope() {
    String samAccountName = "junit";
    String dn = "CN=junit,CN=Users,dc=samdom,dc=example,dc=org";
    DomainGroupMember expected = DomainGroupMember.builder()
        .samAccountName(samAccountName)
        .distinguishedName(dn)
        .memberType(DomainGroupMemberType.USER)
        .displayName("junit")
        .build();
    doReturn(Optional.of(expected))
        .when(ldapOperations)
        .findOne(any(), any());
    Optional<SamAccount> actual = target.findSamAccount(
        samAccountName,
        new Dn("CN=Users,dc=samdom,dc=example,dc=org"),
        TreeSearchScope.SUBTREE);
    assertThat(actual)
        .hasValue(expected);
  }

  /**
   * Resolve memberships.
   */
  @Test
  void resolveMemberships() {
    DomainGroup group1 = DomainGroup.builder()
        .distinguishedName("CN=group1,CN=Users,dc=samdom,dc=example,dc=org")
        .samAccountName("group1")
        .memberships(List.of("CN=group2,CN=Users,dc=samdom,dc=example,dc=org"))
        .build();
    DomainGroup group2 = DomainGroup.builder()
        .distinguishedName("CN=group2,CN=Users,dc=samdom,dc=example,dc=org")
        .samAccountName("group2")
        .memberships(List.of("CN=group3,CN=Users,dc=samdom,dc=example,dc=org"))
        .build();
    doReturn(Optional.of(group2))
        .when(domainGroupRepository)
        .findOne("CN=group2,CN=Users,dc=samdom,dc=example,dc=org", null, null);
    DomainGroup group3 = DomainGroup.builder()
        .distinguishedName("CN=group3,CN=Users,dc=samdom,dc=example,dc=org")
        .samAccountName("group3")
        .memberships(List.of())
        .build();
    doReturn(Optional.of(group3))
        .when(domainGroupRepository)
        .findOne("CN=group3,CN=Users,dc=samdom,dc=example,dc=org", null, null);

    String samAccountName = "junit";
    doReturn(Stream.of(group1))
        .when(target)
        .getMemberships(samAccountName, null, null);

    List<DomainGroup> expected = List.of(group1, group2, group3);
    List<DomainGroup> actual = target
        .resolveMemberships(samAccountName, null, null)
        .toList();
    assertThat(actual)
        .containsExactlyInAnyOrderElementsOf(expected);
  }

  /**
   * Gets member selection.
   *
   * @param softly the softly
   */
  @Test
  void getMemberSelection(SoftAssertions softly) {
    String groupName = "domainGroup";
    DomainGroup domainGroup = DomainGroup.builder()
        .distinguishedName("CN=domainGroup,CN=Users,dc=samdom,dc=example,dc=org")
        .samAccountName(groupName)
        .build();
    doReturn(Optional.of(domainGroup))
        .when(domainGroupRepository)
        .findOne(groupName, null, null);

    DomainGroupMember user = DomainGroupMember.builder()
        .samAccountName("user")
        .distinguishedName("CN=user,CN=Users,dc=samdom,dc=example,dc=org")
        .memberType(DomainGroupMemberType.USER)
        .displayName("user")
        .build();
    DomainGroupMember group = DomainGroupMember.builder()
        .samAccountName("group")
        .distinguishedName("CN=group,CN=Users,dc=samdom,dc=example,dc=org")
        .memberType(DomainGroupMemberType.GROUP)
        .displayName("group")
        .build();
    DomainGroupMember computer = DomainGroupMember.builder()
        .samAccountName("computer")
        .distinguishedName("CN=computer,CN=Users,dc=samdom,dc=example,dc=org")
        .memberType(DomainGroupMemberType.COMPUTER)
        .displayName("computer")
        .build();
    doAnswer(invocationOnMock -> Stream.of(user, group, computer))
        .when(ldapOperations)
        .findAll(any(), any());

    List<DomainGroupMemberType> types = List.of();
    List<DomainGroupMember> actual = target
        .getMemberSelection(groupName, null, null, null, types, true)
        .toList();
    softly
        .assertThat(actual)
        .containsExactlyInAnyOrder(user, computer, group);

    types = List.of(
        DomainGroupMemberType.USER,
        DomainGroupMemberType.GROUP,
        DomainGroupMemberType.COMPUTER);
    actual = target
        .getMemberSelection(groupName, null, null, "group", types, true)
        .toList();
    softly
        .assertThat(actual)
        .containsExactlyInAnyOrder(user, computer, group);
  }

  /**
   * Modify members.
   */
  @Test
  void modifyMembers() {
    String groupName = "group";
    String userDn1 = "CN=user1,CN=Users,dc=samdom,dc=example,dc=org";
    DomainGroup group = DomainGroup.builder()
        .distinguishedName("CN=group,CN=Users,dc=samdom,dc=example,dc=org")
        .samAccountName(groupName)
        .members(List.of(userDn1))
        .build();
    doReturn(Optional.of(group))
        .when(domainGroupRepository)
        .findOne(groupName, null, null);

    String userDn2 = "CN=user2,CN=Users,dc=samdom,dc=example,dc=org";
    SamAccount samAccount2 = SamAccountIntermediate.builder()
        .distinguishedName(userDn2)
        .samAccountName("user2")
        .primaryGroupId(100)
        .build();
    doReturn(Optional.of(samAccount2))
        .when(target)
        .findSamAccount(userDn2, null, null);
    SamAccount samAccount1 = SamAccountIntermediate.builder()
        .distinguishedName(userDn1)
        .samAccountName("user1")
        .primaryGroupId(100)
        .build();
    doReturn(Optional.of(samAccount1))
        .when(target)
        .findSamAccount(userDn1, null, null);

    DomainGroup expected = DomainGroup.builder()
        .from(group)
        .members(List.of(userDn2))
        .build();
    doReturn(expected)
        .when(domainGroupRepository)
        .save(expected);

    Set<String> add = Set.of(userDn2);
    Set<String> remove = Set.of(userDn1);
    DomainGroup actual = target.modifyMembers(groupName, null, null, add, remove);
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Modify no members.
   */
  @Test
  void modifyNoMembers() {
    String groupName = "group";
    String userDn1 = "CN=user1,CN=Users,dc=samdom,dc=example,dc=org";
    DomainGroup group = DomainGroup.builder()
        .distinguishedName("CN=group,CN=Users,dc=samdom,dc=example,dc=org")
        .samAccountName(groupName)
        .members(List.of(userDn1))
        .build();
    doReturn(Optional.of(group))
        .when(domainGroupRepository)
        .findOne(groupName, null, null);
    doReturn(group)
        .when(domainGroupRepository)
        .save(group);
    Set<String> add = Set.of();
    Set<String> remove = Set.of();
    DomainGroup actual = target.modifyMembers(groupName, null, null, add, remove);
    assertThat(actual)
        .isEqualTo(group);
  }

}