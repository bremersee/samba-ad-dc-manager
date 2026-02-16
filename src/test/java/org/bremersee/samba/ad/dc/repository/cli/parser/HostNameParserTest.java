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
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.junit.jupiter.api.Test;

/**
 * The host name parser test.
 *
 * @author Christian Bremer
 */
class HostNameParserTest {

  private static final HostNameParser target = HostNameParser.defaultParser();

  /**
   * Parse hostname.
   */
  @Test
  void parseHostname() {
    String expected = "dc1";
    CommandExecutorResponse response = new CommandExecutorResponse(expected, null);
    Optional<String> actual = target.parse(response);
    assertThat(actual)
        .hasValue(expected);
  }


  /**
   * Gets default value.
   */
  @Test
  void getDefaultValue() {
    CommandExecutorResponse response = new CommandExecutorResponse(null, "ERROR");
    Optional<String> actual = target.parse(response);
    assertThat(actual)
        .isEmpty();
  }
}