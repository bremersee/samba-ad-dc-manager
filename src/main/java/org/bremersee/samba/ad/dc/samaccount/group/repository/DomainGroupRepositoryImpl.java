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

package org.bremersee.samba.ad.dc.samaccount.group.repository;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.common.repository.AdConstants;
import org.bremersee.samba.ad.dc.domain.repository.DomainRepository;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.common.converter.TreeSearchScopeConverter;
import org.bremersee.samba.ad.dc.samaccount.group.model.DomainGroup;
import org.bremersee.samba.ad.dc.common.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.samaccount.group.repository.mapper.DomainGroupLdapMapper;
import org.bremersee.samba.ad.dc.samaccount.common.repository.SamAccountRepository;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
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

  private final DomainGroupLdapMapper domainGroupLdapMapper;

  private final DomainRepository domainRepository;

  private final SambaToolGroup domainGroupTool;

  /**
   * Instantiates a new domain group repository.
   *
   * @param properties the properties
   * @param ldapTemplate the ldap template
   */
  public DomainGroupRepositoryImpl(
      DomainControllerProperties properties,
      LdaptiveTemplate ldapTemplate,
      DomainRepository domainRepository,
      SambaToolGroup domainGroupTool) {
    super(properties, ldapTemplate);
    this.domainRepository = domainRepository;
    this.domainGroupTool = domainGroupTool;
    this.domainGroupLdapMapper = new DomainGroupLdapMapper(this.domainRepository::isRfc2307Enabled);
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
    if (isNull(query) || query.length() <= 2) {
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
    return getLdapTemplate()
        .findAll(searchRequest, domainGroupLdapMapper)
        .filter(getIgnoredObjectFilter(ou, scope))
        .peek(group -> log.debug("Found group: {}", group.getDistinguishedName()));
  }

  @Override
  public Optional<DomainGroup> findOne(String groupName, Dn ou, TreeSearchScope searchScope) {
    SearchScope scope = TreeSearchScopeConverter.toSearchScope(searchScope);
    SearchRequest searchRequest = searchOneRequest(groupName, ou, scope);
    return getLdapTemplate()
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
              new EqualityFilter(AdConstants.OBJECT_SID.getName(), sid));
          SearchRequest searchRequest = SearchRequest.builder()
              .dn(getProperties().getBaseDn().format())
              .filter(filter)
              .scope(SearchScope.SUBTREE)
              .binaryAttributes(getBinaryAttributes())
              .returnAttributes(getReturnAttributes())
              .build();
          return getLdapTemplate()
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
              .dn(getProperties().getBaseDn().format())
              .filter(filter)
              .scope(SearchScope.SUBTREE)
              .binaryAttributes(getBinaryAttributes())
              .returnAttributes(getReturnAttributes())
              .build();
          return getLdapTemplate()
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
              .dn(getProperties().getBaseDn().format())
              .filter(filter)
              .scope(SearchScope.SUBTREE)
              .returnAttributes(new String[]{AdConstants.GID_NUMBER.getName()})
              .build();
          return getLdapTemplate()
              .findOne(searchRequest)
              .filter(getIgnoredEntryFilter())
              .isPresent();
        })
        .orElse(false);
  }

  @Override
  public DomainGroup add(DomainGroup domainGroup, Dn ou) {
    validateSamAccountName(domainGroup);
    if (samAccountExists(domainGroup)) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainGroup.class.getSimpleName(),
          domainGroup.getSamAccountName(),
          EC_SAM_ACCOUNT_ALREADY_EXISTS);
    }
    if (existsByGidNumber(domainGroup.getGidNumber())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainGroup.class.getSimpleName() + ".gidNumber",
          domainGroup.getGidNumber(),
          EC_GID_NUMBER_ALREADY_EXISTS);
    }
    domainGroupTool.addGroup(domainGroup, ou, domainRepository.isRfc2307Enabled());
    return findDnOfSamAccount(domainGroup)
        .map(dn -> getLdapTemplate().save(
            DomainGroup.builder().from(domainGroup).distinguishedName(dn).build(),
            domainGroupLdapMapper))
        .orElseThrow(() -> ServiceException
            .internalServerError(
                String.format("Adding group '%s' failed.", domainGroup.getSamAccountName()),
                EC_ADDING_GROUP_FAILED));
  }

  @Override
  public DomainGroup update(String groupName, DomainGroup domainGroup, Dn newOu) {
    log.debug("update({}, {}, {})", groupName, domainGroup.getSamAccountName(), newOu);
    validateSamAccountName(domainGroup);
    if (!groupName.equalsIgnoreCase(domainGroup.getSamAccountName())
        && samAccountNameExists(domainGroup.getSamAccountName())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainGroup.class.getSimpleName(),
          domainGroup.getSamAccountName(),
          EC_SAM_ACCOUNT_ALREADY_EXISTS);
    }
    DomainGroup existingDomainGroup = findOne(groupName, null, null)
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            DomainGroup.class.getSimpleName(),
            domainGroup.getSamAccountName(),
            EC_SAM_ACCOUNT_NOT_FOUND));
    if (!isEmpty(domainGroup.getGidNumber())
        && !Objects.equals(domainGroup.getGidNumber(), existingDomainGroup.getGidNumber())
        && existsByGidNumber(domainGroup.getGidNumber())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainGroup.class.getSimpleName() + ".gidNumber",
          domainGroup.getGidNumber(),
          EC_GID_NUMBER_ALREADY_EXISTS);
    }
    Dn oldDn = existingDomainGroup.getDn();
    Dn newDn = getNewDn(existingDomainGroup, domainGroup, newOu);
    if (!oldDn.isSame(newDn) && dnExistsWithAnyObjectClass(newDn.format())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainGroup.class.getSimpleName(),
          getProperties().removeBaseDn(newDn),
          EC_DN_ALREADY_EXISTS);
    }
    DomainGroup updatedDomainGroup = domainGroupTool
        .renameAndMoveGroup(existingDomainGroup, domainGroup, newDn);
    return getLdapTemplate().save(updatedDomainGroup, domainGroupLdapMapper);
  }

  Dn getNewDn(DomainGroup oldDomainGroup, DomainGroup newDomainGroup, Dn newOu) {
    Dn newParentDn;
    if (!isEmpty(newOu) && !newOu.isEmpty()) {
      // TODO newParentDn = getProperties().getBaseDn(validateOu(newOu));
      newParentDn = getProperties().getBaseDn(newOu);
    } else {
      newParentDn = oldDomainGroup.getDn().getParent();
    }

    RDn oldRdn = new Dn(oldDomainGroup.getDistinguishedName()).getRDn();
    String newCn = newDomainGroup.getSamAccountName();
    Dn newDn = new Dn(new RDn(new NameValue(oldRdn.getNameValue().getName(), newCn)));
    newDn.add(newParentDn);
    return newDn;
  }

  @Override
  public boolean delete(String groupName) {
    log.debug("delete({})", groupName);
    return findOne(groupName, null, null)
        .map(group -> {
          if (group.isCriticalSystemObject()) {
            throw ServiceException.badRequest(
                String.format(
                    "'%s' is a system group. Deletion failed.",
                    group.getSamAccountName()),
                EC_ILLEGAL_SYSTEM_ENTITY_OPERATION);
          }
          domainGroupTool.deleteGroup(group.getSamAccountName());
          return true;
        })
        .orElse(false);
  }

  @Override
  public DomainGroup save(DomainGroup domainGroup) {
    return getLdapTemplate().save(domainGroup, domainGroupLdapMapper);
  }

}
