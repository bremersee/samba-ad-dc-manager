package org.bremersee.samba.ad.dc.repository.cli;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.model.DhcpLease;
import org.bremersee.samba.ad.dc.repository.DhcpLeaseListTool;
import org.bremersee.samba.ad.dc.repository.cli.parser.DhcpLeaseParser;
import org.springframework.stereotype.Component;

@Component
class DhcpLeaseListToolCli extends CommandExecutor implements DhcpLeaseListTool {

  DhcpLeaseListToolCli(DomainControllerProperties properties) {
    super(properties);
  }

  @Override
  public List<DhcpLease> findActive() {
    if (isEmpty(getProperties().getCli().getDhcpLeaseListBinary())) {
      return List.of();
    }
    List<String> commands = new ArrayList<>();
    commands.add(getProperties().getCli().getDhcpLeaseListBinary());
    commands.add("--parsable");
    return executeAndGet(commands, DhcpLeaseParser.defaultParser());
  }
}
