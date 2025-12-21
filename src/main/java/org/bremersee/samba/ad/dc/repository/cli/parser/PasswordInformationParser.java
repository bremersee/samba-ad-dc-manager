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
import org.bremersee.samba.ad.dc.model.PasswordComplexity;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponseParser;

/**
 * The password information parser.
 *
 * @author Christian Bremer
 */
public interface PasswordInformationParser
    extends CommandExecutorResponseParser<PasswordInformation> {

  /**
   * Return default password information parser.
   *
   * @return the password information parser
   */
  static PasswordInformationParser defaultParser() {
    return Default.getInstance();
  }

  /**
   * The default password information parser.
   */
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @Slf4j
  class Default implements PasswordInformationParser {

    /**
     * The Password complexity.
     */
    static final String PASSWORD_COMPLEXITY = "Password complexity:";

    /**
     * The Store plaintext password.
     */
    static final String STORE_PLAINTEXT_PASSWORD = "Store plaintext passwords:";

    /**
     * The Password history length.
     */
    static final String PASSWORD_HISTORY_LENGTH = "Password history length:";

    /**
     * The Minimum password length.
     */
    static final String MINIMUM_PASSWORD_LENGTH = "Minimum password length:";

    /**
     * The Minimum password age.
     */
    static final String MINIMUM_PASSWORD_AGE = "Minimum password age (days):";

    /**
     * The Maximum password age.
     */
    static final String MAXIMUM_PASSWORD_AGE = "Maximum password age (days):";

    /**
     * The Account lockout duration.
     */
    static final String ACCOUNT_LOCKOUT_DURATION = "Account lockout duration (mins):";

    /**
     * The Account lockout threshold.
     */
    static final String ACCOUNT_LOCKOUT_THRESHOLD = "Account lockout threshold (attempts):";

    /**
     * The Reset account lockout after.
     */
    static final String RESET_ACCOUNT_LOCKOUT_AFTER = "Reset account lockout after (mins):";

    private static PasswordInformationParser instance;

    static PasswordInformationParser getInstance() {
      if (instance == null) {
        instance = new Default();
      }
      return instance;
    }

    @Override
    public PasswordInformation parse(final CommandExecutorResponse response) {
      if (response.stdoutHasNoText()) {
        log.warn("Password information command did not produce output. Error is [{}].",
            response.getStderr());
        return PasswordInformation.builder().build();
      }
      final String output = response.getStdout();
      try (final BufferedReader reader = new BufferedReader(new StringReader(output))) {
        return parse(reader);

      } catch (IOException e) {
        log.error("Parsing password information failed:\n{}\n", output, e);
        return PasswordInformation.builder().build();
      }
    }

    private PasswordInformation parse(final BufferedReader reader) throws IOException {
      final PasswordInformation.Builder info = PasswordInformation.builder();
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        findValue(line, PASSWORD_COMPLEXITY)
            .ifPresent(s -> info.passwordComplexity(PasswordComplexity.fromValue(s)));
        findValue(line, STORE_PLAINTEXT_PASSWORD)
            .flatMap(this::parseBoolean)
            .ifPresent(info::storePlaintextPasswords);
        findValue(line, PASSWORD_HISTORY_LENGTH)
            .flatMap(this::parseInt)
            .ifPresent(info::passwordHistoryLength);
        findValue(line, MINIMUM_PASSWORD_LENGTH)
            .flatMap(this::parseInt)
            .ifPresent(info::minimumPasswordLength);
        findValue(line, MINIMUM_PASSWORD_AGE)
            .flatMap(this::parseInt)
            .ifPresent(info::minimumPasswordAgeInDays);
        findValue(line, MAXIMUM_PASSWORD_AGE)
            .flatMap(this::parseInt)
            .ifPresent(info::maximumPasswordAgeInDays);
        findValue(line, ACCOUNT_LOCKOUT_DURATION)
            .flatMap(this::parseInt)
            .ifPresent(info::accountLockoutDurationInMinutes);
        findValue(line, ACCOUNT_LOCKOUT_THRESHOLD)
            .flatMap(this::parseInt)
            .ifPresent(info::accountLockoutThreshold);
        findValue(line, RESET_ACCOUNT_LOCKOUT_AFTER)
            .flatMap(this::parseInt)
            .ifPresent(info::resetAccountLockoutAfter);
      }
      return info.build();
    }

    private Optional<String> findValue(final String line, final String label) {
      final int index = line.indexOf(label);
      if (index > -1) {
        final String value = line.substring(index + label.length()).trim();
        if (!value.isEmpty()) {
          return Optional.of(value);
        }
      }
      return Optional.empty();
    }

    private Optional<Boolean> parseBoolean(final String value) {
      try {
        return Optional.of(Boolean.parseBoolean(value));
      } catch (Exception ignored) {
        return Optional.empty();
      }
    }

    private Optional<Integer> parseInt(String value) {
      try {
        return Optional.of(Integer.parseInt(value));
      } catch (Exception ignored) {
        return Optional.empty();
      }
    }

  }

}
