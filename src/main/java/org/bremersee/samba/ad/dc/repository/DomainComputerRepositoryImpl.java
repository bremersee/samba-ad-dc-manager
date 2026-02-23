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

import static java.util.Objects.isNull;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.misc.TreeSearchScopeConverter;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.ldaptive.DeleteRequest;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.OrFilter;
import org.ldaptive.filter.SubstringFilter;
import org.springframework.stereotype.Component;

/**
 * The domain computer repository implementation.
 *
 * @author Christian Bremer
 */
@Component("domainComputerRepository")
@Slf4j
public class DomainComputerRepositoryImpl extends SamAccountRepository
    implements DomainComputerRepository {

  private final LdaptiveEntryMapper<DomainComputer> domainComputerLdapMapper;

  /**
   * Instantiates a new domain computer repository.
   *
   * @param properties the properties
   * @param ldapOperations the ldap operations
   * @param domainComputerLdapMapper the domain computer ldap mapper
   */
  public DomainComputerRepositoryImpl(
      ApplicationProperties properties,
      LdaptiveOperations ldapOperations,
      LdaptiveEntryMapper<DomainComputer> domainComputerLdapMapper) {
    super(properties, ldapOperations);
    this.domainComputerLdapMapper = domainComputerLdapMapper;
  }

  @Override
  protected Dn getDefaultOu() {
    return new Dn(getProperties().getComputer().getDefaultOu());
  }

  @Override
  protected String getObjectClassValue() {
    return AdConstants.OBJECT_CLASS_COMPUTER;
  }

  @Override
  protected String[] getBinaryAttributes() {
    return domainComputerLdapMapper.getBinaryAttributeNames();
  }

  @Override
  protected String[] getReturnAttributes() {
    return domainComputerLdapMapper.getMappedAttributeNames();
  }

  /**
   * Gets find all filter.
   *
   * @param query the query
   * @return the find all filter
   */
  private Filter getFindAllFilter(String query) {
    Filter objectClassFilter = objectClassFilter();
    if (isNull(query) || query.length() < getProperties().getComputer().getMinQueryLength()) {
      return objectClassFilter;
    }
    Filter orFilter = new OrFilter(
        new SubstringFilter(
            AdConstants.DESCRIPTION.getName(), null, null, query),
        new SubstringFilter(
            AdConstants.SAM_ACCOUNT_NAME.getName(), null, null, query),
        new SubstringFilter(
            AdConstants.NAME.getName(), null, null, query),
        new SubstringFilter(
            AdConstants.COMPUTER_NETWORK_ADDRESS.getName(), null, null, query),
        new SubstringFilter(
            AdConstants.COMPUTER_OPERATING_SYSTEM.getName(), null, null, query),
        new SubstringFilter(
            AdConstants.COMPUTER_OPERATING_SYSTEM_VERSION.getName(), null, null, query)
    );
    return new AndFilter(objectClassFilter, orFilter);
  }

  @Override
  public Stream<DomainComputer> findAll(String query, Dn ou, TreeSearchScope searchScope) {
    log.debug("findAll({}, {}, {})", query, ou, searchScope);
    SearchScope scope = TreeSearchScopeConverter.toSearchScope(searchScope);
    SearchRequest searchRequest = searchAllRequest(
        ou,
        getFindAllFilter(query),
        scope,
        getReturnAttributes());
    return getLdapOperations()
        .findAll(searchRequest, domainComputerLdapMapper)
        .filter(getIgnoredObjectFilter(ou, scope));
  }

  @Override
  public Optional<DomainComputer> findOne(String name, Dn ou, TreeSearchScope searchScope) {
    log.debug("findOne({})", name);
    String samAccountName;
    if (!name.endsWith("$")) {
      samAccountName = name + "$";
    } else {
      samAccountName = name;
    }
    SearchScope scope = TreeSearchScopeConverter.toSearchScope(searchScope);
    SearchRequest searchRequest = searchOneRequest(samAccountName, ou, scope);
    log.debug("findOne, searchRequest = {}", searchRequest);
    return getLdapOperations()
        .findOne(searchRequest, domainComputerLdapMapper)
        .filter(getIgnoredObjectFilter(ou, scope));
  }

  @Override
  public DomainComputer update(DomainComputer domainComputer, Dn newOu) {
    log.debug("update({}, {})", domainComputer.getSamAccountName(), newOu);
    DomainComputer existingComputer = findOne(
        domainComputer.getSamAccountName(), null, null)
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            DomainComputer.class.getSimpleName(),
            domainComputer.getSamAccountName(),
            EC_SAM_ACCOUNT_NOT_FOUND));
    DomainComputer computer = domainComputer
        .withDistinguishedName(existingComputer.getDistinguishedName())
        .withCreated(existingComputer.getCreated())
        .withModified(OffsetDateTime.now())
        .withSid(existingComputer.getSid())
        .withCriticalSystemObject(existingComputer.isCriticalSystemObject());
    Dn oldDn = new Dn(existingComputer.getDistinguishedName());
    Dn newDn = new Dn(oldDn.getRDn());
    newDn.add(validateParentDn(newOu, oldDn::getParent));
    if (!oldDn.isSame(newDn) && dnExistsWithAnyObjectClass(newDn.format())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainComputer.class.getSimpleName(),
          getDnTool().removeBaseDn(newDn),
          EC_DN_ALREADY_EXISTS);
    }
    moveAndRename(oldDn, newDn);
    String newDnStr = DnTool.toString(newDn);
    return getLdapOperations()
        .save(computer.withDistinguishedName(newDnStr), domainComputerLdapMapper);
  }

  @Override
  public boolean delete(String name) {
    log.debug("delete({})", name);
    return findOne(name, null, null)
        .map(domainComputer -> {
          getLdapOperations().delete(DeleteRequest.builder()
              .dn(domainComputer.getDistinguishedName())
              .build());
          return true;
        })
        .orElse(false);
  }

}
