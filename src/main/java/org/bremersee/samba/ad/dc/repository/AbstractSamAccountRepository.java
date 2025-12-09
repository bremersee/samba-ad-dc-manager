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

package org.bremersee.samba.ad.dc.repository;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.model.SamAccount;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.filter.EqualityFilter;

/**
 * The type AbstractSamAccountRepository.
 *
 * @author Christian Bremer
 */
@Slf4j
abstract class AbstractSamAccountRepository extends AbstractOrganizedEntryRepository {

  private static final Pattern samAccountNamePattern = Pattern
      .compile("^[^/\\\\\\[\\]:;|=,+?<>@â€\u009D]+$");

  /**
   * Instantiates a new abstract repository.
   *
   * @param properties the properties
   * @param ldapTemplate the ldap template
   */
  AbstractSamAccountRepository(
      DomainControllerProperties properties,
      LdaptiveTemplate ldapTemplate) {
    super(properties, ldapTemplate);
  }

  void validateSamAccountName(SamAccount samAccount) {
    if (isEmpty(samAccount.getSamAccountName())) {
      throw ServiceException.badRequest(
          "SamAccountName is required.", EC_SAM_ACCOUNT_NAME_REQUIRED);
    }
    if (!samAccountNamePattern.matcher(samAccount.getSamAccountName()).matches()) {
      throw ServiceException.badRequest(
          "SamAccountName contains illegal characters.", EC_ILLEGAL_SAM_ACCOUNT_NAME);
    }
  }

  boolean samAccountExists(SamAccount samAccount) {
    return findDnOfSamAccount(samAccount).isPresent();
  }

  boolean samAccountNameExists(String samAccountName) {
    return findDnOfSamAccountName(samAccountName).isPresent();
  }

  Optional<String> findDnOfSamAccount(SamAccount samAccount) {
    if (isEmpty(samAccount)) {
      return Optional.empty();
    }
    return findDnOfSamAccountName(samAccount.getSamAccountName());
  }

  Optional<String> findDnOfSamAccountName(String samAccountName) {
    log.debug("findDnOfSamAccountName({})", samAccountName);
    if (isEmpty(samAccountName)) {
      log.debug("Dn of '{}' does not exist", samAccountName);
      return Optional.empty();
    }
    SearchRequest searchRequest = SearchRequest.builder()
        .dn(getProperties().getBaseDn().format())
        .filter(new EqualityFilter(AdConstants.SAM_ACCOUNT_NAME.getName(), samAccountName))
        .scope(SearchScope.SUBTREE)
        .returnAttributes(AdConstants.DN.getName())
        .sizeLimit(1)
        .build();
    log.debug("findDnOfSamAccountName, searchRequest = {}", searchRequest);
    return getLdapTemplate().findOne(searchRequest)
        .map(LdapEntry::getDn)
        .filter(getIgnoredDnFilter());
  }

}
