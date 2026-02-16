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

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;

/**
 * The samba tool cli.
 *
 * @author Christian Bremer
 */
@Slf4j
public abstract class SambaToolCli extends CommandExecutor {

  /**
   * Instantiates a new samba tool cli.
   *
   * @param properties the properties
   */
  protected SambaToolCli(ApplicationProperties properties) {
    super(properties);
    log.info("Samba tool is using binary {} with sub command {}",
        properties.getCli().getSambaToolBinary(), getSubCommand());
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

  /**
   * Gets commands.
   *
   * @return the commands
   */
  protected List<String> getCommands() {
    List<String> commands = new ArrayList<>();
    commands.add(getProperties().getCli().getSambaToolBinary());
    commands.add(getSubCommand());
    return commands;
  }

  /**
   * Gets sub command.
   *
   * @return the sub command
   */
  protected abstract String getSubCommand();

  /**
   * Specifies whether the samba tool command needs credentials or not.
   *
   * @return the boolean
   */
  protected boolean needsSambaToolCredentials() {
    return false;
  }
}
