/*
 * Copyright 2019-2020 the original author or authors.
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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.pagebuilder.PageBuilder;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainGroupMember;
import org.bremersee.samba.ad.dc.model.DomainGroupMemberType;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.repository.DomainGroupMemberRepository;
import org.bremersee.samba.ad.dc.repository.DomainGroupRepository;
import org.ldaptive.dn.Dn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * The domain group service.
 *
 * @author Christian Bremer
 */
@Component("domainGroupService")
@Slf4j
public class DomainGroupServiceImpl implements DomainGroupService {

  private final SortMapper sortMapper;

  private final DomainGroupRepository domainGroupRepository;

  private final DomainGroupMemberRepository domainGroupMemberRepository;

  /**
   * Instantiates a new domain group service.
   *
   * @param domainGroupRepository the domain group repository
   */
  public DomainGroupServiceImpl(
      SortMapper sortMapper,
      DomainGroupRepository domainGroupRepository,
      DomainGroupMemberRepository domainGroupMemberRepository) {
    this.sortMapper = sortMapper;
    this.domainGroupRepository = domainGroupRepository;
    this.domainGroupMemberRepository = domainGroupMemberRepository;
  }

  @Override
  public Page<DomainGroup> getGroups(Pageable pageable, String query, Dn ou,
      TreeSearchScope scope) {
    return new PageBuilder<DomainGroup, DomainGroup>()
        .sourceEntries(domainGroupRepository.findAll(query, ou, scope))
        .pageable(sortMapper.applyDefaults(pageable, null, true, null))
        .build();
  }

  @Override
  public Stream<DomainGroup> getMemberships(String samAccountName, Dn ou,
      TreeSearchScope searchScope) {
    return domainGroupMemberRepository.getMemberships(samAccountName, ou, searchScope);
  }

  @Override
  public Stream<DomainGroup> resolveMemberships(String samAccountName, Dn ou,
      TreeSearchScope searchScope) {
    return domainGroupMemberRepository.resolveMemberships(samAccountName, ou, searchScope);
  }

  @Override
  public Page<DomainGroupMember> getMemberSelection(
      Pageable pageable,
      String query,
      Collection<DomainGroupMemberType> memberTypes,
      boolean withPrimaryMembers,
      String groupName,
      Dn ou,
      TreeSearchScope searchScope) {

    return new PageBuilder<DomainGroupMember, DomainGroupMember>()
        .sourceEntries(domainGroupMemberRepository
            .getMemberSelection(groupName, ou, searchScope, query, memberTypes, withPrimaryMembers))
        .pageable(sortMapper.applyDefaults(pageable, null, true, null))
        .build();
  }

  @Override
  public DomainGroup modifyMembers(String groupName, Dn ou, TreeSearchScope searchScope,
      Set<String> membersToAdd, Set<String> membersToRemove) {
    return domainGroupMemberRepository
        .modifyMembers(groupName, ou, searchScope, membersToAdd, membersToRemove);
  }


  @Override
  public DomainGroup addGroup(DomainGroup domainGroup, Dn dn) {
    return domainGroupRepository.add(domainGroup, dn);
  }

  @Override
  public Optional<DomainGroup> getGroup(String groupName, Dn ou, TreeSearchScope scope) {
    return domainGroupRepository.findOne(groupName, ou, scope);
  }

  @Override
  public Optional<DomainGroup> getGroupByPrimaryGroupId(Integer primaryGroupId) {
    return domainGroupRepository.findOneByPrimaryGroupId(primaryGroupId);
  }

  @Override
  public DomainGroup updateGroup(String groupName, DomainGroup domainGroup, Dn newOu) {
    log.debug("updateGroup({}, {}, {})", groupName, domainGroup.getSamAccountName(), newOu);
    return domainGroupRepository.update(groupName, domainGroup, newOu);
  }

  @Override
  public Boolean deleteGroup(String groupName) {
    return domainGroupRepository.delete(groupName);
  }
}
