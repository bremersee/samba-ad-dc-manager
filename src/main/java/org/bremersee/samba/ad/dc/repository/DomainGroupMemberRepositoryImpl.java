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
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.misc.TreeSearchScopeConverter;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainGroupMember;
import org.bremersee.samba.ad.dc.model.DomainGroupMemberType;
import org.bremersee.samba.ad.dc.model.SamAccount;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.repository.mapper.DomainGroupMemberLdapMapper;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.EqualityFilter;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.NotFilter;
import org.ldaptive.filter.OrFilter;
import org.ldaptive.filter.SubstringFilter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * The domain group member repository.
 *
 * @author Christian Bremer
 */
@Component("domainGroupMemberRepository")
@Validated
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
    return new Dn(getProperties().getGroup().getDefaultOu());
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
  public Stream<DomainGroup> getMemberships(
      String samAccountName, Dn ou, TreeSearchScope searchScope) {

    log.debug("getMemberships({}, {}, {})", samAccountName, ou, searchScope);
    SamAccount samAccount = findSamAccount(samAccountName, ou, searchScope)
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            SamAccount.class.getSimpleName(), samAccountName, EC_SAM_ACCOUNT_NOT_FOUND));
    Stream<DomainGroup> groups = samAccount.getMemberships().stream()
        .flatMap(dn -> domainGroupRepository.findOne(dn, null, null).stream())
        .sorted();
    Optional<DomainGroup> primaryGroup = Optional.ofNullable(samAccount.getPrimaryGroupId())
        .flatMap(domainGroupRepository::findOneByPrimaryGroupId);
    if (primaryGroup.isPresent() && equals(primaryGroup.get(), samAccount)) {
      return groups;
    }
    return Stream.concat(primaryGroup.stream(), groups);
  }

  Optional<SamAccount> findSamAccount(String samAccountName, Dn ou, TreeSearchScope searchScope) {
    String[] returnAttributes = domainGroupMemberLdapMapper.getMappedAttributeNames();
    SearchRequest searchRequest;
    if (getDnTool().isValidDnWithBaseDn(samAccountName)) {
      searchRequest = SearchRequest.objectScopeSearchRequest(samAccountName, returnAttributes);
    } else {
      Dn ouDn;
      SearchScope scope;
      if (!DnTool.isValidDn(ou)) {
        ouDn = getDnTool().getBaseDn();
        scope = SearchScope.SUBTREE;
      } else {
        ouDn = getDnTool().addBaseDn(ou);
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
  public Stream<DomainGroup> resolveMemberships(
      String samAccountName, Dn ou, TreeSearchScope searchScope) {
    log.debug("resolveMemberships({}, {}, {})", samAccountName, ou, searchScope);
    Set<String> groupDns = new HashSet<>();
    UnaryOperator<DomainGroup> addGroupDn = group -> {
      groupDns.add(group.getDistinguishedNameNormalized());
      return group;
    };
    return getMemberships(samAccountName, ou, searchScope)
        .filter(group -> !groupDns.contains(group.getDistinguishedNameNormalized()))
        .map(addGroupDn)
        .flatMap(group -> Stream
            .concat(Stream.of(group), resolveMemberships(group.getMemberships(), groupDns)));
  }

  private Stream<DomainGroup> resolveMemberships(List<String> memberOf, Set<String> groupDns) {
    UnaryOperator<DomainGroup> addGroupDn = group -> {
      groupDns.add(group.getDistinguishedNameNormalized());
      return group;
    };
    return memberOf.stream()
        .filter(dn -> !groupDns.contains(new Dn(dn).format()))
        .flatMap(dn -> domainGroupRepository.findOne(dn, null, null).stream())
        .map(addGroupDn)
        .flatMap(nextGroup -> Stream.concat(
            Stream.of(nextGroup),
            resolveMemberships(nextGroup.getMemberships(), groupDns)));
  }

  @Override
  public Stream<DomainGroupMember> getMemberSelection(
      String groupName,
      Dn ou,
      TreeSearchScope searchScope,
      String query,
      Collection<DomainGroupMemberType> memberTypes,
      boolean withPrimaryMembers) {

    DomainGroup group = domainGroupRepository.findOne(groupName, ou, searchScope)
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            DomainGroup.class.getSimpleName(), groupName, EC_SAM_ACCOUNT_NOT_FOUND));
    Set<String> memberDns = group.getMembers().stream()
        .map(Dn::new)
        .map(Dn::format)
        .collect(Collectors.toSet());
    Set<DomainGroupMemberType> types = isEmpty(memberTypes)
        ? Set.of(DomainGroupMemberType.values())
        : Set.copyOf(memberTypes);
    String[] returnAttributes = domainGroupMemberLdapMapper.getMappedAttributeNames();
    Filter findAllMembersFilter;
    Filter objectClassFilter = getObjectClassFilter(types);
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
    SearchRequest searchRequest = searchAllRequest(getDnTool().getBaseDn(),
        findAllMembersFilter, SearchScope.SUBTREE, returnAttributes);
    return getLdapTemplate().findAll(searchRequest)
        .stream()
        .filter(getIgnoredEntryFilter())
        .map(domainGroupMemberLdapMapper::map)
        .filter(member -> !member.getSamAccountName().equals(group.getSamAccountName()))
        .map(member -> member.withMember(isMember(member, memberDns, group.getPrimaryGroupId())))
        .map(member -> member.withPrimaryMember(isPrimaryMember(member, group.getPrimaryGroupId())))
        .filter(member -> withPrimaryMembers || !member.isPrimaryMember());
  }

  private Filter getObjectClassFilter(Set<DomainGroupMemberType> types) {
    OrFilter orFilter = new OrFilter();
    if (types.contains(DomainGroupMemberType.USER)
        && types.contains(DomainGroupMemberType.COMPUTER)) {
      orFilter.add(new EqualityFilter(
          AdConstants.OBJECT_CLASS.getName(),
          AdConstants.OBJECT_CLASS_USER));
    } else if (types.contains(DomainGroupMemberType.COMPUTER)) {
      orFilter.add(new EqualityFilter(
          AdConstants.OBJECT_CLASS.getName(),
          AdConstants.OBJECT_CLASS_COMPUTER));
    } else if (types.contains(DomainGroupMemberType.USER)) {
      Filter userFilter = new EqualityFilter(
          AdConstants.OBJECT_CLASS.getName(),
          AdConstants.OBJECT_CLASS_USER);
      Filter computerFilter = new EqualityFilter(
          AdConstants.OBJECT_CLASS.getName(),
          AdConstants.OBJECT_CLASS_COMPUTER);
      orFilter.add(new AndFilter(userFilter, new NotFilter(computerFilter)));
    }
    if (types.contains(DomainGroupMemberType.GROUP)) {
      orFilter.add(new EqualityFilter(
          AdConstants.OBJECT_CLASS.getName(),
          AdConstants.OBJECT_CLASS_GROUP));
    }
    return orFilter;
  }

  boolean isMember(DomainGroupMember member, Set<String> memberDns, Integer groupId) {
    return memberDns.contains(member.getDistinguishedNameNormalized())
        || (!isEmpty(groupId) && groupId.equals(member.getPrimaryGroupId()));
  }

  boolean isPrimaryMember(DomainGroupMember member, Integer groupId) {
    return !isEmpty(groupId) && groupId.equals(member.getPrimaryGroupId());
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
    Set<DnPair> add = toDnPairs(group, membersToAdd);
    members.addAll(add);
    Set<DnPair> remove = toDnPairs(group, membersToRemove);
    members.removeAll(remove);
    return DomainGroup.builder()
        .from(group)
        .members(members.stream().map(DnPair::dn).toList())
        .build();
  }

  private Set<DnPair> toDnPairs(DomainGroup group, Set<String> samAccounts) {
    return Stream.ofNullable(samAccounts)
        .flatMap(Collection::stream)
        .filter(member -> !isEmpty(member))
        .flatMap(member -> findSamAccount(member, null, null).stream())
        .filter(member -> !isEmpty(member))
        .filter(member -> !Objects
            .equals(member.getPrimaryGroupId(), group.getPrimaryGroupId()))
        .map(AdEntry::getDistinguishedName)
        .map(dn -> new DnPair(dn, new Dn(dn).format()))
        .collect(Collectors.toSet());
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
