package org.bremersee.samba.ad.dc.repository.cli;

import java.util.ArrayList;
import java.util.List;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;

public abstract class SambaToolCli extends CommandExecutor {

  protected SambaToolCli(DomainControllerProperties properties) {
    super(properties);
  }

  @Override
  public <T> T executeAndGet(List<String> commands,
      CommandExecutorResponseParser<T> responseParser) {

    List<String> extendedCommands = new ArrayList<>(commands);
    if (needsSambaToolCredentials()) {
      extendedCommands.add(getProperties().getCli().getSambaToolCredentialsOptions());
    }
    return super.executeAndGet(extendedCommands, responseParser);
  }

  protected List<String> getCommands() {
    List<String> commands = new ArrayList<>();
    commands.add(getProperties().getCli().getSambaToolBinary());
    commands.add(getSubCommand());
    return commands;
  }

  protected abstract String getSubCommand();

  protected boolean needsSambaToolCredentials() {
    return false;
  }
}
