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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.repository.cli.parser.DomainInfoParser;
import org.bremersee.samba.ad.dc.repository.cli.parser.PasswordInformationParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The samba tool domain cli test.
 *
 * @author Christian Bremer
 */
class SambaToolDomainCliTest {

  private static final DomainInfoParser infoParser = DomainInfoParser.defaultParser();

  private static final PasswordInformationParser passwordParser = PasswordInformationParser
      .defaultParser();

  private SambaToolDomainCli target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    target = spy(new SambaToolDomainCli(properties));
    target.setDomainInfoParser(infoParser);
    target.setPasswordInformationParser(passwordParser);
  }

  /**
   * Gets sub command.
   */
  @Test
  void getSubCommand() {
    String actual = target.getSubCommand();
    assertThat(actual)
        .isEqualTo("domain");
  }

  /**
   * Needs samba tool credentials.
   */
  @Test
  void needsSambaToolCredentials() {
    assertThat(target.needsSambaToolCredentials())
        .isFalse();
  }

  /**
   * Gets domain info.
   */
  @Test
  void getDomainInfo() {
    CommandExecutorResponse response = getDomainInfoResponse();
    doReturn(response)
        .when(target)
        .execute(anyList());
    DomainInfo expected = infoParser.parse(response).orElse(null);
    assertThat(expected)
        .isNotNull();
    DomainInfo actual = target.getDomainInfo("dc1");
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Gets domain info not found.
   */
  @Test
  void getDomainInfoNotFound() {
    CommandExecutorResponse response = new CommandExecutorResponse(null, "Host not found.");
    doReturn(response)
        .when(target)
        .execute(anyList());
    assertThatExceptionOfType(ServiceException.class)
        .isThrownBy(() -> target.getDomainInfo("dc2"));
  }

  /**
   * Gets password information.
   */
  @Test
  void getPasswordInformation() {
    CommandExecutorResponse response = getPasswordInfoResponse();
    doReturn(response)
        .when(target)
        .execute(anyList());
    PasswordInformation expected = passwordParser.parse(response);
    PasswordInformation actual = target.getPasswordInformation();
    assertThat(actual)
        .isEqualTo(expected);
  }

  private static CommandExecutorResponse getDomainInfoResponse() {
    String stdout = """
        Forest           : samdom.example.org
        Domain           : samdom.example.org
        Netbios domain   : SAMDOM
        DC name          : dc1.samdom.example.org
        DC netbios name  : DC1
        Server site      : Default-First-Site-Name
        Client site      : Default-First-Site-Name
        """;
    return new CommandExecutorResponse(stdout, null);
  }

  private static CommandExecutorResponse getPasswordInfoResponse() {
    String stdout = """
        Password information for domain 'DC=samdom,DC=example,DC=org'
        
        Password complexity: on
        Store plaintext passwords: off
        Password history length: 24
        Minimum password length: 7
        Minimum password age (days): 1
        Maximum password age (days): 42
        Account lockout duration (mins): 30
        Account lockout threshold (attempts): 0
        Reset account lockout after (mins): 30
        """;
    return new CommandExecutorResponse(stdout, null);
  }

}