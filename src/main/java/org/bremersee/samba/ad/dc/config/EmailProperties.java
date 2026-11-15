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

package org.bremersee.samba.ad.dc.config;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import lombok.Data;
import org.bremersee.spring.core.regex.RegexFlags;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * The email properties.
 *
 * @author Christian Bremer
 */
@Data
public class EmailProperties implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Email regex from <a href="https://emailregex.com/">emailregex.com</a> (RFC 5322 Official
   * Standard).
   *
   * <p>RFC 6530 is not supported.
   */
  public static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+"
      + "(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
      + "|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]"
      + "|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+"
      + "[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}"
      + "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:"
      + "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]"
      + "|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";

  private String emailRegex = EMAIL_REGEX;

  @NestedConfigurationProperty
  private RegexFlags emailRegexFlags = new RegexFlags();

  private String sender = "";

  private String baseUri = "";

  /**
   * Instantiates new email properties.
   */
  public EmailProperties() {
    super();
  }

  /**
   * Gets email regex flags.
   *
   * @return the email regex flags
   */
  public RegexFlags getEmailRegexFlags() {
    if (Objects.equals(getEmailRegex(), EMAIL_REGEX) && !emailRegexFlags.isCaseInsensitive()) {
      emailRegexFlags.setCaseInsensitive(true);
    }
    return emailRegexFlags;
  }

}
