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

package org.bremersee.samba.ad.dc.repository.cli.parser;

import java.util.Optional;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponseParser;

/**
 * The domain info parser parses Linux command line tool
 * {@code samba-tool domain info <ip_address> [options]}, for example
 * {@code samba-tool domain info dc1}.
 *
 * <p>A response of this command looks like this:
 * <pre>
 *   Forest           : samdom.example.org
 *   Domain           : samdom.example.org
 *   Netbios domain   : SAMDOM
 *   DC name          : dc1.samdom.example.org
 *   DC netbios name  : DC1
 *   Server site      : Default-First-Site-Name
 *   Client site      : Default-First-Site-Name
 * </pre>
 *
 * @author Christian Bremer
 */
public interface DomainInfoParser
    extends CommandExecutorResponseParser<Optional<DomainInfo>> {

  /**
   * Return default domain info parser.
   *
   * @return the domain info parser
   */
  static DomainInfoParser defaultParser() {
    return DefaultDomainInfoParser.getInstance();
  }

}
