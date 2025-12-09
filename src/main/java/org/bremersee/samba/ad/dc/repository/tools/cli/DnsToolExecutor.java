package org.bremersee.samba.ad.dc.repository.tools.cli;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsEntryType;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.model.DnsZoneType;
import org.bremersee.samba.ad.dc.repository.tools.DnsTool;
import org.bremersee.samba.ad.dc.repository.tools.cli.parser.DnsEntriesParser;
import org.bremersee.samba.ad.dc.repository.tools.cli.parser.DnsZoneListParser;
import org.bremersee.samba.ad.dc.repository.tools.cli.parser.DnsZoneParser;
import org.bremersee.samba.ad.dc.repository.tools.cli.validator.DnsEntryAddValidator;
import org.bremersee.samba.ad.dc.repository.tools.cli.validator.DnsEntryDeleteValidator;
import org.bremersee.samba.ad.dc.repository.tools.cli.validator.DnsEntryUpdateValidator;
import org.bremersee.samba.ad.dc.repository.tools.cli.validator.DnsZoneCreateValidator;
import org.bremersee.samba.ad.dc.repository.tools.cli.validator.DnsZoneDeleteValidator;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class DnsToolExecutor extends SambaToolExecutor implements DnsTool {

  DnsToolExecutor(DomainControllerProperties properties) {
    super(properties);
  }

  @Override
  String getSubCommand() {
    return "dns";
  }

  @Override
  boolean needsSambaToolCredentials() {
    return true;
  }

  @Override
  public List<String> getDnsZoneNames(String hostName, DnsZoneType zoneType) {
    List<String> commands = getCommands();
    commands.add("zonelist");
    commands.add(hostName);
    commands.add("--" + zoneType.getParameterValue());
    return executeAndGet(commands, DnsZoneListParser.defaultParser());
  }

  @Override
  public Optional<DnsZone> findDnsZone(String hostName, String zoneName) {
    List<String> commands = getCommands();
    commands.add("zoneinfo");
    commands.add(hostName);
    commands.add(zoneName);
    return Optional.ofNullable(executeAndGet(commands, DnsZoneParser.defaultParser()));
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
    return executeAndGet(commands, DnsEntriesParser.defaultParser(zoneName, ZONE_ENTRIES_NODE_NAME))
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
    execute(commands, DnsEntryAddValidator.getInstance());
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
    execute(commands, DnsEntryUpdateValidator.getInstance());
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
    execute(commands, DnsEntryDeleteValidator.getInstance());
  }

}
