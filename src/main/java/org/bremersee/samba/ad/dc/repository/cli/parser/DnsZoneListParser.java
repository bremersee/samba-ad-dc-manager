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

import java.util.List;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponseParser;

/**
 * The dns zone list parser parses Linux command line tool
 * {@code samba-tool dns zonelist <server> [options]}, for example
 * {@code samba-tool dns zonelist dc1 --primary}.
 *
 * <p>A response of this command looks like this:
 * <pre>
 *   2 zone(s) found
 *
 *   pszZoneName                 : 1.168.192.in-addr.arpa
 *   Flags                       : DNS_RPC_ZONE_DSINTEGRATED DNS_RPC_ZONE_UPDATE_SECURE
 *   ZoneType                    : DNS_ZONE_TYPE_PRIMARY
 *   Version                     : 50
 *   dwDpFlags                   : DNS_DP_AUTOCREATED DNS_DP_DOMAIN_DEFAULT DNS_DP_ENLISTED
 *   pszDpFqdn                   : DomainDnsZones.samdom.example.org
 *
 *   pszZoneName                 : samdom.example.org
 *   Flags                       : DNS_RPC_ZONE_DSINTEGRATED DNS_RPC_ZONE_UPDATE_SECURE
 *   ZoneType                    : DNS_ZONE_TYPE_PRIMARY
 *   Version                     : 50
 *   dwDpFlags                   : DNS_DP_AUTOCREATED DNS_DP_DOMAIN_DEFAULT DNS_DP_ENLISTED
 *   pszDpFqdn                   : DomainDnsZones.samdom.example.org
 * </pre>
 *
 * @author Christian Bremer
 */
public interface DnsZoneListParser extends CommandExecutorResponseParser<List<String>> {

  /**
   * Retruns the default dns zone list parser.
   *
   * @return the dns zone list parser
   */
  static DnsZoneListParser defaultParser() {
    return DefaultDnsZoneListParser.getInstance();
  }

}
