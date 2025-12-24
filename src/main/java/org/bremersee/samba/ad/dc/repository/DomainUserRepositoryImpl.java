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

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.AbstractLdaptiveErrorHandler;
import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.ldaptive.LdaptiveException;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.misc.TreeSearchScopeConverter;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.repository.mapper.DomainUserLdapMapper;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModification.Type;
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

  private final LdaptiveEntryMapper<DomainUser> domainUserLdapMapper;

  private final DomainRepository domainRepository;

  private final SambaToolUser domainUserTool;

  /**
   * Instantiates a new domain user repository.
   *
   * @param properties the properties
   * @param ldapTemplate the ldap template
   * @param domainRepository the domain repository
   */
  public DomainUserRepositoryImpl(
      DomainControllerProperties properties,
      LdaptiveTemplate ldapTemplate,
      DomainRepository domainRepository,
      SambaToolUser domainUserTool) {
    super(properties, ldapTemplate);
    this.domainRepository = domainRepository;
    this.domainUserTool = domainUserTool;
    this.domainUserLdapMapper = new DomainUserLdapMapper(this.domainRepository::isRfc2307Enabled);
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
    return getLdapTemplate()
        .findAll(searchRequest, domainUserLdapMapper)
        .filter(getIgnoredObjectFilter(ou, scope));
  }

  @Override
  public Optional<DomainUser> findOne(String userName, Dn ou, TreeSearchScope searchScope) {
    log.debug("findOne({})", userName);
    SearchScope scope = TreeSearchScopeConverter.toSearchScope(searchScope);
    SearchRequest searchRequest = searchOneRequest(userName, ou, scope);
    return getLdapTemplate()
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
    return getLdapTemplate()
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
    return getLdapTemplate()
        .findOne(searchRequest)
        .filter(getIgnoredEntryFilter())
        .isPresent();
  }

  @Override
  public DomainUser add(DomainUser domainUser, Dn ou, Boolean useUsernameAsCn) {

    log.debug("add({}, {}, {})", domainUser.getSamAccountName(), ou, useUsernameAsCn);

    validateSamAccountName(domainUser);
    validateEmail(domainUser.getEmail());

    String defaultPrincipalName = Optional.ofNullable(domainUser.getUserPrincipalName())
        .filter(name -> !isEmpty(name))
        .orElseGet(() -> domainUser.getSamAccountName()
            + '@' + domainRepository.getDomainInfo().getDomain());
    if (samAccountExists(domainUser) || existsByPrincipalName(defaultPrincipalName)) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName(),
          domainUser.getSamAccountName(),
          EC_SAM_ACCOUNT_ALREADY_EXISTS);
    }
    Pattern passwordPattern = domainRepository.getPasswordInformation().getPasswordPattern();
    if (!isEmpty(domainUser.getPassword())
        && !passwordPattern.matcher(domainUser.getPassword()).matches()) {
      throw ServiceException.badRequest(
          String.format(
              "The password of user '%s' does not meet the complexity criteria!",
              domainUser.getSamAccountName()),
          EC_PASSWORD_RESTRICTIONS);
    }
    if (existsByUid(domainUser.getUid())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName() + ".uid",
          domainUser.getUid(),
          EC_UID_ALREADY_EXISTS);
    }
    if (existsByUidNumber(domainUser.getUidNumber())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName() + ".uidNumber",
          domainUser.getUidNumber(),
          EC_UID_NUMBER_ALREADY_EXISTS);
    }
    Dn userOu = DnTool.isValidDn(ou) ? ou : getDefaultOu();
    domainUserTool
        .addUser(domainUser, userOu, useUsernameAsCn, domainRepository.isRfc2307Enabled());
    return findDnOfSamAccount(domainUser)
        .map(dn -> getLdapTemplate()
            .save(domainUser.withDistinguishedName(dn), domainUserLdapMapper))
        .orElseThrow(() -> ServiceException
            .internalServerError(
                String.format("Adding user '%s' failed.", domainUser.getSamAccountName()),
                EC_ADDING_USER_FAILED));
  }

  @Override
  public DomainUser update(String userName, DomainUser domainUser, Dn newOu) {

    log.debug("update({}, {}, {})", userName, domainUser.getSamAccountName(), newOu);

    validateSamAccountName(domainUser);
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
    if (!isEmpty(domainUser.getUserPrincipalName())
        && !domainUser.getUserPrincipalName()
        .equalsIgnoreCase(existingDomainUser.getUserPrincipalName())
        && existsByPrincipalName(domainUser.getUserPrincipalName())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName() + ".userPrincipalName",
          domainUser.getUserPrincipalName(),
          EC_PRINCIPAL_ALREADY_EXISTS);
    }
    if (!isEmpty(domainUser.getUid())
        && !domainUser.getUid().equalsIgnoreCase(existingDomainUser.getUid())
        && existsByUid(domainUser.getUid())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName() + ".uid",
          domainUser.getUid(),
          EC_UID_ALREADY_EXISTS);
    }
    if (!isEmpty(domainUser.getUidNumber())
        && !Objects.equals(domainUser.getUidNumber(), existingDomainUser.getUidNumber())
        && existsByUidNumber(domainUser.getUidNumber())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName() + ".uidNumber",
          domainUser.getUidNumber(),
          EC_UID_NUMBER_ALREADY_EXISTS);
    }
    Dn oldDn = new Dn(existingDomainUser.getDistinguishedName());
    Dn newDn = getNewDn(existingDomainUser, domainUser, newOu);
    if (!oldDn.isSame(newDn) && dnExistsWithAnyObjectClass(newDn.format())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName(),
          getDnTool().removeBaseDn(newDn),
          EC_DN_ALREADY_EXISTS);
    }

    domainUserTool.renameAndMoveUser(existingDomainUser, domainUser, newDn);
    return findDnOfSamAccount(domainUser)
        .map(domainUser::withDistinguishedName)
        .map(user -> getLdapTemplate().save(user, domainUserLdapMapper))
        .orElseThrow(() -> ServiceException.internalServerError(
            String.format("Updating user '%s' failed.", userName),
            EC_UPDATING_USER_FAILED));
  }

  Dn getNewDn(DomainUser oldUser, DomainUser newUser, Dn newOu) {
    Dn newParentDn;
    if (DnTool.isValidDn(newOu)) {
      newParentDn = getDnTool().addBaseDn(newOu);
    } else {
      newParentDn = oldUser.getDn().getParent();
    }

    RDn oldRdn = new Dn(oldUser.getDistinguishedName()).getRDn();
    String oldCn = oldRdn.getNameValue().getStringValue().toLowerCase();
    String newCn;
    if (oldCn.equalsIgnoreCase(newUser.getSamAccountName())
        || oldCn.equalsIgnoreCase(newUser.getDisplayName())) {
      newCn = oldCn;
    } else if (oldCn.equalsIgnoreCase(oldUser.getDisplayName())
        && !isEmpty(newUser.getFirstName()) && !isEmpty(newUser.getLastName())) {
      newCn = newUser.getFirstName() + " " + newUser.getLastName();
    } else {
      newCn = newUser.getSamAccountName();
    }
    Dn newDn = new Dn(new RDn(new NameValue(oldRdn.getNameValue().getName(), newCn)));
    newDn.add(newParentDn);
    return newDn;
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
    getLdapTemplate()
        .clone(new AbstractLdaptiveErrorHandler() {
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
          domainUserTool.deleteUser(user.getSamAccountName());
          return true;
        })
        .orElse(false);
  }

}
