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

package org.bremersee.samba.ad.dc.common.repository;

import static java.util.Objects.nonNull;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.common.DefaultDnTool;
import org.bremersee.samba.ad.dc.common.DnTool;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.common.model.DistinguishedNameProvider;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.ldaptive.filter.PresenceFilter;
import org.springframework.util.Assert;

/**
 * The common repository.
 *
 * @author Christian Bremer
 */
@Slf4j
public abstract class AdRepository implements ErrorCode {

  private static final Dn[] IGNORED_DN = new Dn[]{
      new Dn("CN=Builtin"),
      new Dn("CN=Configuration"),
      new Dn("CN=Infrastructure"),
      new Dn("CN=LostAndFound"),
      new Dn("CN=NTDS Quotas"),
  };

  @Getter(AccessLevel.PROTECTED)
  private final DomainControllerProperties properties;

  @Getter(AccessLevel.PROTECTED)
  private final DnTool dnTool;

  @Getter(AccessLevel.PROTECTED)
  private final LdaptiveTemplate ldapTemplate;

  @Getter(AccessLevel.PROTECTED)
  private final Predicate<String> ignoredDnFilter;

  @Getter(AccessLevel.PROTECTED)
  private final Predicate<LdapEntry> ignoredEntryFilter;

  @Getter(AccessLevel.PROTECTED)
  private final Predicate<DistinguishedNameProvider> ignoredObjectFilter;

  /**
   * Instantiates a new common repository.
   *
   * @param properties the properties
   * @param ldapTemplate the ldap template
   */
  protected AdRepository(
      DomainControllerProperties properties,
      LdaptiveTemplate ldapTemplate) {

    Assert.notNull(properties, "Domain controller properties must be present.");
    this.properties = properties;
    this.dnTool = new DefaultDnTool(properties);
    this.ldapTemplate = ldapTemplate;
    this.ignoredDnFilter = dn -> isEmpty(dn) || Arrays
        .stream(IGNORED_DN)
        .map(this.properties::getBaseDn)
        .noneMatch(ignoredDn -> ignoredDn.isAncestor(new Dn(dn)));
    this.ignoredEntryFilter = entry -> ignoredDnFilter.test(entry.getDn());
    this.ignoredObjectFilter = distinguishedNameProvider -> ignoredDnFilter
        .test(distinguishedNameProvider.getDistinguishedName());
  }

  private boolean isIgnoredDnFilterRequired(Dn ou, SearchScope scope) {
    if (nonNull(scope) && scope != SearchScope.SUBTREE) {
      return false;
    }
    return DnTool.isSameDn(dnTool.getBaseDn(), dnTool.addBaseDn(ou));
  }

  protected Predicate<String> getIgnoredDnFilter(Dn ou, SearchScope scope) {
    if (isIgnoredDnFilterRequired(ou, scope)) {
      return getIgnoredDnFilter();
    }
    return dn -> true;
  }

  protected Predicate<LdapEntry> getIgnoredEntryFilter(Dn ou, SearchScope scope) {
    if (isIgnoredDnFilterRequired(ou, scope)) {
      return getIgnoredEntryFilter();
    }
    return entry -> true;
  }

  protected Predicate<DistinguishedNameProvider> getIgnoredObjectFilter(Dn ou, SearchScope scope) {
    if (isIgnoredDnFilterRequired(ou, scope)) {
      return getIgnoredObjectFilter();
    }
    return distinguishedNameProvider -> true;
  }

  protected boolean dnExistsWithAnyObjectClass(String dn, String... objectClasses) {
    log.debug("dnExistsWithAnyObjectClass({}, {})", dn, objectClasses);
    if (!getProperties().isDn(dn)) {
      log.debug("Dn '{}' does not exist", dn);
      return false;
    }
    SearchRequest searchRequest = SearchRequest.objectScopeSearchRequest(
        dn,
        new String[]{AdConstants.OBJECT_CLASS.getName()},
        new PresenceFilter(AdConstants.OBJECT_CLASS.getName()));
    log.debug("dnExistsWithAnyObjectClass, searchRequest = {}", searchRequest);
    return getLdapTemplate().findOne(searchRequest)
        .filter(getIgnoredEntryFilter())
        .map(ldapEntry -> {
          Set<String> wantedObjectClasses = Stream.ofNullable(objectClasses)
              .flatMap(Arrays::stream)
              .filter(cls -> !isEmpty(cls))
              .map(String::toLowerCase)
              .collect(Collectors.toSet());
          if (wantedObjectClasses.isEmpty()) {
            return true;
          }
          return Stream
              .ofNullable(ldapEntry.getAttribute(AdConstants.OBJECT_CLASS.getName()))
              .map(LdapAttribute::getStringValues)
              .flatMap(Collection::stream)
              .filter(cls -> !isEmpty(cls))
              .map(String::toLowerCase)
              .anyMatch(wantedObjectClasses::contains);
        })
        .orElse(false);
  }

}
