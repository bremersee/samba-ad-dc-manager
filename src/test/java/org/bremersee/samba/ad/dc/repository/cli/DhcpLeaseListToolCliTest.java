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

import java.util.List;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.DhcpLease;
import org.bremersee.samba.ad.dc.repository.cli.parser.DhcpLeaseParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The dhcp lease list tool cli test.
 *
 * @author Christian Bremer
 */
class DhcpLeaseListToolCliTest {

  private static final DhcpLeaseParser parser = DhcpLeaseParser.defaultParser();

  private DhcpLeaseListToolCli target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    target = spy(new DhcpLeaseListToolCli(properties));
    target.setParser(parser);
  }

  /**
   * Find active.
   */
  @Test
  void findActive() {
    doReturn(getCommandExecutorResponse())
        .when(target)
        .execute(anyList());
    List<DhcpLease> expected = getParsedResponse();
    List<DhcpLease> actual = target.findActive();
    assertThat(actual)
        .containsExactlyElementsOf(expected);
  }

  /**
   * Find active with no binary.
   */
  @Test
  void findActiveWithNoBinary() {
    target.getProperties().getCli().setDhcpLeaseListBinary("");
    List<DhcpLease> expected = List.of();
    List<DhcpLease> actual = target.findActive();
    assertThat(actual)
        .containsExactlyElementsOf(expected);
  }

  private static CommandExecutorResponse getCommandExecutorResponse() {
    String line0 = "MAC b8:xx:xx:xx:xx:xx "
        + "IP 192.168.1.109 "
        + "HOSTNAME ukelei "
        + "BEGIN 2019-08-18 11:20:33 "
        + "END 2019-08-18 11:50:33 "
        + "MANUFACTURER Apple, Inc."
        + "\n";
    String line1 = "MAC ac:xx:xx:xx:xx:yy "
        + "IP 192.168.1.188 "
        + "HOSTNAME -NA- "
        + "BEGIN 2019-08-18 11:25:48 "
        + "END 2019-08-18 11:55:48 "
        + "MANUFACTURER"
        + "\n";
    return new CommandExecutorResponse(line0 + line1, null);
  }

  private static List<DhcpLease> getParsedResponse() {
    return parser.parse(getCommandExecutorResponse());
  }
}