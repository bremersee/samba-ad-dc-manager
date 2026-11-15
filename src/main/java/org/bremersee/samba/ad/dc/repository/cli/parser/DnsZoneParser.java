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

package org.bremersee.samba.ad.dc.repository.cli.parser;

import java.util.Optional;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponseParser;

/**
 * The dns zone parser parses Linux command line tool
 * {@code samba-tool dns zoneinfo <server> <zone> [options]}, for example
 * {@code samba-tool dns zoneinfo dc1 samdom.example.org}.
 *
 * <p>A response of this command looks like this:
 * <pre>
 *   pszZoneName                 : samdom.example.org
 *   dwZoneType                  : DNS_ZONE_TYPE_PRIMARY
 *   fReverse                    : FALSE
 *   fAllowUpdate                : DNS_ZONE_UPDATE_SECURE
 *   fPaused                     : FALSE
 *   fShutdown                   : FALSE
 *   fAutoCreated                : FALSE
 *   fUseDatabase                : TRUE
 *   pszDataFile                 : None
 *   aipMasters                  : []
 *   fSecureSecondaries          : DNS_ZONE_SECSECURE_NO_XFER
 *   fNotifyLevel                : DNS_ZONE_NOTIFY_LIST_ONLY
 *   aipSecondaries              : []
 *   aipNotify                   : []
 *   fUseWins                    : FALSE
 *   fUseNbstat                  : FALSE
 *   fAging                      : FALSE
 *   dwNoRefreshInterval         : 168
 *   dwRefreshInterval           : 168
 *   dwAvailForScavengeTime      : 0
 *   aipScavengeServers          : []
 *   dwRpcStructureVersion       : 0x2
 *   dwForwarderTimeout          : 0
 *   fForwarderSlave             : 0
 *   aipLocalMasters             : []
 *   dwDpFlags                   : DNS_DP_AUTOCREATED DNS_DP_DOMAIN_DEFAULT DNS_DP_ENLISTED
 *   pszDpFqdn                   : DomainDnsZones.samdom.example.org
 *   pwszZoneDn                  : DC=samdom.example.org,CN=MicrosoftDNS,DC=DomainDnsZones,DC=samdom,DC=example,DC=org
 *   dwLastSuccessfulSoaCheck    : 0
 *   dwLastSuccessfulXfr         : 0
 *   fQueuedForBackgroundLoad    : FALSE
 *   fBackgroundLoadInProgress   : FALSE
 *   fReadOnlyZone               : FALSE
 *   dwLastXfrAttempt            : 0
 *   dwLastXfrResult             : 0
 * </pre>
 *
 * @author Christian Bremer
 */
public interface DnsZoneParser extends CommandExecutorResponseParser<Optional<DnsZone>> {

  /**
   * Returns the default dns zone parser.
   *
   * @return the dns zone parser
   */
  static DnsZoneParser defaultParser() {
    return DefaultDnsZoneParser.getInstance();
  }

}
