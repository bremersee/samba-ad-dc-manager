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

package org.bremersee.samba.ad.dc.repository.cli.parser;

import static java.util.Objects.nonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsEntryType;
import org.bremersee.samba.ad.dc.model.ModifiableDnsEntry;
import org.bremersee.samba.ad.dc.repository.cli.AbstractCommandExecutorResponseParser;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponseParser;

/**
 * The dns entries parser parses the response of the linux command line tool
 * {@code samba-tool dns query <server> <zone> <name> <A|AAAA|PTR|CNAME|MX|NS|SOA|SRV|TXT|ALL>
 * [options]}*, for example {@code samba-tool dns query dc1 samdom.example.org @ ALL}.
 *
 * <p>A response of this command looks like this:
 * <pre>
 *   Name=, Records=3, Children=0
 *     SOA: serial=449810, refresh=900, retry=600, expire=86400, minttl=3600, ns=dc1.samdom.example.org., email=hostmaster.samdom.example.org. (flags=600000f0, serial=449809, ttl=3600)
 *     NS: dc1.samdom.example.org. (flags=600000f0, serial=449809, ttl=900)
 *     A: 192.168.1.3 (flags=600000f0, serial=449809, ttl=900)
 *   Name=_sites, Records=0, Children=1
 *   Name=_tcp, Records=0, Children=4
 *   Name=_udp, Records=0, Children=2
 *   Name=data, Records=1, Children=0
 *     A: 192.168.1.4 (flags=f0, serial=428109, ttl=3600)
 *   Name=ha, Records=1, Children=0
 *     A: 192.168.1.5 (flags=f0, serial=428110, ttl=3600)
 *   Name=proxy, Records=1, Children=0
 *     CNAME: ha.samdom.example.org. (flags=f0, serial=446881, ttl=900)
 * </pre>
 *
 * @author Christian Bremer
 */
