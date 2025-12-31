package org.bremersee.samba.ad.dc.repository.cli;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.repository.HostNameSupplier;
import org.bremersee.samba.ad.dc.repository.cli.parser.HostNameParser;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HostNameSupplierCli extends CommandExecutor implements HostNameSupplier {

  public HostNameSupplierCli(ApplicationProperties properties) {
    super(properties);
  }

  @Override
  public String getHostName() {
    if (isEmpty(getProperties().getCli().getHostnameBinary())) {
      log.warn("HostnameBinary is not set. Returning 'localhost'.");
      return "localhost";
    }
    List<String> commands = new ArrayList<>(2);
    commands.add(getProperties().getCli().getHostnameBinary());
    if (!isEmpty(getProperties().getCli().getHostnameOptions())) {
      commands.add(getProperties().getCli().getHostnameOptions());
    }
    return executeAndGet(commands, HostNameParser.defaultParser());
  }

}
