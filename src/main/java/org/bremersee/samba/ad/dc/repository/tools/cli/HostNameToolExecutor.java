package org.bremersee.samba.ad.dc.repository.tools.cli;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.repository.tools.HostNameTool;
import org.bremersee.samba.ad.dc.repository.tools.cli.parser.HostNameResponseParser;
import org.springframework.stereotype.Component;

@Component
public class HostNameToolExecutor extends CommandExecutor implements HostNameTool {

  private String hostName;

  public HostNameToolExecutor(DomainControllerProperties properties) {
    super(properties);
  }

  @Override
  public String getHostName() {
    if (isEmpty(hostName)) {
      hostName = Optional.ofNullable(getProperties().getHostName())
          .filter(name -> !name.isBlank())
          .orElseGet(() -> {
            List<String> commands = new ArrayList<>(2);
            commands.add(getProperties().getCli().getHostnameBinary());
            if (!isEmpty(getProperties().getCli().getHostnameOptions())) {
              commands.add(getProperties().getCli().getHostnameOptions());
            }
            return executeAndGet(commands, HostNameResponseParser.defaultParser());
          });
    }
    return hostName;
  }


}
