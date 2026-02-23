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
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.model.DnsZoneType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;

/**
 * The dns zone repository implementation test.
 *
 * @author Christian Bremer
 */
class DnsZoneRepositoryImplTest {

  private LdaptiveOperations ldapOperations;

  private DomainRepository domainRepository;

  private SambaToolDns dnsTool;

  private DnsZoneRepositoryImpl target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    ldapOperations = mock(LdaptiveOperations.class);
    domainRepository = mock(DomainRepository.class);
    dnsTool = mock(SambaToolDns.class);
    target = new DnsZoneRepositoryImpl(ldapOperations, domainRepository, dnsTool);
  }

  /**
   * Gets dns zone names.
   */
  @Test
  void getDnsZoneNames() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();
    DnsZoneType dnsZoneType = DnsZoneType.PRIMARY;
    List<String> expected = List.of("intranet");
    doReturn(expected)
        .when(dnsTool)
        .getDnsZoneNames(hostName, dnsZoneType);
    List<String> actual = target.getDnsZoneNames(dnsZoneType);
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Gets dns zone.
   */
  @Test
  void getDnsZone() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();

    String zoneName = "intranet";
    DnsZone dnsZone = DnsZone.builder()
        .distinguishedName("CN=intranet")
        .name(zoneName)
        .build();
    doReturn(Optional.of(dnsZone))
        .when(dnsTool)
        .findDnsZone(hostName, zoneName);

    LdapEntry ldapEntry = new LdapEntry();
    ldapEntry.setDn("CN=intranet");
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

    DnsZone expected = DnsZone.builder()
        .from(dnsZone)
        .created(created)
        .modified(changed)
        .build();
    DnsZone actual = target.getDnsZone(zoneName);
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Gets dns zone not found.
   */
  @Test
  void getDnsZoneNotFound() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();

    String zoneName = "intranet";
    doReturn(Optional.empty())
        .when(dnsTool)
        .findDnsZone(hostName, zoneName);

    assertThatExceptionOfType(ServiceException.class)
        .isThrownBy(() -> target.getDnsZone(zoneName));
  }

  /**
   * Create dns zone.
   */
  @Test
  void createDnsZone() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();

    String zoneName = "intranet";
    DnsZone dnsZone = DnsZone.builder()
        .distinguishedName("CN=intranet")
        .name(zoneName)
        .build();
    doReturn(Optional.of(dnsZone))
        .when(dnsTool)
        .findDnsZone(hostName, zoneName);

    LdapEntry ldapEntry = new LdapEntry();
    ldapEntry.setDn("CN=intranet");
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

    DnsZone expected = DnsZone.builder()
        .from(dnsZone)
        .created(created)
        .modified(changed)
        .build();
    DnsZone actual = target.createDnsZone(zoneName);
    assertThat(actual)
        .isEqualTo(expected);
    verify(dnsTool).createDnsZone(hostName, zoneName);
  }

  /**
   * Create dns zone failed.
   */
  @Test
  void createDnsZoneFailed() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();

    String zoneName = "intranet";
    doReturn(Optional.empty())
        .when(dnsTool)
        .findDnsZone(hostName, zoneName);

    assertThatExceptionOfType(ServiceException.class)
        .isThrownBy(() -> target.createDnsZone(zoneName));
  }

  /**
   * Delete dns zone.
   */
  @Test
  void deleteDnsZone() {
    String hostName = "dc";
    doReturn(hostName)
        .when(domainRepository)
        .getHostName();

    String zoneName = "intranet";
    target.deleteDnsZone(zoneName);
    verify(dnsTool).deleteDnsZone(hostName, zoneName);
  }
}