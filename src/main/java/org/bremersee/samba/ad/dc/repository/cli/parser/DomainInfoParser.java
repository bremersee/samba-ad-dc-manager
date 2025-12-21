/*
 * Copyright 2019-2020 the original author or authors.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponseParser;

/**
 * The domain info parser.
 *
 * @author Christian Bremer
 */
public interface DomainInfoParser
    extends CommandExecutorResponseParser<DomainInfo> {

  /**
   * Return default domain info parser.
   *
   * @return the domain info parser
   */
  static DomainInfoParser defaultParser() {
    return Default.getInstance();
  }

  /**
   * The default domain info parser.
   */
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @Slf4j
  class Default implements DomainInfoParser {

    static final String FOREST = "Forest";

    static final String DOMAIN = "Domain";

    static final String NETBIOS_DOMAIN = "Netbios domain";

    static final String DC_NAME = "DC name";

    static final String DC_NETBIOS_NAME = "DC netbios name";

    static final String SERVER_SITE = "Server site";

    static final String CLIENT_SITE = "Client site";

    private static DomainInfoParser instance;

    static DomainInfoParser getInstance() {
      if (instance == null) {
        instance = new Default();
      }
      return instance;
    }

    @Override
    public DomainInfo parse(final CommandExecutorResponse response) {
      if (response.stdoutHasNoText()) {
        log.warn("Domain info command did not produce output. Error is [{}].",
            response.getStderr());
        return DomainInfo.builder().build();
      }
      final String output = response.getStdout();
      try (final BufferedReader reader = new BufferedReader(new StringReader(output))) {
        return parse(reader);

      } catch (IOException e) {
        log.error("Parsing domain info failed:\n{}\n", output, e);
        return DomainInfo.builder().build();
      }
    }

    private DomainInfo parse(final BufferedReader reader) throws IOException {
      final DomainInfo.Builder info = DomainInfo.builder();
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        findValue(line, FOREST)
            .ifPresent(info::forest);
        findValue(line, DOMAIN)
            .ifPresent(info::domain);
        findValue(line, NETBIOS_DOMAIN)
            .ifPresent(info::netbiosDomain);
        findValue(line, DC_NETBIOS_NAME)
            .ifPresent(info::domainControllerNetbiosName);
        findValue(line, DC_NAME)
            .ifPresent(info::domainControllerName);
        findValue(line, SERVER_SITE)
            .ifPresent(info::serverSite);
        findValue(line, CLIENT_SITE)
            .ifPresent(info::clientSite);
      }
      return info.build();
    }

    private Optional<String> findValue(final String line, final String label) {
      final int index = line.indexOf(":", label.length());
      if (line.trim().startsWith(label) && index > -1) {
        final String value = line.substring(index + 1).trim();
        if (!value.isEmpty()) {
          return Optional.of(value);
        }
      }
      return Optional.empty();
    }

  }

}
