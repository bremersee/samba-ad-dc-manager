package org.bremersee.samba.ad.dc.common.repository.cli;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import org.bremersee.samba.ad.dc.common.repository.cli.parser.HostNameResponseParser;
import org.bremersee.samba.ad.dc.common.repository.HostNameSupplier;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.springframework.stereotype.Component;

@Component
public class HostNameCliSupplier extends CommandExecutor implements HostNameSupplier {

  public HostNameCliSupplier(DomainControllerProperties properties) {
    super(properties);
  }

  @Override
  public String getHostName() {
    List<String> commands = new ArrayList<>(2);
    commands.add(getProperties().getCli().getHostnameBinary());
    if (!isEmpty(getProperties().getCli().getHostnameOptions())) {
      commands.add(getProperties().getCli().getHostnameOptions());
    }
    return executeAndGet(commands, HostNameResponseParser.defaultParser());
  }


}
