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
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.repository.mapper.GenericLdapMapper;
import org.ldaptive.DeleteRequest;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.NameValue;
import org.ldaptive.dn.RDn;
import org.ldaptive.filter.EqualityFilter;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.OrFilter;
import org.ldaptive.filter.PresenceFilter;
import org.springframework.stereotype.Component;

/**
 * The organizational unit repository implementation.
 *
 * @author Christian Bremer
 */
@Component("organizationalUnitRepository")
@Slf4j
class OrganizationalUnitRepositoryImpl extends AdRepository
    implements OrganizationalUnitRepository {

  private final LdaptiveEntryMapper<OrganizationalUnit> ouLdapMapper;

  private final GenericLdapMapper genericLdapMapper;

  OrganizationalUnitRepositoryImpl(
      ApplicationProperties properties,
      LdaptiveOperations ldapOperations,
      LdaptiveEntryMapper<OrganizationalUnit> ouLdapMapper,
      GenericLdapMapper genericLdapMapper) {
    super(properties, ldapOperations);
    this.ouLdapMapper = ouLdapMapper;
    this.genericLdapMapper = genericLdapMapper;
  }

  Dn getDefaultOu() {
    return getDnTool().getBaseDn();
  }

  String getObjectClassValue() {
    return AdConstants.OBJECT_CLASS_OU;
  }

  String[] getBinaryAttributes() {
    return ouLdapMapper.getBinaryAttributeNames();
  }

  String[] getReturnAttributes() {
    return ouLdapMapper.getMappedAttributeNames();
  }

  Filter objectClassFilter() {
    return new OrFilter(
        new EqualityFilter(AdConstants.OBJECT_CLASS.getName(), getObjectClassValue()),
        new EqualityFilter(AdConstants.DN.getName(),
            getDnTool().addBaseDn(AdConstants.BASE_DN_USERS).format()),
        new EqualityFilter(AdConstants.DN.getName(),
            getDnTool().addBaseDn(AdConstants.BASE_DN_COMPUTERS).format()));
  }

  @Override
  public Stream<OrganizationalUnit> findCustomOus() {
    SearchRequest searchRequest = SearchRequest.builder()
        .dn(getProperties().getBaseDn())
        .filter(new EqualityFilter(AdConstants.OBJECT_CLASS.getName(), AdConstants.OBJECT_CLASS_OU))
        .scope(SearchScope.SUBTREE)
        .binaryAttributes(getBinaryAttributes())
        .returnAttributes(getReturnAttributes())
        .build();
    return getLdapOperations().findAll(searchRequest, ouLdapMapper)
        .filter(getIgnoredObjectFilter());
  }

  @Override
  public Stream<OrganizationalUnit> findAll() {
    SearchRequest computersSearchRequest = SearchRequest
        .objectScopeSearchRequest(
            getDnTool().addBaseDn(AdConstants.BASE_DN_COMPUTERS).format(),
            getReturnAttributes());
    Stream<OrganizationalUnit> stream = getLdapOperations()
        .findOne(computersSearchRequest, ouLdapMapper)
        .stream();

    SearchRequest usersSearchRequest = SearchRequest
        .objectScopeSearchRequest(
            getDnTool().addBaseDn(AdConstants.BASE_DN_USERS).format(),
            getReturnAttributes());
    stream = Stream.concat(
        stream,
        getLdapOperations().findOne(usersSearchRequest, ouLdapMapper).stream());

    return Stream.concat(stream, findCustomOus());
  }

  @Override
  public Optional<OrganizationalUnit> findOne(Dn ou) {
    log.debug("findOne({})", ou);
    if (ou.isEmpty()) {
      return Optional.empty();
    }
    String dn = getDnTool().addBaseDn(ou).format();
    log.debug("findOne, dn = {}", dn);
    SearchRequest searchRequest = SearchRequest.objectScopeSearchRequest(dn, getReturnAttributes(),
        objectClassFilter());
    return getLdapOperations()
        .findOne(searchRequest, ouLdapMapper);
  }

  @Override
  public boolean exists(Dn ou) {
    return findOne(ou).isPresent();
  }

  @Override
  public boolean hasChildren(Dn ou) {
    /*
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

     */
    Dn dn;
    if (isEmpty(ou) || ou.isEmpty() || ou.isAncestor(getDnTool().getBaseDn())) {
      dn = getDnTool().getBaseDn();
    } else {
      dn = getDnTool().addBaseDn(ou);
    }
    if (!getDnTool().isValidDnWithBaseDn(dn)) {
      return false;
    }
    SearchRequest searchRequest = SearchRequest.builder()
        .dn(dn.format())
        .filter(new PresenceFilter(AdConstants.OBJECT_CLASS.getName()))
        .scope(SearchScope.ONELEVEL)
        .returnAttributes(AdConstants.DN.getName())
        .build();
    return !getLdapOperations().findAll(searchRequest).isEmpty();
  }

  @Override
  public Stream<AdEntry> getChildren(Dn ou) {
    Dn dn;
    if (isEmpty(ou) || ou.isEmpty() || ou.isAncestor(getDnTool().getBaseDn())) {
      dn = getDnTool().getBaseDn();
    } else {
      dn = getDnTool().addBaseDn(ou);
    }
    if (!getDnTool().isValidDnWithBaseDn(dn)) {
      return Stream.empty();
    }
    SearchRequest searchRequest = SearchRequest.builder()
        .dn(dn.format())
        .filter(new PresenceFilter(AdConstants.OBJECT_CLASS.getName()))
        .scope(SearchScope.ONELEVEL)
        .returnAttributes(genericLdapMapper.getMappedAttributeNames())
        .binaryAttributes(genericLdapMapper.getBinaryAttributeNames())
        .build();
    return getLdapOperations().findAll(searchRequest).stream()
        .flatMap(ldapEntry -> genericLdapMapper.map(ldapEntry).stream());
  }

  @Override
  public OrganizationalUnit add(OrganizationalUnit organizationalUnit, Dn parentOu) {
    if (isEmpty(organizationalUnit.getName())) {
      throw ServiceException.badRequest(
          "Name of organizational unit is required.", EC_OU_NAME_REQUIRED);
    }
    if (organizationalUnit.getName().contains(",") || organizationalUnit.getName().contains("=")) {
      throw ServiceException.badRequest(
          "Name of organizational unit contains illegal characters.", EC_ILLEGAL_OU_NAME);
    }
    Dn dn = new Dn(new RDn(new NameValue(
        AdConstants.RDN_ATTR_NAME_OU,
        organizationalUnit.getName())));
    dn.add(validateParentDn(parentOu, this::getDefaultOu));
    if (exists(dn)) {
      throw ServiceException.alreadyExistsWithErrorCode(
          OrganizationalUnit.class.getSimpleName(),
          organizationalUnit.getName(),
          EC_OU_ALREADY_EXISTS);
    }
    String dnStr = dn.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER);
    return getLdapOperations()
        .save(organizationalUnit.withDistinguishedName(dnStr), ouLdapMapper);
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
    if (organizationalUnit.getName().contains(",") || organizationalUnit.getName().contains("=")) {
      throw ServiceException.badRequest(
          "Name of organizational unit contains illegal characters.", EC_ILLEGAL_OU_NAME);
    }

    OrganizationalUnit existing = findOne(new Dn(organizationalUnit.getDistinguishedName()))
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            OrganizationalUnit.class.getSimpleName(),
            organizationalUnit.getDistinguishedName(),
            EC_OU_NOT_FOUND));

    if (existing.isSystemOu()) {
      return getLdapOperations().save(
          OrganizationalUnit.builder()
              .from(existing)
              .description(organizationalUnit.getDescription())
              .build(),
          ouLdapMapper);
    }

    Dn existingDn = new Dn(existing.getDistinguishedName());
    Dn wantedDn = new Dn(new RDn(new NameValue(
        AdConstants.RDN_ATTR_NAME_OU,
        organizationalUnit.getName())));
    if (isEmpty(newParentOu) || newParentOu.isEmpty()) {
      wantedDn.add(existingDn.getParent());
    } else {
      wantedDn.add(validateParentDn(newParentOu, this::getDefaultOu));
    }
    if (!existingDn.isSame(wantedDn) && exists(wantedDn)) {
      throw ServiceException.alreadyExistsWithErrorCode(
          OrganizationalUnit.class.getSimpleName(),
          organizationalUnit.getName(),
          EC_OU_ALREADY_EXISTS);
    }
    moveAndRename(existingDn, wantedDn);
    return findOne(wantedDn)
        .map(ou -> OrganizationalUnit.builder()
            .from(ou)
            .description(organizationalUnit.getDescription())
            .build())
        .map(ou -> getLdapOperations().save(ou, ouLdapMapper))
        .orElseThrow(() -> ServiceException.internalServerError(
            String.format(
                "Updating organization unit '%s' failed.",
                wantedDn.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER)),
            ErrorCode.EC_UPDATING_OU_FAILED));
  }

  @Override
  public boolean delete(Dn ou) {
    return findOne(ou)
        .filter(this::isDeletable)
        .map(o -> {
          getLdapOperations().delete(DeleteRequest.builder()
              .dn(o.getDistinguishedName())
              .build());
          return true;
        })
        .orElse(false);
  }

  boolean isDeletable(OrganizationalUnit o) {
    return !o.isSystemOu();
  }

}
