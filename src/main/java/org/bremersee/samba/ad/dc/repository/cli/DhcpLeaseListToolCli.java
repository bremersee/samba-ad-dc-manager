/*
 * Copyright 2019-2026 the original author or authors.
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
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.DhcpLease;
import org.bremersee.samba.ad.dc.repository.DhcpLeaseListTool;
import org.bremersee.samba.ad.dc.repository.cli.parser.DhcpLeaseParser;
import org.springframework.stereotype.Component;

/**
 * The dhcp lease list tool cli.
 *
 * @author Christian Bremer
 */
@Component
@Slf4j
class DhcpLeaseListToolCli extends CommandExecutor implements DhcpLeaseListTool {

  private DhcpLeaseParser parser;

  /**
   * Instantiates a new dhcp lease list tool cli.
   *
   * @param properties the properties
   */
  DhcpLeaseListToolCli(ApplicationProperties properties) {
    super(properties);
    parser = DhcpLeaseParser.defaultParser();
    log.info("Dhcp lease list tool is using binary {}",
        properties.getCli().getDhcpLeaseListBinary());
  }

  /**
   * Sets parser.
   *
   * @param parser the parser
   */
  public void setParser(DhcpLeaseParser parser) {
    if (nonNull(parser)) {
      this.parser = parser;
    }
  }

  @Override
  public List<DhcpLease> findActive() {
    if (isEmpty(getProperties().getCli().getDhcpLeaseListBinary())) {
      log.warn("DhcpLeaseListBinary is not set. Returning an empty dhcp lease list.");
      return List.of();
    }
    List<String> commands = new ArrayList<>();
    commands.add(getProperties().getCli().getDhcpLeaseListBinary());
    commands.add("--parsable");
    return executeAndGet(commands, parser);
  }
}
