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

import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.junit.jupiter.api.Test;

/**
 * The dns zone parser test.
 *
 * @author Christian Bremer
 */
class DnsZoneParserTest {

  private static final String SAMBA_TOOL_RESPONSE = """
        pszZoneName                 : samdom.example.org
        dwZoneType                  : DNS_ZONE_TYPE_PRIMARY
        fReverse                    : FALSE
        fAllowUpdate                : DNS_ZONE_UPDATE_SECURE
        fPaused                     : FALSE
        fShutdown                   : FALSE
        fAutoCreated                : FALSE
        fUseDatabase                : TRUE
        pszDataFile                 : None
        aipMasters                  : []
        fSecureSecondaries          : DNS_ZONE_SECSECURE_NO_XFER
        fNotifyLevel                : DNS_ZONE_NOTIFY_LIST_ONLY
        aipSecondaries              : []
        aipNotify                   : []
        fUseWins                    : FALSE
        fUseNbstat                  : FALSE
        fAging                      : FALSE
        dwNoRefreshInterval         : 168
        dwRefreshInterval           : 168
        dwAvailForScavengeTime      : 0
        aipScavengeServers          : []
        dwRpcStructureVersion       : 0x2
        dwForwarderTimeout          : 0
        fForwarderSlave             : 0
        aipLocalMasters             : []
        dwDpFlags                   : DNS_DP_AUTOCREATED DNS_DP_DOMAIN_DEFAULT DNS_DP_ENLISTED
        pszDpFqdn                   : DomainDnsZones.samdom.example.org
        pwszZoneDn                  : DC=samdom.example.org,CN=MicrosoftDNS,DC=DomainDnsZones,DC=samdom,DC=example,DC=org
        dwLastSuccessfulSoaCheck    : 0
        dwLastSuccessfulXfr         : 0
        fQueuedForBackgroundLoad    : FALSE
        fBackgroundLoadInProgress   : FALSE
        fReadOnlyZone               : FALSE
        dwLastXfrAttempt            : 0
        dwLastXfrResult             : 0
      """;

  private static final DnsZoneParser target = DnsZoneParser.defaultParser();

  /**
   * Parses dns zone.
   */
  @Test
  void parseDnsZone() {
    CommandExecutorResponse response = new CommandExecutorResponse(SAMBA_TOOL_RESPONSE, null);
    DnsZone expected = DnsZone.builder()
        .distinguishedName(
            "DC=samdom.example.org,CN=MicrosoftDNS,DC=DomainDnsZones,DC=samdom,DC=example,DC=org")
        .name("samdom.example.org")
        .zoneType("DNS_ZONE_TYPE_PRIMARY")
        .fqdn("DomainDnsZones.samdom.example.org")
        .reverseZone(false)
        .allowUpdate("DNS_ZONE_UPDATE_SECURE")
        .paused(false)
        .shutdown(false)
        .autoCreated(false)
        .useDatabase(true)
        .dataFile("None")
        .useWins(false)
        .useNbstat(false)
        .aging(false)
        .queuedForBackgroundLoad(false)
        .backgroundLoadInProgress(false)
        .readOnlyZone(false)
        .build();
    DnsZone actual = target.parse(response);
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Gets default value.
   */
  @Test
  void getDefaultValue() {
    CommandExecutorResponse response = new CommandExecutorResponse(null, "ERROR");
    DnsZone actual = target.parse(response);
    assertThat(actual)
        .isNull();
  }
}