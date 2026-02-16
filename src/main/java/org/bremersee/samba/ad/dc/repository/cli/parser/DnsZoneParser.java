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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.repository.cli.AbstractCommandExecutorResponseParser;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponseParser;

/**
 * The dns zone list parser parses linux command line tool
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
    return Default.getInstance();
  }

  /**
   * The default dns zone parser.
   */
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @Slf4j
  class Default extends AbstractCommandExecutorResponseParser<Optional<DnsZone>>
      implements DnsZoneParser {

    private static final String ZONE_NAME = "pszZoneName";

    private static final String ZONE_TYPE = "dwZoneType";

    private static final String IS_REVERSE = "fReverse";

    private static final String ALLOW_UPDATE = "fAllowUpdate";

    private static final String IS_PAUSED = "fPaused";

    private static final String IS_SHUTDOWN = "fShutdown";

    private static final String IS_AUTO_CREATED = "fAutoCreated";

    private static final String IS_USING_DATABASE = "fUseDatabase";

    private static final String DATA_FILE = "pszDataFile";

    private static final String IS_USING_WINS = "fUseWins";

    private static final String IS_USING_NBSTAT = "fUseNbstat";

    private static final String IS_AGING = "fAging";

    private static final String FQDN = "pszDpFqdn";

    private static final String ZONE_DN = "pwszZoneDn";

    private static final String IS_QUEUED_FOR_BACKGROUND_LOAD = "fQueuedForBackgroundLoad";

    private static final String IS_BACKGROUND_LOAD_IN_PRPGRESS = "fBackgroundLoadInProgress";

    private static final String IS_READ_ONLY_ZONE = "fReadOnlyZone";

    private static DnsZoneParser instance;

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static DnsZoneParser getInstance() {
      if (instance == null) {
        instance = new Default();
      }
      return instance;
    }

    @Override
    protected Optional<DnsZone> getDefaultValue() {
      return Optional.empty();
    }

    protected Optional<DnsZone> doParse(BufferedReader reader) throws IOException {
      DnsZone.Builder zone = DnsZone.builder();
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        log.debug("Parsing line: {}", line);
        int index = line.indexOf(':');
        parseZoneName(line, zone, index);
        parseZoneType(line, zone, index);
        parseIsReverse(line, zone, index);
        parseAllowUpdate(line, zone, index);
        parsePaused(line, zone, index);
        parseShutdown(line, zone, index);
        parseAutoCreated(line, zone, index);
        parseUseDatabase(line, zone, index);
        parseDataFile(line, zone, index);
        parseWins(line, zone, index);
        parseNbstat(line, zone, index);
        parseAging(line, zone, index);
        parseFqdn(line, zone, index);
        parseDistinguishedName(line, zone, index);
        parseQueuedForBackgroundLoad(line, zone, index);
        parseBackgroundLoadInProgress(line, zone, index);
        parseReadOnlyZone(line, zone, index);
      }
      return Optional.of(zone.build());
    }

    private void parseZoneName(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, ZONE_NAME, index)) {
        zone.name(line.substring(index + 1).trim());
      }
    }

    private void parseZoneType(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, ZONE_TYPE, index)) {
        zone.zoneType(line.substring(index + 1).trim());
      }
    }

    private void parseIsReverse(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, IS_REVERSE, index)) {
        String value = line.substring(index + 1).trim();
        zone.reverseZone(Boolean.parseBoolean(value));
      }
    }

    private void parseAllowUpdate(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, ALLOW_UPDATE, index)) {
        zone.allowUpdate(line.substring(index + 1).trim());
      }
    }

    private void parsePaused(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, IS_PAUSED, index)) {
        String value = line.substring(index + 1).trim();
        zone.paused(Boolean.parseBoolean(value));
      }
    }

    private void parseShutdown(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, IS_SHUTDOWN, index)) {
        String value = line.substring(index + 1).trim();
        zone.shutdown(Boolean.parseBoolean(value));
      }
    }

    private void parseAutoCreated(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, IS_AUTO_CREATED, index)) {
        String value = line.substring(index + 1).trim();
        zone.autoCreated(Boolean.parseBoolean(value));
      }
    }

    private void parseUseDatabase(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, IS_USING_DATABASE, index)) {
        String value = line.substring(index + 1).trim();
        zone.useDatabase(Boolean.parseBoolean(value));
      }
    }

    private void parseDataFile(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, DATA_FILE, index)) {
        zone.dataFile(line.substring(index + 1).trim());
      }
    }

    private void parseWins(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, IS_USING_WINS, index)) {
        String value = line.substring(index + 1).trim();
        zone.useWins(Boolean.parseBoolean(value));
      }
    }

    private void parseNbstat(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, IS_USING_NBSTAT, index)) {
        String value = line.substring(index + 1).trim();
        zone.useNbstat(Boolean.parseBoolean(value));
      }
    }

    private void parseAging(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, IS_AGING, index)) {
        String value = line.substring(index + 1).trim();
        zone.aging(Boolean.parseBoolean(value));
      }
    }

    private void parseFqdn(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, FQDN, index)) {
        zone.fqdn(line.substring(index + 1).trim());
      }
    }

    private void parseDistinguishedName(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, ZONE_DN, index)) {
        String dn = line.substring(index + 1).trim();
        zone.distinguishedName(dn);
      }
    }

    private void parseQueuedForBackgroundLoad(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, IS_QUEUED_FOR_BACKGROUND_LOAD, index)) {
        String value = line.substring(index + 1).trim();
        zone.queuedForBackgroundLoad(Boolean.parseBoolean(value));
      }
    }

    private void parseBackgroundLoadInProgress(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, IS_BACKGROUND_LOAD_IN_PRPGRESS, index)) {
        String value = line.substring(index + 1).trim();
        zone.backgroundLoadInProgress(Boolean.parseBoolean(value));
      }
    }

    private void parseReadOnlyZone(String line, DnsZone.Builder zone, int index) {
      if (lineContains(line, IS_READ_ONLY_ZONE, index)) {
        String value = line.substring(index + 1).trim();
        zone.readOnlyZone(Boolean.parseBoolean(value));
      }
    }

    private static boolean lineContains(String line, String name, int index) {
      return line.contains(name) && index >= 0 && index < line.length();
    }

  }

}
