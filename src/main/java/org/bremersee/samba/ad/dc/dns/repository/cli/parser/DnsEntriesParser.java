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

package org.bremersee.samba.ad.dc.dns.repository.cli.parser;

import static java.util.Objects.nonNull;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.repository.cli.AbstractCommandExecutorResponseParser;
import org.bremersee.samba.ad.dc.dns.model.DnsEntry;
import org.bremersee.samba.ad.dc.dns.model.DnsEntryType;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponseParser;

/**
 * The interface DnsEntriesParser.
 *
 * @author Christian Bremer
 */
public interface DnsEntriesParser
    extends CommandExecutorResponseParser<Stream<DnsEntry>> {

  static DnsEntriesParser defaultParser(String zoneName, String name) {
    return new Default(zoneName, name);
  }

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
      Stream<DnsEntry> entries = Stream.empty();
      DnsEntry currentEntry = null;
      String line;
      while (nonNull(line = reader.readLine())) {
        line = line.trim();
        if (line.startsWith(NAME_KEY)) {
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
          currentEntry = DnsEntry.builder()
              .zoneName(zoneName)
              .name(currentName)
              .build(); // TODO type and value should not be nullable
        } else if (nonNull(currentEntry)) {
          int i0 = line.indexOf(RECORD_LINE_INDICATOR);
          if (i0 > 0) {
            String recordType = line.substring(0, i0).trim();
            if (CONFLICT.equalsIgnoreCase(recordType)) {
              int i1 = line.indexOf(RECORDS);
              StringBuilder sb = new StringBuilder(currentEntry.getName())
                  .append(DnsEntry.CONFLICT_NAME_PART);
              if (i1 > i0) {
                sb.append(line.substring(i0 + 1, i1).trim());
              } else {
                sb.append(line.substring(i0 + 1).trim());
              }
              currentEntry = DnsEntry.builder()
                  .from(currentEntry)
                  .name(sb.toString())
                  .build();
            } else {
              currentEntry = parseDnsRecord(line, currentEntry);
              String currentName = currentEntry.getName();
              if (!isEmpty(currentName) && !isEmpty(currentEntry.getType())
                  && !DnsEntryType.ALL.equals(currentEntry.getType())
                  && !isEmpty(currentEntry.getValue())) {
                entries = Stream.concat(entries, Stream.of(currentEntry));
                currentEntry = DnsEntry.builder()
                    .zoneName(zoneName)
                    .name(currentEntry.getName())
                    .build();
              }
            }
          }
        }
      }
      return entries;
    }

    private DnsEntry parseDnsRecord(String line, DnsEntry currentEntry) { // TODO use builder
      DnsEntry.Builder dnsEntryBuilder = DnsEntry.builder().from(currentEntry);
      int i0 = line.indexOf(RECORD_LINE_INDICATOR);
      if (i0 > 0) {
        dnsEntryBuilder.type(DnsEntryType.fromValue(line.substring(0, i0).trim(), null));
        int i1 = line.indexOf(FLAGS, i0 + 1);
        if (i1 > i0) {
          String value = line.substring(i0 + 1, i1).trim();
          dnsEntryBuilder.value(value);
          i0 = i1 + FLAGS.length();
          i1 = line.indexOf(SERIAL, i0);
          if (i1 > i0) {
            String flags = line.substring(i0, i1).trim();
            dnsEntryBuilder.flags(flags);
            i0 = i1 + SERIAL.length();
            i1 = line.indexOf(TTL, i0);
            if (i1 > i0) {
              String serial = line.substring(i0, i1).trim();
              try {
                dnsEntryBuilder.serial(Integer.parseInt(serial));
              } catch (NumberFormatException ignored) {
                // ignored
              }
              i0 = i1 + TTL.length();
              i1 = line.indexOf(END, i0);
              if (i1 > i0) {
                String ttl = line.substring(i0, i1).trim();
                try {
                  dnsEntryBuilder.ttlSeconds(Integer.parseInt(ttl));
                } catch (NumberFormatException ignored) {
                  // ignored
                }
              }
            }
          }
        }
      }
      return dnsEntryBuilder.build();
    }

  }

}
