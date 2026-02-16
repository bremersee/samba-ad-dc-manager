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

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.AbstractLdaptiveErrorHandler;
import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.ldaptive.LdaptiveException;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.misc.TreeSearchScopeConverter;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModification.Type;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.NameValue;
import org.ldaptive.dn.RDn;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.EqualityFilter;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.NotFilter;
import org.ldaptive.filter.OrFilter;
import org.ldaptive.filter.SubstringFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * The domain user repository.
 *
 * @author Christian Bremer
 */
@Component("domainUserRepository")
@Slf4j
public class DomainUserRepositoryImpl extends SamAccountRepository
    implements DomainUserRepository {

  private final DomainRepository domainRepository;

  private final LdaptiveEntryMapper<DomainUser> domainUserLdapMapper;

  /**
   * Instantiates a new domain user repository.
   *
   * @param properties the properties
   * @param ldapOperations the ldap operations
   * @param domainRepository the domain repository
   */
  public DomainUserRepositoryImpl(
      ApplicationProperties properties,
      LdaptiveOperations ldapOperations,
      DomainRepository domainRepository,
      LdaptiveEntryMapper<DomainUser> domainUserLdapMapper) {
    super(properties, ldapOperations);
    this.domainRepository = domainRepository;
    this.domainUserLdapMapper = domainUserLdapMapper;
  }

  @Override
  protected Dn getDefaultOu() {
    return new Dn(getProperties().getUser().getDefaultOu());
  }

  @Override
  protected String getObjectClassValue() {
    return AdConstants.OBJECT_CLASS_USER;
  }

  @Override
  protected String[] getBinaryAttributes() {
    return domainUserLdapMapper.getBinaryAttributeNames();
  }

  @Override
  protected String[] getReturnAttributes() {
    return domainUserLdapMapper.getMappedAttributeNames();
  }

  @Override
  protected Filter objectClassFilter() {
    Filter objectClassFilter = new EqualityFilter(
        AdConstants.OBJECT_CLASS.getName(), getObjectClassValue());
    Filter noComputerFilter = new NotFilter(
        new EqualityFilter(AdConstants.OBJECT_CLASS.getName(), AdConstants.OBJECT_CLASS_COMPUTER));
    return new AndFilter(objectClassFilter, noComputerFilter);
  }

  @Override
  protected Filter findOneFilter(String uniqueName) {
    Filter samAccountNameFilter = super.findOneFilter(uniqueName);
    if (getEmailPattern().matcher(uniqueName).matches()) {
      Filter emailFilter = new EqualityFilter(AdConstants.MAIL.getName(), uniqueName);
      Filter principalFilter = new EqualityFilter(
          AdConstants.USER_PRINCIPAL_NAME.getName(),
          uniqueName);
      return new OrFilter(samAccountNameFilter, emailFilter, principalFilter);
    }
    return samAccountNameFilter;
  }

  private Filter getFindAllFilter(String query) {
    Filter objectClassFilter = objectClassFilter();
    if (isNull(query) || query.length() < getProperties().getUser().getMinQueryLength()) {
      return objectClassFilter;
    }
    Filter orFilter = new OrFilter(
        new SubstringFilter(AdConstants.USER_COMPANY.getName(), null, null, query),
        new SubstringFilter(AdConstants.USER_DEPARTMENT.getName(), null, null, query),
        new SubstringFilter(AdConstants.DESCRIPTION.getName(), null, null, query),
        new SubstringFilter(AdConstants.USER_DISPLAY_NAME.getName(), null, null, query),
        new EqualityFilter(AdConstants.GID_NUMBER.getName(), query),
        new SubstringFilter(AdConstants.USER_GIVEN_NAME.getName(), null, null, query),
        new SubstringFilter(AdConstants.MAIL.getName(), null, null, query),
        new SubstringFilter(AdConstants.USER_MOBILE.getName(), null, null, query),
        new SubstringFilter(AdConstants.USER_OFFICE_NAME.getName(), null, null,
            query),
        new SubstringFilter(AdConstants.SAM_ACCOUNT_NAME.getName(), null, null, query),
        new SubstringFilter(AdConstants.USER_SN.getName(), null, null, query),
        new SubstringFilter(AdConstants.USER_TELEPHONE_NUMBER.getName(), null, null, query),
        new EqualityFilter(AdConstants.USER_UID_NUMBER.getName(), query)
    );
    return new AndFilter(objectClassFilter, orFilter);
  }

  @Override
  public Stream<DomainUser> findAll(String query, Dn ou, TreeSearchScope searchScope) {
    log.debug("findAll({}, {}, {})", query, ou, searchScope);
    SearchScope scope = TreeSearchScopeConverter.toSearchScope(searchScope);
    SearchRequest searchRequest = searchAllRequest(
        ou,
        getFindAllFilter(query),
        scope,
        getReturnAttributes());
    return getLdapOperations()
        .findAll(searchRequest, domainUserLdapMapper)
        .filter(getIgnoredObjectFilter(ou, scope));
  }

  @Override
  public Optional<DomainUser> findOne(String userName, Dn ou, TreeSearchScope searchScope) {
    log.debug("findOne({})", userName);
    SearchScope scope = TreeSearchScopeConverter.toSearchScope(searchScope);
    SearchRequest searchRequest = searchOneRequest(userName, ou, scope);
    return getLdapOperations()
        .findOne(searchRequest, domainUserLdapMapper)
        .filter(getIgnoredObjectFilter(ou, scope));
  }

  public Optional<DomainUser> findOneByUid(String uid) {
    log.debug("findOneByUid({})", uid);
    if (isEmpty(uid)) {
      return Optional.empty();
    }
    Filter filter = new OrFilter(
        new EqualityFilter(AdConstants.USER_UID.getName(), uid),
        new EqualityFilter(AdConstants.NIS_NAME.getName(), uid));
    return findOneByFilter(filter);
  }

  public Optional<DomainUser> findOneByUidNumber(Integer uidNumber) {
    log.debug("findOneByUidNumber({})", uidNumber);
    if (isEmpty(uidNumber)) {
      return Optional.empty();
    }
    return findOneByFilter(
        new EqualityFilter(AdConstants.USER_UID_NUMBER.getName(), uidNumber.toString()));
  }

  public Optional<DomainUser> findOneByPrincipalName(String principalName) {
    log.debug("findOneByPrincipalName({})", principalName);
    if (isEmpty(principalName)) {
      return Optional.empty();
    }
    return findOneByFilter(
        new EqualityFilter(AdConstants.USER_PRINCIPAL_NAME.getName(),
            principalName));
  }

  private Optional<DomainUser> findOneByFilter(Filter filter) {
    SearchRequest searchRequest = SearchRequest.builder()
        .dn(getProperties().getBaseDn())
        .filter(new AndFilter(objectClassFilter(), filter))
        .scope(SearchScope.SUBTREE)
        .binaryAttributes(getBinaryAttributes())
        .returnAttributes(getReturnAttributes())
        .build();
    return getLdapOperations()
        .findOne(searchRequest, domainUserLdapMapper)
        .filter(getIgnoredObjectFilter());
  }

  public boolean existsByUid(String uid) {
    log.debug("existsByUid({})", uid);
    if (isEmpty(uid)) {
      return false;
    }
    Filter filter = new OrFilter(
        new EqualityFilter(AdConstants.USER_UID.getName(), uid),
        new EqualityFilter(AdConstants.NIS_NAME.getName(), uid));
    return existsByFilter(filter);
  }

  public boolean existsByUidNumber(Integer uidNumber) {
    log.debug("existsByUidNumber({})", uidNumber);
    if (isEmpty(uidNumber)) {
      return false;
    }
    return existsByFilter(
        new EqualityFilter(AdConstants.USER_UID_NUMBER.getName(), uidNumber.toString()));
  }

  public boolean existsByPrincipalName(String principalName) {
    log.debug("existsByPrincipalName({})", principalName);
    if (isEmpty(principalName)) {
      return false;
    }
    return existsByFilter(
        new EqualityFilter(AdConstants.USER_PRINCIPAL_NAME.getName(),
            principalName));
  }

  private boolean existsByFilter(Filter filter) {
    SearchRequest searchRequest = SearchRequest.builder()
        .dn(getProperties().getBaseDn())
        .filter(new AndFilter(objectClassFilter(), filter))
        .scope(SearchScope.SUBTREE)
        .returnAttributes(AdConstants.OBJECT_CLASS.getName())
        .build();
    return getLdapOperations()
        .findOne(searchRequest)
        .filter(getIgnoredEntryFilter())
        .isPresent();
  }

  private RDn getRdn(DomainUser user, Boolean useUsernameAsCn) {
    boolean useUsername = requireNonNullElse(
        useUsernameAsCn, getProperties().getUser().isUseUsernameAsCn())
        || isEmpty(user.getFirstName())
        || isEmpty(user.getLastName());
    String cn = useUsername
        ? user.getSamAccountName()
        : user.getFirstName() + ' ' + user.getLastName();
    return new RDn(new NameValue(AdConstants.CN.getName(), cn));
  }

  private RDn getRdn(DomainUser oldUser, DomainUser newUser) {
    RDn oldRdn = oldUser.getDn().getRDn();
    boolean useUsername = oldUser.getSamAccountName()
        .equalsIgnoreCase(oldRdn.getNameValue().getStringValue())
        || isEmpty(newUser.getFirstName())
        || isEmpty(newUser.getLastName());
    String cn = useUsername
        ? newUser.getSamAccountName()
        : newUser.getFirstName() + ' ' + oldUser.getLastName();
    return new RDn(new NameValue(oldRdn.getNameValue().getName(), cn));
  }

  @Override
  public DomainUser add(
      DomainUser domainUser,
      String clearPassword,
      Dn ou,
      Boolean useUsernameAsCn) {

    log.debug("add({}, {}, {}, {})", domainUser.getSamAccountName(),
        isEmpty(clearPassword) ? "null" : "****", ou, useUsernameAsCn);

    DomainUser user = domainUser
        .withDistinguishedName("")
        .withCreated(null)
        .withModified(null)
        .withSid(null)
        .withCriticalSystemObject(false)
        .withPrimaryGroupId(null)
        .withMemberships(List.of());

    validateNewSamAccountName(user);
    validateEmail(user.getEmail());

    String defaultPrincipalName = Optional.ofNullable(user.getUserPrincipalName())
        .filter(name -> !isEmpty(name))
        .orElseGet(() -> user.getSamAccountName()
            + '@' + domainRepository.getDomainInfo().getDomain());
    if (samAccountExists(user) || existsByPrincipalName(defaultPrincipalName)) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName(),
          user.getSamAccountName(),
          EC_SAM_ACCOUNT_ALREADY_EXISTS);
    }
    Pattern passwordPattern = domainRepository.getPasswordInformation().getPasswordPattern();
    if (!isEmpty(clearPassword) && !passwordPattern.matcher(clearPassword).matches()) {
      throw ServiceException.badRequest(
          String.format(
              "The password of user '%s' does not meet the complexity criteria!",
              user.getSamAccountName()),
          EC_PASSWORD_RESTRICTIONS);
    }
    if (existsByUid(user.getUid())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName() + ".uid",
          user.getUid(),
          EC_UID_ALREADY_EXISTS);
    }
    if (existsByUidNumber(user.getUidNumber())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName() + ".uidNumber",
          user.getUidNumber(),
          EC_UID_NUMBER_ALREADY_EXISTS);
    }
    Dn dn = new Dn(getRdn(user, useUsernameAsCn));
    dn.add(validateParentDn(ou, this::getDefaultOu));
    String dnStr = DnTool.toString(dn);
    DomainUser addedUser = getLdapOperations()
        .save(user.withDistinguishedName(dnStr), domainUserLdapMapper);
    if (!isEmpty(clearPassword)) {
      doSavePassword(addedUser.getDistinguishedName(), clearPassword);
    }
    return addedUser;
  }

  @Override
  public DomainUser update(String userName, DomainUser domainUser, Dn newOu) {

    log.debug("update({}, {}, {})", userName, domainUser.getSamAccountName(), newOu);

    if (!userName.equals(domainUser.getSamAccountName())) {
      validateNewSamAccountName(domainUser);
    }
    validateEmail(domainUser.getEmail());

    if (!userName.equalsIgnoreCase(domainUser.getSamAccountName())
        && samAccountNameExists(domainUser.getSamAccountName())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName(),
          domainUser.getSamAccountName(),
          EC_SAM_ACCOUNT_ALREADY_EXISTS);
    }
    DomainUser existingDomainUser = findOne(userName, null, null)
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            DomainUser.class.getSimpleName(),
            domainUser.getSamAccountName(),
            EC_SAM_ACCOUNT_NOT_FOUND));
    DomainUser user = domainUser
        .withDistinguishedName(existingDomainUser.getDistinguishedName())
        .withCreated(existingDomainUser.getCreated())
        .withModified(OffsetDateTime.now())
        .withSid(existingDomainUser.getSid())
        .withCriticalSystemObject(existingDomainUser.isCriticalSystemObject())
        .withMemberships(existingDomainUser.getMemberships());
    if (!isEmpty(user.getUserPrincipalName())
        && !user.getUserPrincipalName()
        .equalsIgnoreCase(existingDomainUser.getUserPrincipalName())
        && existsByPrincipalName(user.getUserPrincipalName())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName() + ".userPrincipalName",
          user.getUserPrincipalName(),
          EC_PRINCIPAL_ALREADY_EXISTS);
    }
    if (!isEmpty(user.getUid())
        && !user.getUid().equalsIgnoreCase(existingDomainUser.getUid())
        && existsByUid(user.getUid())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName() + ".uid",
          user.getUid(),
          EC_UID_ALREADY_EXISTS);
    }
    if (!isEmpty(user.getUidNumber())
        && !Objects.equals(user.getUidNumber(), existingDomainUser.getUidNumber())
        && existsByUidNumber(user.getUidNumber())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName() + ".uidNumber",
          user.getUidNumber(),
          EC_UID_NUMBER_ALREADY_EXISTS);
    }
    Dn oldDn = new Dn(existingDomainUser.getDistinguishedName());
    Dn newDn = new Dn(getRdn(existingDomainUser, user));
    newDn.add(validateParentDn(newOu, oldDn::getParent));
    if (!oldDn.isSame(newDn) && dnExistsWithAnyObjectClass(newDn.format())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName(),
          getDnTool().removeBaseDn(newDn),
          EC_DN_ALREADY_EXISTS);
    }
    moveAndRename(oldDn, newDn);
    String newDnStr = DnTool.toString(newDn);
    return getLdapOperations()
        .save(user.withDistinguishedName(newDnStr), domainUserLdapMapper);
  }

  @Override
  public void savePassword(String userName, String newPassword) {
    log.debug("savePassword({}, ****)", userName);
    findDnOfSamAccountName(userName).ifPresentOrElse(
        dn -> doSavePassword(dn, newPassword),
        () -> {
          throw ServiceException.notFoundWithErrorCode(
              DomainUser.class.getSimpleName(),
              userName,
              EC_SAM_ACCOUNT_NOT_FOUND);
        });
  }

  void doSavePassword(String dn, String newPassword) {
    AttributeModification attributeModification = new AttributeModification(
        Type.REPLACE,
        AdConstants.USER_UNICODE_PWD.createAttribute(newPassword));
    ModifyRequest modifyRequest = ModifyRequest.builder()
        .dn(dn)
        .modifications(attributeModification)
        .build();
    getLdapOperations()
        .copy(new AbstractLdaptiveErrorHandler() {
          @Override
          public LdaptiveException map(LdapException ldapException) {
            HttpStatus httpStatus;
            String errorCode;
            if (ldapException.getResultCode() == ResultCode.CONSTRAINT_VIOLATION
                && ldapException.getMessage().contains("check_password_restrictions")) {
              httpStatus = HttpStatus.BAD_REQUEST;
              errorCode = "check_password_restrictions";
            } else {
              httpStatus = ldapException.getResultCode() == ResultCode.NO_SUCH_OBJECT
                  ? HttpStatus.NOT_FOUND
                  : HttpStatus.INTERNAL_SERVER_ERROR;
              errorCode = httpStatus == HttpStatus.NOT_FOUND
                  ? EC_SAM_ACCOUNT_NOT_FOUND
                  : EC_SAVING_PASSWORD_FAILED;
            }
            return LdaptiveException.builder()
                .httpStatus(httpStatus.value())
                .errorCode(errorCode)
                .cause(ldapException)
                .build();
          }
        })
        .modify(modifyRequest);
  }

  @Override
  public boolean delete(String userName) {
    log.debug("delete({})", userName);
    return findOne(userName, null, null)
        .map(user -> {
          if (user.isCriticalSystemObject()) {
            throw ServiceException.badRequest(
                String.format(
                    "User '%s' is a system account. Deletion failed.",
                    user.getSamAccountName()),
                EC_ILLEGAL_SYSTEM_ENTITY_OPERATION);
          }
          getLdapOperations().delete(DeleteRequest.builder()
              .dn(user.getDistinguishedName())
              .build());
          return true;
        })
        .orElse(false);
  }

}
