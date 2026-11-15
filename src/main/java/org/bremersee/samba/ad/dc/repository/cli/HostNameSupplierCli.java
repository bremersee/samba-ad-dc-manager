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

import static java.util.Objects.nonNull;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.repository.HostNameSupplier;
import org.bremersee.samba.ad.dc.repository.cli.parser.HostNameParser;
import org.springframework.stereotype.Component;

/**
 * The host name supplier cli.
 *
 * @author Christian Bremer
 */
@Component
@Slf4j
public class HostNameSupplierCli extends CommandExecutor implements HostNameSupplier {

  private HostNameParser parser;

  /**
   * Instantiates a new host name supplier cli.
   *
   * @param properties the properties
   */
  public HostNameSupplierCli(ApplicationProperties properties) {
    super(properties);
    parser = HostNameParser.defaultParser();
    log.info("Hostname supplier is using binary {}", properties.getCli().getHostnameBinary());
  }

  /**
   * Sets parser.
   *
   * @param parser the parser
   */
  public void setParser(HostNameParser parser) {
    if (nonNull(parser)) {
      this.parser = parser;
    }
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
    return executeAndGet(commands, parser)
        .orElseThrow(() -> ServiceException.internalServerError(
            "Getting hostname failed.",
            ErrorCode.EC_GETTING_HOSTNAME_FAILED));
  }

}
