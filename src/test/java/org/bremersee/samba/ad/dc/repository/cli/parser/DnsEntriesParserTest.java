/*
 * Copyright 2026 the original author or authors.
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

package org.bremersee.samba.ad.dc.repository.cli.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsEntryType;
import org.bremersee.samba.ad.dc.repository.SambaToolDns;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.junit.jupiter.api.Test;

/**
 * The dns entries parser test.
 *
 * @author Christian Bremer
 */
class DnsEntriesParserTest {

  private static final String SAMBA_TOOL_RESPONSE = """
        Name=, Records=3, Children=0
          SOA: serial=449810, refresh=900, retry=600, expire=86400, minttl=3600, ns=dc1.samdom.example.org., email=hostmaster.samdom.example.org. (flags=600000f0, serial=449809, ttl=3600)
          NS: dc1.samdom.example.org. (flags=600000f0, serial=449809, ttl=900)
          A: 192.168.1.3 (flags=600000f0, serial=449809, ttl=900)
        Name=_sites, Records=0, Children=1
        Name=_tcp, Records=0, Children=4
        Name=_udp, Records=0, Children=2
        Name=data, Records=1, Children=0
          A: 192.168.1.4 (flags=f0, serial=428109, ttl=3600)
        Name=ha, Records=1, Children=0
          A: 192.168.1.5 (flags=f0, serial=428110, ttl=3600)
        Name=proxy, Records=1, Children=0
          CNAME: ha.samdom.example.org. (flags=f0, serial=446881, ttl=900)
        Name=phone, Records=1, Children=0
      CNF:a3a5e6d1-f667-4c6d-ba0b-fc7cb780719f
          A: 192.168.1.234 (flags=f0, serial=446890, ttl=3600)
      """;

  private static final DnsEntriesParser target = DnsEntriesParser
      .defaultParser("samdom.example.org", SambaToolDns.ZONE_ENTRIES_NODE_NAME);

  /**
   * Parse zone entries.
   */
  @Test
  void parseZoneEntries() {
    CommandExecutorResponse response = new CommandExecutorResponse(SAMBA_TOOL_RESPONSE, null);
    List<DnsEntry> expected = List.of(
        DnsEntry.builder()
            .zoneName("samdom.example.org")
            .name(SambaToolDns.ZONE_ENTRIES_NODE_NAME)
            .type(DnsEntryType.SOA)
            .value(
                "serial=449810, refresh=900, retry=600, expire=86400, minttl=3600, ns=dc1.samdom.example.org., email=hostmaster.samdom.example.org.")
            .flags("600000f0")
            .serial(449809)
            .ttlSeconds(3600)
            .build(),
        DnsEntry.builder()
            .zoneName("samdom.example.org")
            .name(SambaToolDns.ZONE_ENTRIES_NODE_NAME)
            .type(DnsEntryType.NS)
            .value("dc1.samdom.example.org.")
            .flags("600000f0")
            .serial(449809)
            .ttlSeconds(900)
            .build(),
        DnsEntry.builder()
            .zoneName("samdom.example.org")
            .name(SambaToolDns.ZONE_ENTRIES_NODE_NAME)
            .type(DnsEntryType.A)
            .value("192.168.1.3")
            .flags("600000f0")
            .serial(449809)
            .ttlSeconds(900)
            .build(),
        DnsEntry.builder()
            .zoneName("samdom.example.org")
            .name("data")
            .type(DnsEntryType.A)
            .value("192.168.1.4")
            .flags("f0")
            .serial(428109)
            .ttlSeconds(3600)
            .build(),
        DnsEntry.builder()
            .zoneName("samdom.example.org")
            .name("ha")
            .type(DnsEntryType.A)
            .value("192.168.1.5")
            .flags("f0")
            .serial(428110)
            .ttlSeconds(3600)
            .build(),
        DnsEntry.builder()
            .zoneName("samdom.example.org")
            .name("proxy")
            .type(DnsEntryType.CNAME)
            .value("ha.samdom.example.org.")
            .flags("f0")
            .serial(446881)
            .ttlSeconds(900)
            .build(),
        DnsEntry.builder()
            .zoneName("samdom.example.org")
            .name("phone" + DnsEntry.CONFLICT_NAME_PART + "a3a5e6d1-f667-4c6d-ba0b-fc7cb780719f")
            .type(DnsEntryType.A)
            .value("192.168.1.234")
            .flags("f0")
            .serial(446890)
            .ttlSeconds(3600)
            .build()
    );
    List<DnsEntry> actual = target.parse(response).toList();
    assertThat(actual)
        .containsExactlyElementsOf(expected);
  }

  /**
   * Gets default value.
   */
  @Test
  void getDefaultValue() {
    CommandExecutorResponse response = new CommandExecutorResponse(null, "ERROR");
    Stream<DnsEntry> actual = target.parse(response);
    assertThat(actual)
        .isEmpty();
  }
}