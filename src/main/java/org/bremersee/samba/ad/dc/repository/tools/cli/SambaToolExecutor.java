package org.bremersee.samba.ad.dc.repository.tools.cli;

import java.util.ArrayList;
import java.util.List;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;

abstract class SambaToolExecutor extends CommandExecutor {

  SambaToolExecutor(DomainControllerProperties properties) {
    super(properties);
  }

  @Override
  <T> T executeAndGet(List<String> commands,
      CommandExecutorResponseParser<T> responseParser) {

    List<String> extendedCommands = new ArrayList<>(commands);
    if (needsSambaToolCredentials()) {
      extendedCommands.add(getProperties().getCli().getSambaToolCredentialsOptions());
    }
    return super.executeAndGet(extendedCommands, responseParser);
  }

  List<String> getCommands() {
    List<String> commands = new ArrayList<>();
    commands.add(getProperties().getCli().getSambaToolBinary());
    commands.add(getSubCommand());
    return commands;
  }

  abstract String getSubCommand();

  boolean needsSambaToolCredentials() {
    return false;
  }
}