public interface DnsEntriesParser
    extends CommandExecutorResponseParser<Stream<DnsEntry>> {

  /**
   * Returns a new default dns entries parser.
   *
   * @param zoneName the zone name
   * @param name the name
   * @return the dns entries parser
   */
  static DnsEntriesParser defaultParser(String zoneName, String name) {
    return new Default(zoneName, name);
  }

  /**
   * The default dns entries parser.
   *
   * @author Christian Bremer
   */
  @Slf4j
  class Default extends AbstractCommandExecutorResponseParser<Stream<DnsEntry>>
      implements DnsEntriesParser {

    private static final String NAME_KEY = "Name=";

    private static final String RECORDS = ", Records=";

    private static final String CONFLICT = DnsEntry.CONFLICT_IDENTIFIER;

    private static final char RECORD_LINE_INDICATOR = ':';

    private static final String FLAGS = "(flags=";

    private static final String SERIAL = ", serial=";

    private static final String TTL = ", ttl=";

    private static final char END = ')';

    private final String zoneName;

    private final String name;

    /**
     * Instantiates a new default dns entries parser.
     *
     * @param zoneName the zone name
     * @param name the name
     */
    Default(String zoneName, String name) {
      this.zoneName = zoneName;
      this.name = name;
    }

    @Override
    protected Stream<DnsEntry> getDefaultValue() {
      return Stream.empty();
    }

    @Override
    protected Stream<DnsEntry> doParse(BufferedReader reader) throws IOException {
      List<DnsEntry> entries = new ArrayList<>();
      ModifiableDnsEntry currentEntry = ModifiableDnsEntry.create();
      String line;
      while (nonNull(line = reader.readLine())) {
        line = line.trim();
        log.debug("Parsing line: {}", line);
        if (line.startsWith(NAME_KEY)) {
          parseName(line, currentEntry);
        } else if (currentEntry.nameIsSet()) {
          parseConflictOrDnsRecord(line, currentEntry)
              .ifPresent(entries::add);
        }
      }
      return entries.stream();
    }

    private void parseName(String line, ModifiableDnsEntry dnsEntry) {
      int end = line.indexOf(RECORDS);
      String currentName;
      if (end > 0) {
        currentName = line.substring(NAME_KEY.length(), end).trim();
      } else {
        currentName = line.substring(NAME_KEY.length()).trim();
      }
      if (currentName.isEmpty()) {
        currentName = this.name;
      }
      dnsEntry.setZoneName(zoneName)
          .setName(currentName);
    }

    private Optional<DnsEntry> parseConflictOrDnsRecord(String line, ModifiableDnsEntry dnsEntry) {
      int i0 = line.indexOf(RECORD_LINE_INDICATOR);
      if (i0 > 0) {
        String recordType = line.substring(0, i0).trim();
        if (CONFLICT.equalsIgnoreCase(recordType)) {
          parseConflict(line, dnsEntry, i0);
        } else {
          parseDnsRecord(line, dnsEntry);
          if (dnsEntry.isInitialized()) {
            Optional<DnsEntry> next = Optional.of(dnsEntry.toImmutable());
            String currentName = dnsEntry.getName();
            dnsEntry.clear()
                .setZoneName(zoneName)
                .setName(currentName);
            return next;
          }
        }
      }
      return Optional.empty();
    }

    private void parseConflict(String line, ModifiableDnsEntry dnsEntry, int i0) {
      int i1 = line.indexOf(RECORDS);
      StringBuilder sb = new StringBuilder(dnsEntry.getName())
          .append(DnsEntry.CONFLICT_NAME_PART);
      if (i1 > i0) {
        sb.append(line.substring(i0 + 1, i1).trim());
      } else {
        sb.append(line.substring(i0 + 1).trim());
      }
      dnsEntry.from(dnsEntry)
          .setName(sb.toString());
    }

    private void parseDnsRecord(String line, ModifiableDnsEntry dnsEntry) {
      int i0 = line.indexOf(RECORD_LINE_INDICATOR);
      if (i0 > 0) {
        parseType(line, dnsEntry, i0);
      }
    }

    private void parseType(String line, ModifiableDnsEntry dnsEntry, int i0) {
      dnsEntry.setType(DnsEntryType.fromValue(line.substring(0, i0).trim(), null));
      int i1 = line.indexOf(FLAGS, i0 + 1);
      if (i1 > i0) {
        parseValue(line, dnsEntry, i0, i1);
      }
    }

    private void parseValue(String line, ModifiableDnsEntry dnsEntry, int i0, int i1) {
      String value = line.substring(i0 + 1, i1).trim();
      dnsEntry.setValue(value);
      i0 = i1 + FLAGS.length();
      i1 = line.indexOf(SERIAL, i0);
      if (i1 > i0) {
        parseFlags(line, dnsEntry, i0, i1);
      }
    }

    private void parseFlags(String line, ModifiableDnsEntry dnsEntry, int i0, int i1) {
      String flags = line.substring(i0, i1).trim();
      dnsEntry.setFlags(flags);
      i0 = i1 + SERIAL.length();
      i1 = line.indexOf(TTL, i0);
      if (i1 > i0) {
        parseSerial(line, dnsEntry, i0, i1);
      }
    }

    private void parseSerial(String line, ModifiableDnsEntry dnsEntry, int i0, int i1) {
      String serial = line.substring(i0, i1).trim();
      try {
        dnsEntry.setSerial(Integer.parseInt(serial));
      } catch (NumberFormatException ignored) {
        // ignored
      }
      i0 = i1 + TTL.length();
      i1 = line.indexOf(END, i0);
      if (i1 > i0) {
        parseTtl(line, dnsEntry, i0, i1);
      }
    }

    private void parseTtl(String line, ModifiableDnsEntry dnsEntry, int i0, int i1) {
      String ttl = line.substring(i0, i1).trim();
      try {
        dnsEntry.setTtlSeconds(Integer.parseInt(ttl));
      } catch (NumberFormatException ignored) {
        // ignored
      }
    }

  }

}
