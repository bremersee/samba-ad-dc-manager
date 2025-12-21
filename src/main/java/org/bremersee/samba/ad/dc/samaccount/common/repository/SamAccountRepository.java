/*
 * Copyright 2025 the original author or authors.
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

package org.bremersee.samba.ad.dc.samaccount.common.repository;

import static java.util.Objects.requireNonNullElseGet;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.bremersee.samba.ad.dc.repository.AdRepository;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.samaccount.common.model.SamAccount;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.EqualityFilter;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.PresenceFilter;

/**
 * The type AbstractSamAccountRepository.
 *
 * @author Christian Bremer
 */
@Slf4j
public abstract class SamAccountRepository extends AdRepository {

  private static final Pattern samAccountNamePattern = Pattern
      .compile("^[^/\\\\\\[\\]:;|=,+?<>@â€\u009D]+$");

  /**
   * Instantiates a new abstract repository.
   *
   * @param properties the properties
   * @param ldapTemplate the ldap template
   */
  protected SamAccountRepository(
      DomainControllerProperties properties,
      LdaptiveTemplate ldapTemplate) {
    super(properties, ldapTemplate);
  }

  protected abstract Dn getDefaultOu();

  protected abstract String getObjectClassValue();

  protected abstract String[] getBinaryAttributes();

  protected abstract String[] getReturnAttributes();

  protected String getUniqueNameAttributeName() {
    return AdConstants.SAM_ACCOUNT_NAME.getName();
  }

  protected Filter objectClassFilter() {
    return new EqualityFilter(AdConstants.OBJECT_CLASS.getName(), getObjectClassValue());
  }

  protected Filter findOneFilter(String uniqueName) {
    return new AndFilter(
        objectClassFilter(),
        new EqualityFilter(getUniqueNameAttributeName(), uniqueName));
  }

  protected SearchRequest searchOneRequest(
      String uniqueName,
      String... returnAttributes) {
    return searchOneRequest(uniqueName, null, null, returnAttributes);
  }

  protected SearchRequest searchOneRequest(
      String uniqueName,
      Dn ouRdn,
      SearchScope scope,
      String... returnAttributes) {
    return searchOneRequest(uniqueName, ouRdn, null, scope, returnAttributes);
  }

  protected SearchRequest searchOneRequest(
      String uniqueName,
      Dn ouRdn,
      Filter filter,
      SearchScope scope,
      String... returnAttributes) {

    if (getDnTool().isValidDnWithBaseDn(uniqueName)) {
      return SearchRequest.builder()
          .dn(uniqueName)
          .filter(objectClassFilter())
          .scope(SearchScope.OBJECT)
          .binaryAttributes(getBinaryAttributes())
          .returnAttributes(isEmpty(returnAttributes) ? getReturnAttributes() : returnAttributes)
          .sizeLimit(1)
          .build();
    }
    Dn ouDn = getDnTool().addBaseDn(ouRdn);
    return SearchRequest.builder()
        .dn(ouDn.format())
        .filter(requireNonNullElseGet(filter, () -> findOneFilter(uniqueName)))
        .scope(Optional.ofNullable(scope)
            .filter(searchScope -> !ouDn.isSame(getDnTool().getBaseDn()))
            .orElse(SearchScope.SUBTREE))
        .binaryAttributes(getBinaryAttributes())
        .returnAttributes(isEmpty(returnAttributes) ? getReturnAttributes() : returnAttributes)
        .sizeLimit(1)
        .build();
  }

  protected SearchRequest searchAllRequest(
      Dn ouRdn,
      Filter filter,
      SearchScope scope,
      String... returnAttributes) {
    Dn ouDn = getDnTool().addBaseDn(ouRdn);
    return SearchRequest.builder()
        .dn(ouDn.format())
        .filter(filter)
        .scope(Optional.ofNullable(scope)
            .filter(searchScope -> !ouDn.isSame(getDnTool().getBaseDn()))
            .orElse(SearchScope.SUBTREE))
        .binaryAttributes(getBinaryAttributes())
        .returnAttributes(isEmpty(returnAttributes) ? getReturnAttributes() : returnAttributes)
        .build();
  }

  protected void validateSamAccountName(SamAccount samAccount) {
    if (isEmpty(samAccount.getSamAccountName())) {
      throw ServiceException.badRequest(
          "SamAccountName is required.", EC_SAM_ACCOUNT_NAME_REQUIRED);
    }
    if (!samAccountNamePattern.matcher(samAccount.getSamAccountName()).matches()) {
      throw ServiceException.badRequest(
          "SamAccountName contains illegal characters.", EC_ILLEGAL_SAM_ACCOUNT_NAME);
    }
  }

  protected boolean samAccountExists(SamAccount samAccount) {
    return findDnOfSamAccount(samAccount).isPresent();
  }

  protected boolean samAccountNameExists(String samAccountName) {
    return findDnOfSamAccountName(samAccountName).isPresent();
  }

  protected Optional<String> findDnOfSamAccount(SamAccount samAccount) {
    if (isEmpty(samAccount)) {
      return Optional.empty();
    }
    return findDnOfSamAccountName(samAccount.getSamAccountName());
  }

  protected Optional<String> findDnOfSamAccountName(String samAccountName) {
    log.debug("findDnOfSamAccountName({})", samAccountName);
    if (isEmpty(samAccountName)) {
      log.debug("Dn of '{}' does not exist", samAccountName);
      return Optional.empty();
    }
    String[] returnAttributes = new String[]{AdConstants.DN.getName()};
    SearchRequest searchRequest;
    if (getDnTool().isValidDnWithBaseDn(samAccountName)) {
      searchRequest = SearchRequest.objectScopeSearchRequest(
          samAccountName,
          returnAttributes,
          new PresenceFilter(AdConstants.SAM_ACCOUNT_NAME.getName()));
    } else {
      searchRequest = SearchRequest.builder()
          .dn(getProperties().getBaseDn())
          .filter(new EqualityFilter(AdConstants.SAM_ACCOUNT_NAME.getName(), samAccountName))
          .scope(SearchScope.SUBTREE)
          .returnAttributes(returnAttributes)
          .sizeLimit(1)
          .build();
    }
    log.debug("findDnOfSamAccountName, searchRequest = {}", searchRequest);
    return getLdapTemplate().findOne(searchRequest)
        .map(LdapEntry::getDn)
        .filter(getIgnoredDnFilter());
  }

}
