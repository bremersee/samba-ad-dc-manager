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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsEntryType;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;

/**
 * The dns entry repository implementation test.
 *
 * @author Christian Bremer
 */
class DnsEntryRepositoryImplTest {

  private LdaptiveOperations ldapOperations;

  private DomainRepository domainRepository;

  private DnsZoneRepository dnsZoneRepository;

  private SambaToolDns dnsTool;

  private DnsEntryRepositoryImpl target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    ldapOperations = mock(LdaptiveOperations.class);
    domainRepository = mock(DomainRepository.class);
    dnsZoneRepository = mock(DnsZoneRepository.class);
    dnsTool = mock(SambaToolDns.class);
    target = new DnsEntryRepositoryImpl(
        properties,
        ldapOperations,
        domainRepository,
        dnsZoneRepository,
        dnsTool);
  }

  /**
   * Gets dns entries.
   */
  @Test
  void getDnsEntries() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();
    String zoneName = "intranet";
    DnsZone dnsZone = DnsZone.builder()
        .name(zoneName)
        .build();
    doReturn(Optional.of(dnsZone))
        .when(dnsTool)
        .findDnsZone(hostName, zoneName);
    List<DnsEntry> expected = List.of(DnsEntry.builder()
        .zoneName(zoneName)
        .name("dc")
        .type(DnsEntryType.A)
        .value("192.168.1.3")
        .build());
    doReturn(expected)
        .when(dnsTool)
        .getDnsEntries(hostName, zoneName);
    List<DnsEntry> actual = target.getDnsEntries(zoneName);
    assertThat(actual)
        .containsExactlyInAnyOrderElementsOf(expected);
  }

  /**
   * Gets dns entries with invalid zone name.
   */
  @Test
  void getDnsEntriesWithInvalidZoneName() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();
    String zoneName = "intranet";
    doReturn(Optional.empty())
        .when(dnsTool)
        .findDnsZone(hostName, zoneName);
    assertThatExceptionOfType(ServiceException.class)
        .isThrownBy(() -> target.getDnsEntries(zoneName));
  }

  /**
   * Add common attributes.
   */
  @Test
  void addCommonAttributes() {
    String zoneName = "intranet";
    DnsZone dnsZone = DnsZone.builder()
        .name(zoneName)
        .distinguishedName("CN=intranet")
        .build();
    doReturn(dnsZone)
        .when(dnsZoneRepository)
        .getDnsZone(zoneName);

    LdapEntry ldapEntry = new LdapEntry();
    ldapEntry.setDn("CN=dc");
    OffsetDateTime created = OffsetDateTime.now(ZoneOffset.UTC)
        .minusMinutes(5L)
        .truncatedTo(ChronoUnit.SECONDS);
    AdConstants.WHEN_CREATED.setValue(ldapEntry, created);
    OffsetDateTime changed = OffsetDateTime.now(ZoneOffset.UTC)
        .truncatedTo(ChronoUnit.SECONDS);
    AdConstants.WHEN_CHANGED.setValue(ldapEntry, changed);
    doReturn(Optional.of(ldapEntry))
        .when(ldapOperations)
        .findOne(any(SearchRequest.class));

    DnsEntry source = DnsEntry.builder()
        .zoneName(zoneName)
        .name("dc")
        .type(DnsEntryType.A)
        .value("192.168l.1.4")
        .build();
    DnsEntry expected = DnsEntry.builder()
        .from(source)
        .distinguishedName("CN=dc")
        .created(created)
        .modified(changed)
        .build();
    DnsEntry actual = target.addCommonAttributes(source);
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Add dns entry.
   */
  @Test
  void addDnsEntry() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();
    String zoneName = "intranet";
    DnsZone dnsZone = DnsZone.builder()
        .name(zoneName)
        .build();
    doReturn(Optional.of(dnsZone))
        .when(dnsTool)
        .findDnsZone(hostName, zoneName);

    DnsEntry dnsEntry = DnsEntry.builder()
        .zoneName(zoneName)
        .name("dc")
        .type(DnsEntryType.A)
        .value("192.168l.1.4")
        .build();
    target.addDnsEntry(dnsEntry);
    verify(dnsTool).addDnsEntry(hostName, dnsEntry);
  }

  /**
   * Add dns entry with invalid type.
   */
  @Test
  void addDnsEntryWithInvalidType() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();
    String zoneName = "intranet";
    DnsZone dnsZone = DnsZone.builder()
        .name(zoneName)
        .build();
    doReturn(Optional.of(dnsZone))
        .when(dnsTool)
        .findDnsZone(hostName, zoneName);

    DnsEntry dnsEntry = DnsEntry.builder()
        .zoneName(zoneName)
        .name("dc")
        .type(DnsEntryType.SOA)
        .value("192.168l.1.4")
        .build();
    assertThatExceptionOfType(ServiceException.class)
        .isThrownBy(() -> target.addDnsEntry(dnsEntry));
  }

  /**
   * Add dns entry without zone name.
   */
  @Test
  void addDnsEntryWithoutZoneName() {
    DnsEntry dnsEntry = DnsEntry.builder()
        .name("dc")
        .type(DnsEntryType.SOA)
        .value("192.168l.1.4")
        .build();
    assertThatExceptionOfType(ServiceException.class)
        .isThrownBy(() -> target.addDnsEntry(dnsEntry));
  }

  /**
   * Update dns entry.
   */
  @Test
  void updateDnsEntry() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();
    String zoneName = "intranet";
    DnsZone dnsZone = DnsZone.builder()
        .name(zoneName)
        .build();
    doReturn(Optional.of(dnsZone))
        .when(dnsTool)
        .findDnsZone(hostName, zoneName);

    DnsEntry dnsEntry = DnsEntry.builder()
        .zoneName(zoneName)
        .name("dc")
        .type(DnsEntryType.A)
        .value("192.168l.1.4")
        .build();
    String newValue = "192.168.1.5";
    target.updateDnsEntry(dnsEntry, newValue);
    verify(dnsTool).updateDnsEntry(hostName, dnsEntry, newValue);
  }

  /**
   * Update dns entry with invalid type.
   */
  @Test
  void updateDnsEntryWithInvalidType() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();
    String zoneName = "intranet";
    DnsZone dnsZone = DnsZone.builder()
        .name(zoneName)
        .build();
    doReturn(Optional.of(dnsZone))
        .when(dnsTool)
        .findDnsZone(hostName, zoneName);

    DnsEntry dnsEntry = DnsEntry.builder()
        .zoneName(zoneName)
        .name("dc")
        .type(DnsEntryType.NULL)
        .value("192.168l.1.4")
        .build();
    String newValue = "192.168.1.5";
    assertThatExceptionOfType(ServiceException.class)
        .isThrownBy(() -> target.updateDnsEntry(dnsEntry, newValue));
  }

  /**
   * Delete dns entry.
   */
  @Test
  void deleteDnsEntry() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();
    String zoneName = "intranet";
    DnsZone dnsZone = DnsZone.builder()
        .name(zoneName)
        .build();
    doReturn(Optional.of(dnsZone))
        .when(dnsTool)
        .findDnsZone(hostName, zoneName);

    DnsEntry dnsEntry = DnsEntry.builder()
        .zoneName(zoneName)
        .name("dc")
        .type(DnsEntryType.A)
        .value("192.168l.1.4")
        .build();
    target.deleteDnsEntry(dnsEntry);
    verify(dnsTool).deleteDnsEntry(hostName, dnsEntry);
  }

  /**
   * Delete dns entry with invalid type.
   */
  @Test
  void deleteDnsEntryWithInvalidType() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();
    String zoneName = "intranet";
    DnsZone dnsZone = DnsZone.builder()
        .name(zoneName)
        .build();
    doReturn(Optional.of(dnsZone))
        .when(dnsTool)
        .findDnsZone(hostName, zoneName);

    DnsEntry dnsEntry = DnsEntry.builder()
        .zoneName(zoneName)
        .name("dc")
        .type(DnsEntryType.SOA)
        .value("192.168l.1.4")
        .build();
    assertThatExceptionOfType(ServiceException.class)
        .isThrownBy(() -> target.deleteDnsEntry(dnsEntry));
  }

  /**
   * Delete dns entry with conflict.
   */
  @Test
  void deleteDnsEntryWithConflict() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();
    String zoneName = "intranet";
    DnsZone dnsZone = DnsZone.builder()
        .name(zoneName)
        .build();
    doReturn(Optional.of(dnsZone))
        .when(dnsTool)
        .findDnsZone(hostName, zoneName);

    doReturn(dnsZone)
        .when(dnsZoneRepository)
        .getDnsZone(zoneName);

    UUID id = UUID.randomUUID();
    DnsEntry dnsEntry = DnsEntry.builder()
        .zoneName(zoneName)
        .name("dc" + DnsEntry.CONFLICT_NAME_PART + id)
        .type(DnsEntryType.A)
        .value("192.168l.1.4")
        .build();
    target.deleteDnsEntry(dnsEntry);
    verify(ldapOperations).delete(any(DeleteRequest.class));
  }

}