/*
 * Copyright 2025-2026 the original author or authors.
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


package org.bremersee.samba.ad.dc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.samba.ad.dc.model.DhcpLease;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsEntryType;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.model.DnsZoneType;
import org.bremersee.samba.ad.dc.repository.DhcpRepository;
import org.bremersee.samba.ad.dc.repository.DnsEntryRepository;
import org.bremersee.samba.ad.dc.repository.DnsZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * The dns service implementation test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class DnsServiceImplTest {

  private static final String ZONE = "example.org";

  private static final String REVERSE_ZONE = "1.168.192.in-addr.arpa";

  private DnsZoneRepository dnsZoneRepository;

  private DnsEntryRepository dnsEntryRepository;

  private DhcpRepository dhcpRepository;

  private DnsServiceImpl target;

  private DnsEntry ns;

  private DnsEntry host;

  private DnsEntry host9;

  private DnsEntry conflict;

  private DnsEntry www;

  private DnsEntry ptr;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    dnsZoneRepository = mock(DnsZoneRepository.class);
    dnsEntryRepository = mock(DnsEntryRepository.class);
    dhcpRepository = mock(DhcpRepository.class);
    target = new DnsServiceImpl(
        SortMapper.defaultSortMapper(), dnsZoneRepository, dnsEntryRepository, dhcpRepository);

    ns = entry(ZONE, DnsEntryRepository.ZONE_ENTRIES_NODE_NAME, DnsEntryType.NS, "dc.example.org.");
    host = entry(ZONE, "host", DnsEntryType.A, "192.168.1.10");
    host9 = entry(ZONE, "nine", DnsEntryType.A, "192.168.1.9");
    conflict = entry(ZONE, "host" + DnsEntry.CONFLICT_NAME_PART + UUID.randomUUID(),
        DnsEntryType.A, "192.168.1.10");
    www = entry(ZONE, "www", DnsEntryType.CNAME, "host.example.org.");
    ptr = entry(REVERSE_ZONE, "10", DnsEntryType.PTR, "host.example.org");

    lenient()
        .doReturn(List.of(ZONE))
        .when(dnsZoneRepository)
        .getDnsZoneNames(DnsZoneType.PRIMARY);
    lenient()
        .doReturn(List.of(REVERSE_ZONE))
        .when(dnsZoneRepository)
        .getDnsZoneNames(DnsZoneType.REVERSE);
    lenient()
        .doReturn(DnsZone.builder().name(ZONE).reverseZone(false).build())
        .when(dnsZoneRepository)
        .getDnsZone(ZONE);
    lenient()
        .doReturn(DnsZone.builder().name(REVERSE_ZONE).reverseZone(true).build())
        .when(dnsZoneRepository)
        .getDnsZone(REVERSE_ZONE);
    lenient()
        .doReturn(List.of(ns, host, host9, conflict, www))
        .when(dnsEntryRepository)
        .getDnsEntries(ZONE);
    lenient()
        .doReturn(List.of(ptr))
        .when(dnsEntryRepository)
        .getDnsEntries(REVERSE_ZONE);
    lenient()
        .doAnswer(invocation -> invocation.getArgument(0))
        .when(dnsEntryRepository)
        .addCommonAttributes(any());
  }

  private static DnsEntry entry(String zone, String name, DnsEntryType type, String value) {
    return DnsEntry.builder()
        .zoneName(zone)
        .name(name)
        .type(type)
        .value(value)
        .build();
  }

  /**
   * Gets dns zone names.
   *
   * @param softly the softly
   */
  @Test
  void getDnsZoneNames(SoftAssertions softly) {
    softly.assertThat(target.getDnsZoneNames(null))
        .containsExactly(ZONE);
    softly.assertThat(target.getDnsZoneNames(DnsZoneType.PRIMARY))
        .containsExactly(ZONE);
    softly.assertThat(target.getDnsZoneNames(DnsZoneType.REVERSE))
        .containsExactly(REVERSE_ZONE);
  }

  /**
   * Gets, creates and deletes dns zone.
   *
   * @param softly the softly
   */
  @Test
  void getCreateAndDeleteDnsZone(SoftAssertions softly) {
    softly.assertThat(target.getDnsZone(ZONE).getName())
        .isEqualTo(ZONE);

    DnsZone created = DnsZone.builder().name("new.org").build();
    doReturn(created).when(dnsZoneRepository).createDnsZone("new.org");
    softly.assertThat(target.createDnsZone("new.org"))
        .isEqualTo(created);

    target.deleteDnsZone("new.org");
    verify(dnsZoneRepository).deleteDnsZone("new.org");
  }

  /**
   * Gets dns entries.
   *
   * @param softly the softly
   */
  @Test
  void getDnsEntries(SoftAssertions softly) {
    Page<DnsEntry> actual = target.getDnsEntries(
        ZONE, PageRequest.of(0, 10, Sort.by("name")), null);
    softly.assertThat(actual.getTotalElements())
        .isEqualTo(5L);
    softly.assertThat(actual.getContent().getFirst())
        .as("Zone node '@' comes first when sorted by name")
        .isEqualTo(ns);

    actual = target.getDnsEntries(ZONE, PageRequest.of(0, 10, Sort.by("name")), "WWW");
    softly.assertThat(actual.getContent())
        .containsExactly(www);

    actual = target.getDnsEntries(ZONE, PageRequest.of(0, 10, Sort.by("name")), "cname");
    softly.assertThat(actual.getContent())
        .containsExactly(www);

    actual = target.getDnsEntries(ZONE, PageRequest.of(0, 10, Sort.by("value")), "192.168.1");
    softly.assertThat(actual.getContent())
        .as("IPv4 values are sorted numerically")
        .startsWith(host9)
        .containsExactlyInAnyOrder(host9, host, conflict);

    actual = target.getDnsEntries(ZONE, PageRequest.of(0, 10, Sort.by("type")), "nothing");
    softly.assertThat(actual.getContent())
        .isEmpty();

    actual = target.getDnsEntries(ZONE, PageRequest.of(0, 10, Sort.by("type")), "foo nine");
    softly.assertThat(actual.getContent())
        .containsExactly(host9);
  }

  /**
   * Find dns entry.
   *
   * @param softly the softly
   */
  @Test
  void findDnsEntry(SoftAssertions softly) {
    softly.assertThat(target.findDnsEntry(entry(ZONE, "HOST", DnsEntryType.A, "192.168.1.10")))
        .hasValue(host);
    softly.assertThat(target.findDnsEntry(
            entry(ZONE, "www", DnsEntryType.CNAME, "host.example.org")))
        .as("FQDN values are compared with trailing dot")
        .hasValue(www);
    softly.assertThat(target.findDnsEntry(entry(ZONE, "host", DnsEntryType.A, "192.168.1.11")))
        .isEmpty();
    softly.assertThat(target.findDnsEntry(
            entry(ZONE, DnsEntryRepository.ZONE_ENTRIES_NODE_NAME, DnsEntryType.ALL,
                "192.168.1.9")))
        .hasValue(host9);
  }

  /**
   * Find dns entry by ip address.
   *
   * @param softly the softly
   */
  @Test
  void findDnsEntryByIpAddress(SoftAssertions softly) {
    softly.assertThat(target.findDnsEntry("192.168.1.9"))
        .hasValue(host9);
    softly.assertThat(target.findDnsEntry("10.0.0.1"))
        .isEmpty();
    softly.assertThat(target.findDnsEntry("host.example.org."))
        .as("CNAME values are not matched")
        .isEmpty();
  }

  /**
   * Find reverse dns entry of A record.
   *
   * @param softly the softly
   */
  @Test
  void findReverseDnsEntryOfA(SoftAssertions softly) {
    softly.assertThat(target.findReverseDnsEntry(host))
        .hasValue(ptr);
    softly.assertThat(target.findReverseDnsEntry(host9))
        .isEmpty();
  }

  /**
   * Find reverse dns entry of PTR record.
   *
   * @param softly the softly
   */
  @Test
  void findReverseDnsEntryOfPtr(SoftAssertions softly) {
    softly.assertThat(target.findReverseDnsEntry(ptr))
        .hasValue(host);
    softly.assertThat(target.findReverseDnsEntry(
            entry(REVERSE_ZONE, "11", DnsEntryType.PTR, "host.example.org")))
        .isEmpty();
    softly.assertThat(target.findReverseDnsEntry(
            entry(ZONE, "10", DnsEntryType.PTR, "host.example.org")))
        .as("PTR record outside of a reverse zone")
        .isEmpty();
    softly.assertThat(target.findReverseDnsEntry(
            entry("168.192.in-addr.arpa", "10.1", DnsEntryType.PTR, "host.example.org")))
        .as("Short reverse zone of IPv4")
        .isEmpty();
    softly.assertThat(target.findReverseDnsEntry(
            entry("0.0.0.0.8.b.d.0.1.0.0.2.in-addr.arpa", "1", DnsEntryType.PTR,
                "host.example.org")))
        .as("Reverse zone of IPv6")
        .isEmpty();
  }

  /**
   * Find reverse dns entry of other record.
   */
  @Test
  void findReverseDnsEntryOfOther() {
    assertThat(target.findReverseDnsEntry(www))
        .isEmpty();
  }

  /**
   * Find dns entries conflicting with.
   *
   * @param softly the softly
   */
  @Test
  void findDnsEntriesConflictingWith(SoftAssertions softly) {
    softly.assertThat(target.findDnsEntriesConflictingWith(
            entry(ZONE, "other", DnsEntryType.A, "192.168.1.10")))
        .as("Entries marked as conflict are ignored")
        .containsExactly(host);
    softly.assertThat(target.findDnsEntriesConflictingWith(
            entry(ZONE, "nine", DnsEntryType.A, "10.0.0.1")))
        .containsExactly(host9);
    softly.assertThat(target.findDnsEntriesConflictingWith(
            entry(ZONE, "xyz", DnsEntryType.A, "10.0.0.1")))
        .isEmpty();
  }

  /**
   * Add, update and delete dns entry.
   */
  @Test
  void addUpdateAndDeleteDnsEntry() {
    target.addDnsEntry(host);
    verify(dnsEntryRepository).addDnsEntry(host);
    target.updateDnsEntry(host, "192.168.1.11");
    verify(dnsEntryRepository).updateDnsEntry(host, "192.168.1.11");
    target.deleteDnsEntry(host);
    verify(dnsEntryRepository).deleteDnsEntry(host);
  }

  /**
   * Gets dhcp leases.
   *
   * @param softly the softly
   */
  @Test
  void getDhcpLeases(SoftAssertions softly) {
    OffsetDateTime now = OffsetDateTime.now();
    DhcpLease ukelei = DhcpLease.builder()
        .hostname("ukelei")
        .ip("192.168.1.201")
        .mac("b8:00:00:00:00:01")
        .begin(now.minusMinutes(5))
        .end(now.plusMinutes(5))
        .manufacturer("Apple, Inc.")
        .build();
    DhcpLease forelle = DhcpLease.builder()
        .hostname("forelle")
        .ip("192.168.1.202")
        .mac("b7:00:00:00:00:02")
        .begin(now.minusMinutes(5))
        .end(now.plusMinutes(5))
        .build();
    doAnswer(invocation -> List.of(ukelei, forelle))
        .when(dhcpRepository)
        .findActive();

    Sort sort = Sort.by("hostname");
    softly.assertThat(target.getDhcpLeases(PageRequest.of(0, 10, sort), null).getContent())
        .containsExactly(forelle, ukelei);
    softly.assertThat(target.getDhcpLeases(PageRequest.of(0, 10, sort), "apple").getContent())
        .containsExactly(ukelei);
    softly.assertThat(target.getDhcpLeases(PageRequest.of(0, 10, sort), "B7:").getContent())
        .containsExactly(forelle);
    softly.assertThat(target.getDhcpLeases(PageRequest.of(0, 10, sort), ".202").getContent())
        .containsExactly(forelle);
    softly.assertThat(target.getDhcpLeases(PageRequest.of(0, 10, sort), "uke").getContent())
        .containsExactly(ukelei);
    softly.assertThat(target.getDhcpLeases(PageRequest.of(0, 10, sort), "none").getContent())
        .isEmpty();
  }

}
