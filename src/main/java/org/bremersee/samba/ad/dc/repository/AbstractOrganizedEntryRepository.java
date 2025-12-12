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

import static java.util.Objects.requireNonNullElseGet;
import static org.bremersee.exception.ServiceException.badRequest;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.common.repository.AdRepository;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.model.NisDomainMember;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.EqualityFilter;
import org.ldaptive.filter.Filter;

/**
 * The type AbstractOrganizedEntryRepository.
 *
 * @author Christian Bremer
 */
@Slf4j
abstract class AbstractOrganizedEntryRepository extends AdRepository {

  /**
   * Instantiates a new abstract repository.
   *
   * @param properties the properties
   * @param ldapTemplate the ldap template
   */
  AbstractOrganizedEntryRepository(
      DomainControllerProperties properties,
      LdaptiveTemplate ldapTemplate) {
    super(properties, ldapTemplate);
  }

  abstract Dn getDefaultOu();

  abstract String getObjectClassValue();

  abstract String[] getBinaryAttributes();

  abstract String[] getReturnAttributes();

  protected String getUniqueNameAttributeName() {
    return AdConstants.SAM_ACCOUNT_NAME.getName();
  }

  Dn validateOu(Dn ou) {
    Dn ouDn = isEmpty(ou) || ou.isEmpty() ? getDefaultOu() : ou;
    if (isEmpty(ouDn) || ouDn.isEmpty()) {
      throw badRequest("Organizational unit cannot be empty.", EC_EMPTY_OU_RDN);
    }
    Dn dn = getProperties().getBaseDn(ouDn);
    if (!isEmpty(getLdapTemplate()) && !getLdapTemplate().exists(dn.format())) {
      throw badRequest(
          String.format("Organizational unit '%s' does not exist.", ouDn.format()),
          EC_OU_NOT_FOUND);
    }
    return ouDn;
  }

  String getNisDomain(NisDomainMember nisDomainMember) {
    return !isEmpty(nisDomainMember) && !isEmpty(nisDomainMember.getNisDomain())
        ? nisDomainMember.getNisDomain()
        : getProperties().getDomain().getDefaultNisDomain();
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

    if (getProperties().isDn(uniqueName)) {
      return SearchRequest.builder()
          .dn(uniqueName)
          .filter(objectClassFilter())
          .scope(SearchScope.OBJECT)
          .binaryAttributes(getBinaryAttributes())
          .returnAttributes(isEmpty(returnAttributes) ? getReturnAttributes() : returnAttributes)
          .sizeLimit(1)
          .build();
    }
    Dn ouDn = getProperties().getBaseDn(ouRdn);
    return SearchRequest.builder()
        .dn(ouDn.format())
        .filter(requireNonNullElseGet(filter, () -> findOneFilter(uniqueName)))
        .scope(Optional.ofNullable(scope)
            .filter(searchScope -> !ouDn.isSame(getProperties().getBaseDn()))
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
    Dn ouDn = getProperties().getBaseDn(ouRdn);
    return SearchRequest.builder()
        .dn(ouDn.format())
        .filter(filter)
        .scope(Optional.ofNullable(scope)
            .filter(searchScope -> !ouDn.isSame(getProperties().getBaseDn()))
            .orElse(SearchScope.SUBTREE))
        .binaryAttributes(getBinaryAttributes())
        .returnAttributes(isEmpty(returnAttributes) ? getReturnAttributes() : returnAttributes)
        .build();
  }

}
