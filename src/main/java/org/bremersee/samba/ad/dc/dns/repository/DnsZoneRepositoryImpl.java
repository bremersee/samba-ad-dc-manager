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

package org.bremersee.samba.ad.dc.dns.repository;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.domain.repository.DomainRepository;
import org.bremersee.samba.ad.dc.dns.model.DnsZone;
import org.bremersee.samba.ad.dc.dns.model.DnsZoneType;
import org.bremersee.samba.ad.dc.common.repository.mapper.AdEntryLdapMapper;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * The type DnsRepositoryImpl.
 *
 * @author Christian Bremer
 */
@Component("dnsZoneRepository")
@Slf4j
public class DnsZoneRepositoryImpl implements DnsZoneRepository {

  private final LdaptiveTemplate ldapTemplate;

  private final DomainRepository domainRepository;

  private final AdEntryLdapMapper adEntryMapper;

  private final SambaToolDns dnsTool;

  public DnsZoneRepositoryImpl(
      LdaptiveTemplate ldapTemplate,
      DomainRepository domainRepository,
      SambaToolDns dnsTool) {
    this.ldapTemplate = ldapTemplate;
    this.domainRepository = domainRepository;
    this.dnsTool = dnsTool;
    this.adEntryMapper = new AdEntryLdapMapper();
  }

  @Cacheable(value = "dnsZoneListCache", key = "{ #p0 }")
  @Override
  public List<String> getDnsZoneNames(DnsZoneType zoneType) {
    log.debug("findDnsZoneNames({})", zoneType);
    return dnsTool.getDnsZoneNames(getHostName(), zoneType);
  }

  @Cacheable(value = "dnsZoneCache", key = "{ #p0 }")
  @Override
  public DnsZone getDnsZone(String zoneName) {
    log.debug("findDnsZone {}", zoneName);
    return doFindDnsZone(zoneName)
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            "DnsZone", zoneName, ErrorCode.EC_DNS_ZONE_NOT_FOUND));
  }

  private Optional<DnsZone> doFindDnsZone(String zoneName) {
    return dnsTool.findDnsZone(getHostName(), zoneName)
        .map(zone -> findLdapEntryOfDnsZone(zone.getDistinguishedName())
            .map(ldapEntry -> {
              adEntryMapper.map(ldapEntry, zone);
              return zone;
            })
            .orElse(zone));
  }

  private Optional<LdapEntry> findLdapEntryOfDnsZone(String dn) {
    SearchRequest searchRequest = SearchRequest.objectScopeSearchRequest(dn,
        adEntryMapper.getMappedAttributeNames());
    return ldapTemplate.findOne(searchRequest);
  }

  @CachePut(value = "dnsZoneCache", key = "{ #result.name }")
  @Override
  public DnsZone createDnsZone(String zoneName) {
    log.debug("createDnsZone {}", zoneName);
    dnsTool.createDnsZone(getHostName(), zoneName);
    return doFindDnsZone(zoneName)
        .orElseThrow(() -> ServiceException.internalServerError(
            String.format("Creating dns zone '%s' failed.", zoneName),
            ErrorCode.EC_CREATING_DNS_ZONE_FAILED));
  }

  @CacheEvict(value = "dnsZoneCache", key = "{ #p0 }")
  @Override
  public void deleteDnsZone(String zoneName) {
    log.debug("deleteDnsZone {}", zoneName);
    dnsTool.deleteDnsZone(getHostName(), zoneName);
  }

  private String getHostName() {
    return domainRepository.getHostName();
  }

}
