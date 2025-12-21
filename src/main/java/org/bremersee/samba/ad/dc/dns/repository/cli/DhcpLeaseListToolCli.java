package org.bremersee.samba.ad.dc.dns.repository.cli;

import java.util.ArrayList;
import java.util.List;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutor;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.dns.model.DhcpLease;
import org.bremersee.samba.ad.dc.dns.repository.DhcpLeaseListTool;
import org.bremersee.samba.ad.dc.dns.repository.cli.parser.DhcpLeaseParser;
import org.springframework.stereotype.Component;

@Component
class DhcpLeaseListToolCli extends CommandExecutor implements DhcpLeaseListTool {

  DhcpLeaseListToolCli(DomainControllerProperties properties) {
    super(properties);
  }

  @Override
  public List<DhcpLease> findActive() {
    List<String> commands = new ArrayList<>();
    commands.add(getProperties().getCli().getDhcpLeaseListBinary());
    commands.add("--parsable");
    return executeAndGet(commands, DhcpLeaseParser.defaultParser());
  }
}
