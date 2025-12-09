/*
 * Copyright 2024 the original author or authors.
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

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.repository.mapper.OrganizationalUnitLdapMapper;
import org.bremersee.samba.ad.dc.repository.tools.OrganizationalUnitTool;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.NameValue;
import org.ldaptive.dn.RDn;
import org.ldaptive.filter.EqualityFilter;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.NotFilter;
import org.ldaptive.filter.OrFilter;
import org.springframework.stereotype.Component;

/**
 * The OrganizationalUnitRepositoryImpl.
 *
 * @author Christian Bremer
 */
@Component("organizationalUnitRepository")
@Slf4j
class OrganizationalUnitRepositoryImpl extends AbstractOrganizedEntryRepository
    implements OrganizationalUnitRepository {

  private final LdaptiveEntryMapper<OrganizationalUnit> ouLdapMapper;

  private final OrganizationalUnitTool organizationalUnitTool;

  OrganizationalUnitRepositoryImpl(
      DomainControllerProperties properties,
      LdaptiveTemplate ldapTemplate,
      OrganizationalUnitTool organizationalUnitTool) {
    super(properties, ldapTemplate);
    this.ouLdapMapper = new OrganizationalUnitLdapMapper();
    this.organizationalUnitTool = organizationalUnitTool;
  }

  @Override
  Dn getDefaultOu() {
    return getProperties().getBaseDn();
  }

  @Override
  String getObjectClassValue() {
    return AdConstants.OBJECT_CLASS_OU;
  }

  @Override
  String[] getBinaryAttributes() {
    return ouLdapMapper.getBinaryAttributeNames();
  }

  @Override
  String[] getReturnAttributes() {
    return ouLdapMapper.getMappedAttributeNames();
  }

  @Override
  Filter objectClassFilter() {
    return new OrFilter(
        new EqualityFilter(AdConstants.OBJECT_CLASS.getName(), getObjectClassValue()),
        new EqualityFilter(AdConstants.DN.getName(),
            getProperties().getBaseDn(AdConstants.BASE_DN_USERS).format()),
        new EqualityFilter(AdConstants.DN.getName(),
            getProperties().getBaseDn(AdConstants.BASE_DN_COMPUTERS).format()));
  }

  @Override
  public Stream<OrganizationalUnit> findAll() {
    SearchRequest searchRequest = searchAllRequest(
        null,
        new EqualityFilter(AdConstants.OBJECT_CLASS.getName(), AdConstants.OBJECT_CLASS_OU),
        SearchScope.SUBTREE,
        getReturnAttributes());
    return getLdapTemplate().findAll(searchRequest, ouLdapMapper)
        .filter(getIgnoredObjectFilter());
  }

  @Override
  public Stream<OrganizationalUnit> findAllWithSystemOus() {
    SearchRequest computersSearchRequest = SearchRequest
        .objectScopeSearchRequest(
            getProperties().getBaseDn(AdConstants.BASE_DN_COMPUTERS).format(),
            getReturnAttributes());
    Stream<OrganizationalUnit> stream = getLdapTemplate()
        .findOne(computersSearchRequest, ouLdapMapper)
        .stream();

    SearchRequest usersSearchRequest = SearchRequest
        .objectScopeSearchRequest(
            getProperties().getBaseDn(AdConstants.BASE_DN_USERS).format(),
            getReturnAttributes());
    stream = Stream.concat(
        stream,
        getLdapTemplate().findOne(usersSearchRequest, ouLdapMapper).stream());

    return Stream.concat(stream, findAll());
  }

  @Override
  public Optional<OrganizationalUnit> findOne(Dn ou) {
    log.debug("findOne({})", ou);
    if (ou.isEmpty()) {
      return Optional.empty();
    }
    String dn = getProperties().getBaseDn(ou).format();
    log.debug("findOne, dn = {}", dn);
    new EqualityFilter(AdConstants.OBJECT_CLASS.getName(), getObjectClassValue());
    SearchRequest searchRequest = searchOneRequest(dn);
    return getLdapTemplate()
        .findOne(searchRequest, ouLdapMapper);
  }

  @Override
  public boolean exists(Dn ou) {
    return findOne(ou).isPresent();
  }

  @Override
  public boolean hasChildren(Dn ou) {
    if (isEmpty(ou) || ou.isEmpty()) {
      return false;
    }
    RDn rdn = ou.getRDn();
    Filter filter = new NotFilter(new EqualityFilter(
        rdn.getNameValue().getName(),
        rdn.getNameValue().getStringValue()));
    SearchRequest searchRequest = SearchRequest.builder()
        .dn(ou.format())
        .filter(filter)
        .scope(SearchScope.SUBTREE)
        .returnAttributes(AdConstants.DN.getName())
        .build();
    return !getLdapTemplate().findAll(searchRequest).isEmpty();
  }

  @Override
  public OrganizationalUnit add(OrganizationalUnit organizationalUnit, Dn parentOu) {
    if (isEmpty(organizationalUnit.getName())) {
      throw ServiceException.badRequest(
          "Name of organizational unit is required.", EC_OU_NAME_REQUIRED);
    }
    if (organizationalUnit.getName().contains(",")) {
      throw ServiceException.badRequest(
          "Name of organizational unit contains illegal characters.", EC_ILLEGAL_OU_NAME);
    }
    Dn dn = new Dn(new RDn(new NameValue(
        AdConstants.RDN_ATTR_NAME_OU,
        organizationalUnit.getName())));
    if (!isEmpty(parentOu) && !parentOu.isEmpty()) {
      dn.add(validateOu(parentOu));
    }
    if (exists(dn)) {
      throw ServiceException.alreadyExistsWithErrorCode(
          OrganizationalUnit.class.getSimpleName(),
          organizationalUnit.getName(),
          EC_OU_ALREADY_EXISTS);
    }
    organizationalUnitTool.addOrganizationalUnit(organizationalUnit, validateOu(parentOu));
    return findOne(dn)
        .orElseThrow(() -> ServiceException
            .internalServerError(
                String.format("Adding organization unit '%s' failed.", dn.format(rdn -> rdn)),
                ErrorCode.EC_ADDING_OU_FAILED));
  }

  @Override
  public OrganizationalUnit update(
      OrganizationalUnit organizationalUnit,
      Dn newParentOu) {

    log.debug("update({}, {})", organizationalUnit.getName(), newParentOu);

    if (isEmpty(organizationalUnit.getName())) {
      throw ServiceException.badRequest(
          "Name of organizational unit is required.", EC_OU_NAME_REQUIRED);
    }
    if (organizationalUnit.getName().contains(",")) {
      throw ServiceException.badRequest(
          "Name of organizational unit contains illegal characters.", EC_ILLEGAL_OU_NAME);
    }

    OrganizationalUnit existing = findOne(new Dn(organizationalUnit.getDistinguishedName()))
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            OrganizationalUnit.class.getSimpleName(),
            organizationalUnit.getDistinguishedName(),
            EC_OU_NOT_FOUND));

    if (Boolean.TRUE.equals(existing.getSystemOu())) {
      existing.setDescription(organizationalUnit.getDescription());
      return getLdapTemplate().save(existing, ouLdapMapper);
    }

    Dn existingDn = new Dn(existing.getDistinguishedName());
    log.debug("Existing ou dn: {}", existingDn);
    Dn wantedDn = new Dn(new RDn(new NameValue(
        AdConstants.RDN_ATTR_NAME_OU,
        organizationalUnit.getName())));
    if (isEmpty(newParentOu) || newParentOu.isEmpty()) {
      wantedDn.add(existingDn.getParent());
    } else {
      wantedDn.add(newParentOu);
    }
    log.debug("Wanted ou dn:   {}", wantedDn);
    if (!existingDn.isSame(wantedDn) && exists(wantedDn)) {
      throw ServiceException.alreadyExistsWithErrorCode(
          OrganizationalUnit.class.getSimpleName(),
          organizationalUnit.getName(),
          EC_OU_ALREADY_EXISTS);
    }

    Dn currentDn = new Dn(existing.getDistinguishedName());
    String tmpName = null;
    if (!existingDn.getParent().isSame(wantedDn.getParent())) {
      if (!existing.getName().equalsIgnoreCase(organizationalUnit.getName())) {
        tmpName = organizationalUnit.getName() + '-' + UUID.randomUUID();
        currentDn = organizationalUnitTool.renameOrganizationalUnit(currentDn, tmpName);
      }
      currentDn = organizationalUnitTool.moveOrganizationalUnit(currentDn, wantedDn.getParent());
    }
    if (!isEmpty(tmpName)
        || (!existing.getName().equalsIgnoreCase(organizationalUnit.getName()))) {
      currentDn = organizationalUnitTool
          .renameOrganizationalUnit(currentDn, organizationalUnit.getName());
    }
    organizationalUnit.setDistinguishedName(currentDn.format());
    return getLdapTemplate().save(organizationalUnit, ouLdapMapper);
  }

  @Override
  public boolean delete(Dn ou) {
    return findOne(ou)
        .filter(this::isDeletable)
        .map(o -> doDelete(ou))
        .orElse(false);
  }

  boolean isDeletable(OrganizationalUnit o) {
    return !o.getSystemOu();
  }

  boolean doDelete(Dn ou) {
    organizationalUnitTool.deleteOrganizationalUnit(ou);
    if (exists(ou)) {
      throw ServiceException.internalServerError(
          String.format("Deleting organization unit '%s' failed.", ou.format(rdn -> rdn)),
          ErrorCode.EC_DELETING_OU_FAILED);
    }
    return true;
  }

}
