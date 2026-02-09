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

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.repository.mapper.AdEntryLdapMapper;
import org.ldaptive.DeleteRequest;
import org.ldaptive.SearchRequest;
import org.ldaptive.dn.Dn;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * The type DnsRepositoryImpl.
 *
 * @author Christian Bremer
 */
@Component("dnsEntryRepository")
@Slf4j
public class DnsEntryRepositoryImpl extends AdRepository implements DnsEntryRepository {

  private final AdEntryLdapMapper adEntryMapper;

  private final DomainRepository domainRepository;

  private final DnsZoneRepository dnsZoneRepository;

  private final SambaToolDns dnsTool;

  public DnsEntryRepositoryImpl(
      ApplicationProperties properties,
      LdaptiveOperations ldapOperations,
      DomainRepository domainRepository,
      DnsZoneRepository dnsZoneRepository,
      SambaToolDns dnsTool) {
    super(properties, ldapOperations);
    this.domainRepository = domainRepository;
    this.dnsZoneRepository = dnsZoneRepository;
    this.dnsTool = dnsTool;
    this.adEntryMapper = new AdEntryLdapMapper();
  }

  @Cacheable(value = "dnsEntryListCache", key = "{ #p0 }")
  @Override
  public List<DnsEntry> getDnsEntries(String zoneName) {
    log.debug("findDnsEntries({})", zoneName);
    validateExistence(zoneName);
    return dnsTool.getDnsEntries(getHostName(), zoneName);
  }

  @Override
  public DnsEntry addCommonAttributes(DnsEntry entry) {
    Dn dn = new Dn("DC=" + entry.getName());
    dn.add(dnsZoneRepository.getDnsZone(entry.getZoneName()).getDn());
    String[] returnAttributes = adEntryMapper.getMappedAttributeNames();
    SearchRequest searchRequest = SearchRequest
        .objectScopeSearchRequest(dn.format(), returnAttributes);
    return getLdapOperations().findOne(searchRequest)
        .map(ldapEntry -> DnsEntry.builder()
            .from(entry).from(adEntryMapper.map(ldapEntry))
            .build())
        .orElse(entry);
  }

  @CacheEvict(value = "dnsEntryListCache", allEntries = true)
  @Override
  public void addDnsEntry(DnsEntry entry) {
    log.debug("addDnsEntry({})", entry);
    validateDnsZoneName(entry);
    if (isEmpty(entry.getType()) || !entry.getType().isAddable()) {
      throw getDnsTypeNotSupportedException(entry);
    }
    dnsTool.addDnsEntry(getHostName(), entry);
  }

  @CacheEvict(value = "dnsEntryListCache", allEntries = true)
  @Override
  public void updateDnsEntry(DnsEntry entry, String newValue) {
    log.debug("updateDnsEntry {}, {}", entry, newValue);
    validateDnsZoneName(entry);
    if (isEmpty(entry.getType()) || !entry.getType().isUpdatable()) {
      throw getDnsTypeNotSupportedException(entry);
    }
    dnsTool.updateDnsEntry(getHostName(), entry, newValue);
  }

  @CacheEvict(value = "dnsEntryListCache", allEntries = true)
  @Override
  public void deleteDnsEntry(DnsEntry entry) {
    validateDnsZoneName(entry);
    if (entry.isConflict()) {
      deleteDnsEntryConflict(entry);
      return;
    }
    log.debug("deleteDnsEntry({})", entry);
    if (isEmpty(entry.getType()) || !entry.getType().isAddable()) {
      throw getDnsTypeNotSupportedException(entry);
    }
    dnsTool.deleteDnsEntry(getHostName(), entry);
  }

  private void deleteDnsEntryConflict(DnsEntry entry) {
    log.debug("deleteDnsEntryConflict({})", entry);
    Dn dn = new Dn("DC=" + entry.getName());
    dn.add(dnsZoneRepository.getDnsZone(entry.getZoneName()).getDn());
    getLdapOperations().delete(DeleteRequest.builder()
        .dn(dn.format())
        .build());
  }

  private String getHostName() {
    return domainRepository.getHostName();
  }

  private void validateDnsZoneName(DnsEntry dnsEntry) {
    if (isEmpty(dnsEntry.getZoneName())) {
      throw ServiceException
          .badRequest("DNS zone name is required.", ErrorCode.EC_DNS_ZONE_REQUIRED);
    }
    validateExistence(dnsEntry.getZoneName());
  }

  private void validateExistence(String zoneName) {
    if (dnsTool.findDnsZone(getHostName(), zoneName).isEmpty()) {
      throw ServiceException.notFoundWithErrorCode(
          DnsZone.class.getSimpleName(),
          zoneName,
          ErrorCode.EC_DNS_ZONE_NOT_FOUND);
    }
  }

  private ServiceException getDnsTypeNotSupportedException(DnsEntry entry) {
    return ServiceException.badRequest(
        String.format("Dns entry type '%s' is not supported.", entry.getType()),
        ErrorCode.EC_ILLEGAL_DNS_ENTRY_TYPE);
  }

}
