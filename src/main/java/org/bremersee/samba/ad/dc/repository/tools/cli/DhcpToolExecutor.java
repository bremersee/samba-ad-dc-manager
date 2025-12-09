package org.bremersee.samba.ad.dc.repository.tools.cli;

import java.util.ArrayList;
import java.util.List;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.model.DhcpLease;
import org.bremersee.samba.ad.dc.repository.tools.DhcpTool;
import org.bremersee.samba.ad.dc.repository.tools.cli.parser.DhcpLeaseParser;
import org.springframework.stereotype.Component;

@Component
class DhcpToolExecutor extends CommandExecutor implements DhcpTool {

  DhcpToolExecutor(DomainControllerProperties properties) {
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
