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

package org.bremersee.samba.ad.dc.repository;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.common.converter.TreeSearchScopeConverter;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainGroupMember;
import org.bremersee.samba.ad.dc.model.DomainGroupMemberType;
import org.bremersee.samba.ad.dc.samaccount.common.model.SamAccount;
import org.bremersee.samba.ad.dc.common.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.samaccount.group.repository.mapper.DomainGroupMemberLdapMapper;
import org.bremersee.samba.ad.dc.samaccount.common.repository.SamAccountRepository;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.EqualityFilter;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.OrFilter;
import org.ldaptive.filter.SubstringFilter;
import org.springframework.stereotype.Component;

/**
 * The domain group member repository.
 *
 * @author Christian Bremer
 */
@Component("domainGroupMemberRepository")
@Slf4j
public class DomainGroupMemberRepositoryImpl extends SamAccountRepository
    implements DomainGroupMemberRepository {

  private final DomainGroupRepository domainGroupRepository;

  private final DomainGroupMemberLdapMapper domainGroupMemberLdapMapper;

  /**
   * Instantiates a new domain group repository.
   *
   * @param properties the properties
   * @param ldapTemplate the ldap template
   */
  public DomainGroupMemberRepositoryImpl(
      DomainControllerProperties properties,
      LdaptiveTemplate ldapTemplate,
      DomainGroupRepository domainGroupRepository) {
    super(properties, ldapTemplate);
    this.domainGroupRepository = domainGroupRepository;
    this.domainGroupMemberLdapMapper = new DomainGroupMemberLdapMapper();
  }

  @Override
  protected Dn getDefaultOu() {
    return getProperties().getGroup().getDefaultOu();
  }

  @Override
  protected String getObjectClassValue() {
    return AdConstants.OBJECT_CLASS_GROUP;
  }

  @Override
  protected String[] getBinaryAttributes() {
    return domainGroupMemberLdapMapper.getBinaryAttributeNames();
  }

  @Override
  protected String[] getReturnAttributes() {
    return domainGroupMemberLdapMapper.getMappedAttributeNames();
  }

  @Override
  public Stream<DomainGroup> resolveMemberships(
      String samAccountName, Dn ou, TreeSearchScope searchScope) {
    log.debug("resolveMemberships({}, {}, {})", samAccountName, ou, searchScope);
    Set<Dn> groupDns = new HashSet<>();
    return getMemberships(samAccountName, ou, searchScope)
        .filter(group -> !groupDns.contains(new Dn(group.getDistinguishedName())))
        .peek(group -> groupDns.add(new Dn(group.getDistinguishedName())))
        .flatMap(group -> Stream
            .concat(Stream.of(group), resolveMemberships(group.getMemberships(), groupDns)));
  }

  private Stream<DomainGroup> resolveMemberships(List<String> memberOf, Set<Dn> groupDns) {
    return memberOf.stream()
        .filter(dn -> !groupDns.contains(new Dn(dn)))
        .flatMap(dn -> domainGroupRepository.findOne(dn, null, null).stream())
        .peek(group -> groupDns.add(new Dn(group.getDistinguishedName())))
        .flatMap(nextGroup -> Stream
            .concat(Stream.of(nextGroup),
                resolveMemberships(nextGroup.getMemberships(), groupDns)));
  }

  @Override
  public Stream<DomainGroup> getMemberships(
      String samAccountName, Dn ou, TreeSearchScope searchScope) {

    log.debug("getMemberships({}, {}, {})", samAccountName, ou, searchScope);
    SamAccount samAccount = findSamAccount(samAccountName, ou, searchScope)
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            SamAccount.class.getSimpleName(), samAccountName, EC_SAM_ACCOUNT_NOT_FOUND));
    Stream<DomainGroup> groups = samAccount.getMemberships().stream()
        .flatMap(dn -> domainGroupRepository.findOne(dn, null, null).stream())
        .sorted();
    Optional<DomainGroup> primaryGroup = domainGroupRepository
        .findOneByPrimaryGroupId(samAccount.getPrimaryGroupId());
    if (primaryGroup.isPresent() && equals(primaryGroup.get(), samAccount)) {
      return groups;
    }
    return Stream.concat(primaryGroup.stream(), groups);
  }

  private boolean equals(SamAccount samAccount1, SamAccount samAccount2) {
    boolean result = Objects.equals(samAccount1, samAccount2);
    if (result) {
      return true;
    }
    result = Objects.equals(samAccount1.getSamAccountName(), samAccount2.getSamAccountName());
    if (result) {
      return true;
    }
    return Objects.equals(
        samAccount1.getSamAccountName().toLowerCase(),
        samAccount2.getSamAccountName().toLowerCase());
  }

  @Override
  public Stream<DomainGroupMember> getPossibleMembers(
      String groupName,
      Dn ou,
      TreeSearchScope searchScope,
      String query,
      Set<DomainGroupMemberType> memberTypes) {
    return findPossibleMembers(groupName, ou, searchScope, memberTypes, query);
  }

  @Override
  public Stream<DomainGroupMember> findPossibleMembers(
      String groupName, Dn ou, TreeSearchScope searchScope) {
    log.debug("findPossibleMembers({}, {}, {})", groupName, ou, searchScope);
    return findPossibleMembers(groupName, ou, searchScope, null, null);
  }

  @Override
  public Stream<DomainGroupMember> queryPossibleMembers(String groupName, Dn ou,
      TreeSearchScope searchScope, String query) {
    log.debug("queryPossibleMembers({}, {}, {}, {})", groupName, ou, searchScope, query);
    if (isEmpty(query) || query.length() <= 2) {
      return Stream.empty();
    }
    return findPossibleMembers(groupName, ou, searchScope, null, query);
  }

  @Override
  public Stream<DomainGroupMember> getMembers(
      String groupName,
      Dn ou,
      TreeSearchScope searchScope) {
    DomainGroup group = domainGroupRepository.findOne(groupName, ou, searchScope)
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            DomainGroup.class.getSimpleName(), groupName, EC_SAM_ACCOUNT_NOT_FOUND));
    return findMembers(new HashSet<>(group.getMembers()), null);
  }

  Stream<DomainGroupMember> findMembers(Set<String> memberDnSet, String query) {

    log.debug("findMembers({})", memberDnSet);
    String[] returnAttributes = domainGroupMemberLdapMapper.getMappedAttributeNames();
    return memberDnSet.stream()
        .flatMap(dn -> getLdapTemplate()
            .findOne(SearchRequest.objectScopeSearchRequest(dn, returnAttributes))
            .stream())
        .map(ldapEntry -> DomainGroupMember.builder()
            .from(domainGroupMemberLdapMapper.map(ldapEntry))
            .selected(true)
            .build())
        .filter(member -> query(member, query));
  }

  boolean query(DomainGroupMember member, String query) {
    if (isEmpty(query) || query.length() <= 2) {
      return true;
    }
    String lowerQuery = query.toLowerCase();
    String samAccountName = Objects.requireNonNullElse(member.getSamAccountName(), "")
        .toLowerCase();
    String displayName = Objects.requireNonNullElse(member.getDisplayName(), "").toLowerCase();
    return samAccountName.contains(lowerQuery)
        || displayName.toLowerCase().contains(lowerQuery);
  }

  Stream<DomainGroupMember> findPossibleMembers(
      String groupName,
      Dn ou,
      TreeSearchScope searchScope,
      Set<DomainGroupMemberType> memberTypes,
      String query) {

    DomainGroup group = domainGroupRepository.findOne(groupName, ou, searchScope)
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            DomainGroup.class.getSimpleName(), groupName, EC_SAM_ACCOUNT_NOT_FOUND));
    Set<String> memberDns = group.getMembers().stream()
        .map(Dn::new)
        .map(Dn::format)
        .collect(Collectors.toSet());
    Set<DomainGroupMemberType> types = isEmpty(memberTypes)
        ? Set.of(DomainGroupMemberType.values())
        : memberTypes;
    return Stream
        .concat(
            findMembers(memberDns, query),
            findPossibleMembers(memberDns, group.getPrimaryGroupId(), query))
        .filter(member -> types.contains(member.getMemberType()));
  }

  Stream<DomainGroupMember> findPossibleMembers(
      Set<String> excludedDns,
      Integer groupId,
      String query) {
    log.debug("findPossibleMembers({})", excludedDns);
    String[] returnAttributes = domainGroupMemberLdapMapper.getMappedAttributeNames();
    Filter findAllMembersFilter;
    Filter objectClassFilter = new OrFilter(
        new EqualityFilter(AdConstants.OBJECT_CLASS.getName(), AdConstants.OBJECT_CLASS_GROUP),
        new EqualityFilter(AdConstants.OBJECT_CLASS.getName(), AdConstants.OBJECT_CLASS_USER)
        // computers are also users
    );
    if (isEmpty(query) || query.length() <= 2) {
      findAllMembersFilter = objectClassFilter;
    } else {
      Filter queryFilter = new OrFilter(
          new SubstringFilter(AdConstants.SAM_ACCOUNT_NAME.getName(), null, null, query),
          new SubstringFilter(AdConstants.USER_GIVEN_NAME.getName(), null, null, query),
          new SubstringFilter(AdConstants.USER_SN.getName(), null, null, query),
          new SubstringFilter(AdConstants.USER_DISPLAY_NAME.getName(), null, null, query),
          new SubstringFilter(AdConstants.NAME.getName(), null, null, query)
      );
      findAllMembersFilter = new AndFilter(objectClassFilter, queryFilter);
    }
    SearchRequest searchRequest = searchAllRequest(getProperties().getBaseDn(),
        findAllMembersFilter, SearchScope.SUBTREE, returnAttributes);
    return getLdapTemplate().findAll(searchRequest)
        .stream()
        .filter(getIgnoredEntryFilter())
        .filter(ldapEntry -> !excludedDns.contains(new Dn(ldapEntry.getDn()).format()))
        .map(ldapEntry -> DomainGroupMember.builder()
            .from(domainGroupMemberLdapMapper.map(ldapEntry))
            .selected(false)
            .build())
        .filter(member -> isEmpty(groupId)
            || !groupId.equals(member.getPrimaryGroupId()));
  }

  Optional<SamAccount> findSamAccount(String samAccountName, Dn ou, TreeSearchScope searchScope) {
    String[] returnAttributes = domainGroupMemberLdapMapper.getMappedAttributeNames();
    SearchRequest searchRequest;
    if (getProperties().isDn(samAccountName)) {
      searchRequest = SearchRequest.objectScopeSearchRequest(samAccountName, returnAttributes);
    } else {
      Dn ouDn;
      SearchScope scope;
      if (isEmpty(ou) || ou.isEmpty()) {
        ouDn = getProperties().getBaseDn();
        scope = SearchScope.SUBTREE;
      } else {
        ouDn = getProperties().getBaseDn(ou);
        scope = requireNonNullElse(TreeSearchScopeConverter
            .toSearchScope(searchScope), SearchScope.SUBTREE);
      }
      Filter filter = new EqualityFilter(AdConstants.SAM_ACCOUNT_NAME.getName(), samAccountName);
      searchRequest = searchOneRequest(samAccountName, ouDn, filter, scope, returnAttributes);
    }
    return getLdapTemplate().findOne(searchRequest)
        .filter(getIgnoredEntryFilter(ou, TreeSearchScopeConverter.toSearchScope(searchScope)))
        .map(domainGroupMemberLdapMapper::map);
  }

  @Override
  public DomainGroup modifyMembers(
      String groupName,
      Dn ou,
      TreeSearchScope searchScope,
      Set<String> membersToAdd,
      Set<String> membersToRemove) {

    return domainGroupRepository.findOne(groupName, ou, searchScope)
        .map(group -> modifyMembers(group, membersToAdd, membersToRemove))
        .map(domainGroupRepository::save)
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            DomainGroup.class.getSimpleName(),
            groupName,
            EC_SAM_ACCOUNT_NOT_FOUND));
  }

  private DomainGroup modifyMembers(
      DomainGroup group,
      Set<String> membersToAdd,
      Set<String> membersToRemove) {

    if (isEmpty(membersToAdd) && isEmpty(membersToRemove)) {
      return group;
    }
    Set<DnPair> members = group.getMembers().stream()
        .map(dn -> new DnPair(dn, new Dn(dn).format()))
        .collect(Collectors.toCollection(LinkedHashSet::new));
    Set<DnPair> add = Stream.ofNullable(membersToAdd)
        .flatMap(Collection::stream)
        .filter(member -> !isEmpty(member))
        .flatMap(member -> findDnOfSamAccountName(member).stream())
        .map(dn -> new DnPair(dn, new Dn(dn).format()))
        .collect(Collectors.toSet());
    members.addAll(add);
    Set<DnPair> remove = Stream.ofNullable(membersToRemove)
        .flatMap(Collection::stream)
        .filter(member -> !isEmpty(member))
        .flatMap(member -> findDnOfSamAccountName(member).stream())
        .map(dn -> new DnPair(dn, new Dn(dn).format()))
        .collect(Collectors.toSet());
    members.removeAll(remove);
    return DomainGroup.builder()
        .from(group)
        .members(members.stream().map(DnPair::dn).toList())
        .build();
  }

  private record DnPair(String dn, String formattedDn) {

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      DnPair dnPair = (DnPair) o;
      return Objects.equals(formattedDn, dnPair.formattedDn);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(formattedDn);
    }
  }

}
