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

package org.bremersee.samba.ad.dc.repository.cli;

import static java.util.Objects.nonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsEntryType;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.model.DnsZoneType;
import org.bremersee.samba.ad.dc.repository.SambaToolDns;
import org.bremersee.samba.ad.dc.repository.cli.parser.DnsEntriesParser;
import org.bremersee.samba.ad.dc.repository.cli.parser.DnsZoneListParser;
import org.bremersee.samba.ad.dc.repository.cli.parser.DnsZoneParser;
import org.bremersee.samba.ad.dc.repository.cli.validator.DnsEntryAddValidator;
import org.bremersee.samba.ad.dc.repository.cli.validator.DnsEntryDeleteValidator;
import org.bremersee.samba.ad.dc.repository.cli.validator.DnsEntryUpdateValidator;
import org.bremersee.samba.ad.dc.repository.cli.validator.DnsZoneCreateValidator;
import org.bremersee.samba.ad.dc.repository.cli.validator.DnsZoneDeleteValidator;
import org.springframework.stereotype.Component;

/**
 * The samba tool dns cli.
 *
 * @author Christian Bremer
 */
@Component
class SambaToolDnsCli extends SambaToolCli implements SambaToolDns {

  private DnsZoneListParser zoneListParser;

  private DnsZoneParser zoneParser;

  private Function<String, DnsEntriesParser> dnsEntriesParserFn;

  /**
   * Instantiates a new samba tool dns cli.
   *
   * @param properties the properties
   */
  SambaToolDnsCli(ApplicationProperties properties) {
    super(properties);
    zoneListParser = DnsZoneListParser.defaultParser();
    zoneParser = DnsZoneParser.defaultParser();
    dnsEntriesParserFn = zoneName -> DnsEntriesParser
        .defaultParser(zoneName, ZONE_ENTRIES_NODE_NAME);
  }

  /**
   * Sets zone list parser.
   *
   * @param zoneListParser the zone list parser
   */
  public void setZoneListParser(DnsZoneListParser zoneListParser) {
    if (nonNull(zoneListParser)) {
      this.zoneListParser = zoneListParser;
    }
  }

  /**
   * Sets zone parser.
   *
   * @param zoneParser the zone parser
   */
  public void setZoneParser(DnsZoneParser zoneParser) {
    if (nonNull(zoneParser)) {
      this.zoneParser = zoneParser;
    }
  }

  /**
   * Sets dns entries parser function.
   *
   * @param dnsEntriesParserFn the dns entries parser function
   */
  public void setDnsEntriesParserFn(Function<String, DnsEntriesParser> dnsEntriesParserFn) {
    if (nonNull(dnsEntriesParserFn)) {
      this.dnsEntriesParserFn = dnsEntriesParserFn;
    }
  }

  @Override
  protected String getSubCommand() {
    return "dns";
  }

  @Override
  protected boolean needsSambaToolCredentials() {
    return true;
  }

  @Override
  public List<String> getDnsZoneNames(String hostName, DnsZoneType zoneType) {
    List<String> commands = getCommands();
    commands.add("zonelist");
    commands.add(hostName);
    commands.add("--" + zoneType.getParameterValue());
    return executeAndGet(commands, zoneListParser);
  }

  @Override
  public Optional<DnsZone> findDnsZone(String hostName, String zoneName) {
    List<String> commands = getCommands();
    commands.add("zoneinfo");
    commands.add(hostName);
    commands.add(zoneName);
    return executeAndGet(commands, zoneParser);
  }

  @Override
  public void createDnsZone(String hostName, String zoneName) {
    List<String> commands = getCommands();
    commands.add("zonecreate");
    commands.add(hostName);
    commands.add(zoneName);
    execute(commands, new DnsZoneCreateValidator(zoneName));
  }

  @Override
  public void deleteDnsZone(String hostName, String zoneName) {
    List<String> commands = getCommands();
    commands.add("zonedelete");
    commands.add(hostName);
    commands.add(zoneName);
    execute(commands, new DnsZoneDeleteValidator(zoneName));
  }

  @Override
  public List<DnsEntry> getDnsEntries(String hostName, String zoneName) {
    List<String> commands = getCommands();
    commands.add("query");
    commands.add(hostName);
    commands.add(zoneName);
    commands.add(ZONE_ENTRIES_NODE_NAME);
    commands.add(DnsEntryType.ALL.name());
    return executeAndGet(commands, dnsEntriesParserFn.apply(zoneName))
        .toList();
  }

  @Override
  public void addDnsEntry(String hostName, DnsEntry entry) {
    List<String> commands = getCommands();
    commands.add("add");
    commands.add(hostName);
    commands.add(entry.getZoneName());
    commands.add(entry.getName());
    commands.add(entry.getType().name());
    commands.add(entry.getType().getToSambaToolValueTransformer().apply(entry.getValue()));
    execute(commands, new DnsEntryAddValidator());
  }

  @Override
  public void updateDnsEntry(String hostName, DnsEntry entry, String newValue) {
    List<String> commands = getCommands();
    commands.add("update");
    commands.add(hostName);
    commands.add(entry.getZoneName());
    commands.add(entry.getName());
    commands.add(entry.getType().name());
    commands.add(entry.getType().getToSambaToolValueTransformer().apply(entry.getValue()));
    commands.add(entry.getType().getToSambaToolValueTransformer().apply(newValue));
    execute(commands, new DnsEntryUpdateValidator());
  }

  @Override
  public void deleteDnsEntry(String hostName, DnsEntry entry) {
    List<String> commands = getCommands();
    commands.add("delete");
    commands.add(hostName);
    commands.add(entry.getZoneName());
    commands.add(entry.getName());
    commands.add(entry.getType().name());
    commands.add(entry.getType().getToSambaToolValueTransformer().apply(entry.getValue()));
    execute(commands, new DnsEntryDeleteValidator());
  }

}
