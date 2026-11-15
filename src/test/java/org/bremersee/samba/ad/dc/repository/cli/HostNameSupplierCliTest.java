/*
 * Copyright 2026 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Optional;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.repository.cli.parser.HostNameParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The host name supplier cli test.
 *
 * @author Christian Bremer
 */
class HostNameSupplierCliTest {

  private static final HostNameParser parser = HostNameParser.defaultParser();

  private HostNameSupplierCli target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    properties.getCli().setHostnameOptions("--fqdn");
    target = spy(new HostNameSupplierCli(properties));
    target.setParser(parser);
  }

  /**
   * Get hostname.
   */
  @Test
  void getHostName() {
    doReturn(getCommandExecutorResponse())
        .when(target)
        .execute(anyList());
    String expected = getParsedResponse().orElse(null);
    assertThat(expected)
        .isNotNull();
    String actual = target.getHostName();
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Get hostname with no binary.
   */
  @Test
  void getHostNameWithNoBinary() {
    target.getProperties().getCli().setHostnameBinary("");
    String expected = "localhost";
    String actual = target.getHostName();
    assertThat(actual)
        .isEqualTo(expected);
  }

  private static CommandExecutorResponse getCommandExecutorResponse() {
    return new CommandExecutorResponse("dc1\n", null);
  }

  private static Optional<String> getParsedResponse() {
    return parser.parse(getCommandExecutorResponse());
  }
}