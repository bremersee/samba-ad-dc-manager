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

package org.bremersee.samba.ad.dc.repository.cli.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.junit.jupiter.api.Test;

/**
 * The domain info parser test.
 *
 * @author Christian Bremer
 */
class DomainInfoParserTest {

  private static final String SAMBA_TOOL_RESPONSE = """
      Forest           : samdom.example.org
      Domain           : samdom.example.org
      Netbios domain   : SAMDOM
      DC name          : dc1.samdom.example.org
      DC netbios name  : DC1
      Server site      : Default-First-Site-Name
      Client site      : Default-First-Site-Name
      """;

  private static final DomainInfoParser target = DomainInfoParser.defaultParser();

  /**
   * Parse domain info.
   */
  @Test
  void parseDomainInfo() {
    CommandExecutorResponse response = new CommandExecutorResponse(SAMBA_TOOL_RESPONSE, null);
    DomainInfo expected = DomainInfo.builder()
        .forest("samdom.example.org")
        .domain("samdom.example.org")
        .netbiosDomain("SAMDOM")
        .domainControllerName("dc1.samdom.example.org")
        .domainControllerNetbiosName("DC1")
        .serverSite("Default-First-Site-Name")
        .clientSite("Default-First-Site-Name")
        .build();
    Optional<DomainInfo> actual = target.parse(response);
    assertThat(actual)
        .hasValue(expected);
  }


  /**
   * Gets default value.
   */
  @Test
  void getDefaultValue() {
    CommandExecutorResponse response = new CommandExecutorResponse(null, "ERROR");
    Optional<DomainInfo> actual = target.parse(response);
    assertThat(actual).isEmpty();
  }
}