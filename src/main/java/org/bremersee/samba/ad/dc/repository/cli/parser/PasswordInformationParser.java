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

import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponseParser;

/**
 * The password information parser parses Linux command line tool
 * {@code samba-tool domain passwordsettings show}.
 *
 * <p>A response of this command looks like this:
 * <pre>
 *   Password information for domain 'DC=samdom,DC=example,DC=org'
 *
 *   Password complexity: on
 *   Store plaintext passwords: off
 *   Password history length: 24
 *   Minimum password length: 7
 *   Minimum password age (days): 1
 *   Maximum password age (days): 42
 *   Account lockout duration (mins): 30
 *   Account lockout threshold (attempts): 0
 *   Reset account lockout after (mins): 30
 * </pre>
 *
 * @author Christian Bremer
 */
public interface PasswordInformationParser
    extends CommandExecutorResponseParser<PasswordInformation> {

  /**
   * Return the default password information parser.
   *
   * @return the password information parser
   */
  static PasswordInformationParser defaultParser() {
    return DefaultPasswordInformationParser.getInstance();
  }

}
