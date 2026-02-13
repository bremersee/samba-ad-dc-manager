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

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.misc.TreeSearchScopeConverter;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.ldaptive.DeleteRequest;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.ad.SecurityIdentifier;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.NameValue;
import org.ldaptive.dn.RDn;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.EqualityFilter;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.OrFilter;
import org.ldaptive.filter.SubstringFilter;
import org.springframework.stereotype.Component;

/**
 * The domain group repository.
 *
 * @author Christian Bremer
 */
@Component("domainGroupRepository")
@Slf4j
public class DomainGroupRepositoryImpl extends SamAccountRepository
    implements DomainGroupRepository {

  private final DomainRepository domainRepository;

  private final LdaptiveEntryMapper<DomainGroup> domainGroupLdapMapper;

  /**
   * Instantiates a new domain group repository.
   *
   * @param properties the properties
   * @param ldapOperations the ldap operations
   */
  public DomainGroupRepositoryImpl(
      ApplicationProperties properties,
      LdaptiveOperations ldapOperations,
      DomainRepository domainRepository,
      LdaptiveEntryMapper<DomainGroup> domainGroupLdapMapper) {
    super(properties, ldapOperations);
    this.domainRepository = domainRepository;
    this.domainGroupLdapMapper = domainGroupLdapMapper;
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
    return domainGroupLdapMapper.getBinaryAttributeNames();
  }

  @Override
  protected String[] getReturnAttributes() {
    return domainGroupLdapMapper.getMappedAttributeNames();
  }

  Filter getFindAllFilter(String query) {
    Filter objectClassFilter = new EqualityFilter(
        AdConstants.OBJECT_CLASS.getName(),
        getObjectClassValue());
    if (isNull(query) || query.length() < getProperties().getGroup().getMinQueryLength()) {
      return objectClassFilter;
    }
    Filter orFilter = new OrFilter(
        new SubstringFilter(AdConstants.SAM_ACCOUNT_NAME.getName(), null, null, query),
        new SubstringFilter(AdConstants.DESCRIPTION.getName(), null, null, query),
        new SubstringFilter(AdConstants.MAIL.getName(), null, null, query),
        new EqualityFilter(AdConstants.GID_NUMBER.getName(), query)
    );
    return new AndFilter(objectClassFilter, orFilter);
  }

  @Override
  public Stream<DomainGroup> findAll(String query, Dn ou, TreeSearchScope searchScope) {
    log.debug("findAll({}, {}, {})", query, ou, searchScope);
    SearchScope scope = TreeSearchScopeConverter.toSearchScope(searchScope);
    SearchRequest searchRequest = searchAllRequest(
        ou,
        getFindAllFilter(query),
        scope,
        getReturnAttributes());
    return getLdapOperations()
        .findAll(searchRequest, domainGroupLdapMapper)
        .filter(getIgnoredObjectFilter(ou, scope));
  }

  @Override
  public Optional<DomainGroup> findOne(String groupName, Dn ou, TreeSearchScope searchScope) {
    SearchScope scope = TreeSearchScopeConverter.toSearchScope(searchScope);
    SearchRequest searchRequest = searchOneRequest(groupName, ou, scope);
    return getLdapOperations()
        .findOne(searchRequest, domainGroupLdapMapper)
        .filter(getIgnoredObjectFilter(ou, scope));
  }

  @Override
  public Optional<DomainGroup> findOneByPrimaryGroupId(Integer primaryGroupId) {
    log.debug("findByPrimaryGroupId({})", primaryGroupId);
    return Optional.ofNullable(primaryGroupId)
        .map(id -> domainRepository.getDomainSid() + "-" + primaryGroupId)
        .flatMap(sid -> {
          Filter filter = new AndFilter(
              objectClassFilter(),
              new EqualityFilter(AdConstants.OBJECT_SID.getName(),
                  SecurityIdentifier.toBytes(sid)));
          // SecurityIdentifier.toBytes(sid)
          SearchRequest searchRequest = SearchRequest.builder()
              .dn(getProperties().getBaseDn())
              .filter(filter)
              .scope(SearchScope.SUBTREE)
              .binaryAttributes(getBinaryAttributes())
              .returnAttributes(getReturnAttributes())
              .build();
          return getLdapOperations()
              .findOne(searchRequest, domainGroupLdapMapper)
              .filter(getIgnoredObjectFilter());
        });
  }

  public Optional<DomainGroup> findOneByGidNumber(Integer gidNumber) {
    log.debug("findByGidNumber({})", gidNumber);
    return Optional.ofNullable(gidNumber)
        .flatMap(gid -> {
          Filter filter = new AndFilter(
              objectClassFilter(),
              new EqualityFilter(AdConstants.GID_NUMBER.getName(), gid.toString()));
          SearchRequest searchRequest = SearchRequest.builder()
              .dn(getProperties().getBaseDn())
              .filter(filter)
              .scope(SearchScope.SUBTREE)
              .binaryAttributes(getBinaryAttributes())
              .returnAttributes(getReturnAttributes())
              .build();
          return getLdapOperations()
              .findOne(searchRequest, domainGroupLdapMapper)
              .filter(getIgnoredObjectFilter());
        });
  }

  public boolean existsByGidNumber(Integer gidNumber) {
    log.debug("existsByGidNumber({})", gidNumber);
    return Optional.ofNullable(gidNumber)
        .map(gid -> {
          Filter filter = new AndFilter(
              objectClassFilter(),
              new EqualityFilter(AdConstants.GID_NUMBER.getName(), gid.toString()));
          SearchRequest searchRequest = SearchRequest.builder()
              .dn(getProperties().getBaseDn())
              .filter(filter)
              .scope(SearchScope.SUBTREE)
              .returnAttributes(AdConstants.GID_NUMBER.getName())
              .build();
          return getLdapOperations()
              .findOne(searchRequest)
              .filter(getIgnoredEntryFilter())
              .isPresent();
        })
        .orElse(false);
  }

  @Override
  public DomainGroup add(DomainGroup domainGroup, Dn ou) {
    DomainGroup group = domainGroup
        .withDistinguishedName("")
        .withCreated(null)
        .withModified(null)
        .withSid(null)
        .withCriticalSystemObject(false)
        .withMemberships(List.of())
        .withMembers(List.of());
    validateSamAccountName(group);
    if (samAccountExists(group)) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainGroup.class.getSimpleName(),
          group.getSamAccountName(),
          EC_SAM_ACCOUNT_ALREADY_EXISTS);
    }
    if (existsByGidNumber(group.getGidNumber())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainGroup.class.getSimpleName() + ".gidNumber",
          group.getGidNumber(),
          EC_GID_NUMBER_ALREADY_EXISTS);
    }
    Dn dn = new Dn(new RDn(new NameValue(AdConstants.CN.getName(), group.getSamAccountName())));
    dn.add(validateParentDn(ou, this::getDefaultOu));
    String dnStr = DnTool.toString(dn);
    return getLdapOperations().save(group.withDistinguishedName(dnStr), domainGroupLdapMapper);
  }

  @Override
  public DomainGroup update(String name, DomainGroup domainGroup, Dn newOu) {
    log.debug("update({}, {}, {})", name, domainGroup.getSamAccountName(), newOu);
    validateSamAccountName(domainGroup);
    if (!name.equalsIgnoreCase(domainGroup.getSamAccountName())
        && samAccountNameExists(domainGroup.getSamAccountName())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainGroup.class.getSimpleName(),
          domainGroup.getSamAccountName(),
          EC_SAM_ACCOUNT_ALREADY_EXISTS);
    }
    DomainGroup existingGroup = findOne(name, null, null)
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            DomainGroup.class.getSimpleName(),
            domainGroup.getSamAccountName(),
            EC_SAM_ACCOUNT_NOT_FOUND));
    DomainGroup group = domainGroup
        .withDistinguishedName(existingGroup.getDistinguishedName())
        .withCreated(existingGroup.getCreated())
        .withModified(OffsetDateTime.now())
        .withSid(existingGroup.getSid())
        .withCriticalSystemObject(existingGroup.isCriticalSystemObject())
        .withGroupType(existingGroup.getGroupType())
        .withMemberships(existingGroup.getMemberships())
        .withMembers(existingGroup.getMembers());
    if (!isEmpty(group.getGidNumber())
        && !Objects.equals(group.getGidNumber(), existingGroup.getGidNumber())
        && existsByGidNumber(group.getGidNumber())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainGroup.class.getSimpleName() + ".gidNumber",
          group.getGidNumber(),
          EC_GID_NUMBER_ALREADY_EXISTS);
    }
    Dn oldDn = existingGroup.getDn();
    Dn newDn = new Dn(new RDn(new NameValue(
        oldDn.getRDn().getNameValue().getName(),
        group.getSamAccountName())));
    newDn.add(validateParentDn(newOu, oldDn::getParent));
    if (!newDn.isSame(oldDn) && dnExistsWithAnyObjectClass(newDn.format())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainGroup.class.getSimpleName(),
          getDnTool().removeBaseDn(newDn),
          EC_DN_ALREADY_EXISTS);
    }
    moveAndRename(oldDn, newDn);
    String newDnStr = DnTool.toString(newDn);
    return getLdapOperations().save(group.withDistinguishedName(newDnStr), domainGroupLdapMapper);
  }

  @Override
  public boolean delete(String name) {
    log.debug("delete({})", name);
    return findOne(name, null, null)
        .map(group -> {
          if (group.isCriticalSystemObject()) {
            throw ServiceException.badRequest(
                String.format(
                    "'%s' is a system group. Deletion failed.",
                    group.getSamAccountName()),
                EC_ILLEGAL_SYSTEM_ENTITY_OPERATION);
          }
          getLdapOperations().delete(DeleteRequest.builder()
              .dn(group.getDistinguishedName())
              .build());
          return true;
        })
        .orElse(false);
  }

  @Override
  public DomainGroup save(DomainGroup group) {
    return getLdapOperations().save(group, domainGroupLdapMapper);
  }

}
