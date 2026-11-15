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
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.junit.jupiter.api.Test;

/**
 * The dns zone list parser test.
 *
 * @author Christian Bremer
 */
class DnsZoneListParserTest {

  private static final String SAMBA_TOOL_RESPONSE = """
        2 zone(s) found
      
        pszZoneName                 : 1.168.192.in-addr.arpa
        Flags                       : DNS_RPC_ZONE_DSINTEGRATED DNS_RPC_ZONE_UPDATE_SECURE
        ZoneType                    : DNS_ZONE_TYPE_PRIMARY
        Version                     : 50
        dwDpFlags                   : DNS_DP_AUTOCREATED DNS_DP_DOMAIN_DEFAULT DNS_DP_ENLISTED
        pszDpFqdn                   : DomainDnsZones.samdom.example.org
      
        pszZoneName                 : samdom.example.org
        Flags                       : DNS_RPC_ZONE_DSINTEGRATED DNS_RPC_ZONE_UPDATE_SECURE
        ZoneType                    : DNS_ZONE_TYPE_PRIMARY
        Version                     : 50
        dwDpFlags                   : DNS_DP_AUTOCREATED DNS_DP_DOMAIN_DEFAULT DNS_DP_ENLISTED
        pszDpFqdn                   : DomainDnsZones.samdom.example.org
      """;

  private static final DnsZoneListParser target = DnsZoneListParser.defaultParser();

  /**
   * Gets dns zone names.
   */
  @Test
  void getDnsZoneNames() {
    CommandExecutorResponse response = new CommandExecutorResponse(SAMBA_TOOL_RESPONSE, null);
    List<String> expected = List.of(
        "1.168.192.in-addr.arpa",
        "samdom.example.org"
    );
    List<String> actual = target.parse(response);
    assertThat(actual)
        .containsExactlyElementsOf(expected);
  }

  /**
   * Gets default value.
   */
  @Test
  void getDefaultValue() {
    CommandExecutorResponse response = new CommandExecutorResponse(null, "ERROR");
    List<String> actual = target.parse(response);
    assertThat(actual)
        .isEmpty();
  }
}