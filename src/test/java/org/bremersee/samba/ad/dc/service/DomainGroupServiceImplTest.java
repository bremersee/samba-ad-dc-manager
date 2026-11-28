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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainGroupMember;
import org.bremersee.samba.ad.dc.model.DomainGroupMemberType;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.repository.DomainGroupMemberRepository;
import org.bremersee.samba.ad.dc.repository.DomainGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ldaptive.dn.Dn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * The domain group service implementation test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class DomainGroupServiceImplTest {

  private static final Dn OU = new Dn("CN=Users,DC=samdom,DC=example,DC=org");

  private DomainGroupRepository groupRepository;

  private DomainGroupMemberRepository memberRepository;

  private DomainGroupServiceImpl target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    groupRepository = mock(DomainGroupRepository.class);
    memberRepository = mock(DomainGroupMemberRepository.class);
    target = new DomainGroupServiceImpl(
        SortMapper.defaultSortMapper(), groupRepository, memberRepository);
  }

  private static DomainGroup group(String name) {
    return DomainGroup.builder()
        .distinguishedName("CN=" + name + "," + OU.format())
        .samAccountName(name)
        .build();
  }

  /**
   * Gets groups.
   *
   * @param softly the softly
   */
  @Test
  void getGroups(SoftAssertions softly) {
    DomainGroup a = group("a");
    DomainGroup b = group("b");
    DomainGroup c = group("c");
    doAnswer(invocation -> Stream.of(b, c, a))
        .when(groupRepository)
        .findAll("q", OU, TreeSearchScope.ONELEVEL);
    Page<DomainGroup> actual = target.getGroups(
        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "samAccountName")),
        "q", OU, TreeSearchScope.ONELEVEL);
    softly.assertThat(actual.getTotalElements()).isEqualTo(3L);
    softly.assertThat(actual.getContent()).containsExactly(c, b, a);
  }

  /**
   * Gets memberships.
   */
  @Test
  void getMemberships() {
    DomainGroup a = group("a");
    doAnswer(invocation -> Stream.of(a))
        .when(memberRepository)
        .getMemberships("user", OU, TreeSearchScope.SUBTREE);
    assertThat(target.getMemberships("user", OU, TreeSearchScope.SUBTREE))
        .containsExactly(a);
  }

  /**
   * Resolve memberships.
   */
  @Test
  void resolveMemberships() {
    DomainGroup a = group("a");
    DomainGroup b = group("b");
    doAnswer(invocation -> Stream.of(a, b))
        .when(memberRepository)
        .resolveMemberships("user", OU, TreeSearchScope.SUBTREE);
    assertThat(target.resolveMemberships("user", OU, TreeSearchScope.SUBTREE))
        .containsExactly(a, b);
  }

  /**
   * Gets member selection.
   *
   * @param softly the softly
   */
  @Test
  void getMemberSelection(SoftAssertions softly) {
    DomainGroupMember m1 = DomainGroupMember.builder()
        .samAccountName("anna")
        .displayName("Anna")
        .memberType(DomainGroupMemberType.USER)
        .build();
    DomainGroupMember m2 = DomainGroupMember.builder()
        .samAccountName("bert")
        .displayName("Bert")
        .memberType(DomainGroupMemberType.USER)
        .build();
    List<DomainGroupMemberType> types = List.of(DomainGroupMemberType.USER);
    doAnswer(invocation -> Stream.of(m2, m1))
        .when(memberRepository)
        .getMemberSelection("group", OU, TreeSearchScope.ONELEVEL, "q", types, true);
    Page<DomainGroupMember> actual = target.getMemberSelection(
        PageRequest.of(0, 1, Sort.by("samAccountName")),
        "q", types, true, "group", OU, TreeSearchScope.ONELEVEL);
    softly.assertThat(actual.getTotalElements()).isEqualTo(2L);
    softly.assertThat(actual.getContent()).containsExactly(m1);
  }

  /**
   * Modify members.
   */
  @Test
  void modifyMembers() {
    DomainGroup expected = group("group");
    Set<String> add = Set.of("anna");
    Set<String> remove = Set.of("bert");
    doReturn(expected)
        .when(memberRepository)
        .modifyMembers("group", OU, TreeSearchScope.ONELEVEL, add, remove);
    assertThat(target.modifyMembers("group", OU, TreeSearchScope.ONELEVEL, add, remove))
        .isEqualTo(expected);
  }

  /**
   * Add group.
   */
  @Test
  void addGroup() {
    DomainGroup source = group("group");
    doReturn(source).when(groupRepository).add(source, OU);
    assertThat(target.addGroup(source, OU))
        .isEqualTo(source);
    verify(groupRepository).add(source, OU);
  }

  /**
   * Gets group.
   *
   * @param softly the softly
   */
  @Test
  void getGroup(SoftAssertions softly) {
    DomainGroup expected = group("group");
    doReturn(Optional.of(expected))
        .when(groupRepository)
        .findOne("group", OU, TreeSearchScope.ONELEVEL);
    doReturn(Optional.of(expected))
        .when(groupRepository)
        .findOneByPrimaryGroupId(513);
    doReturn(Optional.of(expected))
        .when(groupRepository)
        .findOneByGidNumber(100);
    softly.assertThat(target.getGroup("group", OU, TreeSearchScope.ONELEVEL))
        .hasValue(expected);
    softly.assertThat(target.getGroupByPrimaryGroupId(513))
        .hasValue(expected);
    softly.assertThat(target.getGroupByUnixGroupId(100))
        .hasValue(expected);
    softly.assertThat(target.getGroupByUnixGroupId(101))
        .isEmpty();
  }

  /**
   * Update group.
   */
  @Test
  void updateGroup() {
    DomainGroup source = group("group");
    DomainGroup expected = group("renamed");
    doReturn(expected).when(groupRepository).update("group", source, OU);
    assertThat(target.updateGroup("group", source, OU))
        .isEqualTo(expected);
  }

  /**
   * Delete group.
   *
   * @param softly the softly
   */
  @Test
  void deleteGroup(SoftAssertions softly) {
    doReturn(true).when(groupRepository).delete("group");
    softly.assertThat(target.deleteGroup("group")).isTrue();
    softly.assertThat(target.deleteGroup("other")).isFalse();
  }

}
