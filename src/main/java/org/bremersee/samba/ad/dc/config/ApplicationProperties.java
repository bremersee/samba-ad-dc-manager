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

package org.bremersee.samba.ad.dc.config;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.spring.core.regex.RegexFlags;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * The domain controller properties.
 *
 * @author Christian Bremer
 */
@ConfigurationProperties(prefix = "bremersee.domain-controller")
@Component
@Getter
@Setter
@ToString(exclude = "cryptoSecret")
@EqualsAndHashCode
@Slf4j
public class ApplicationProperties implements Serializable {

  @Serial
  private static final long serialVersionUID = 3L;

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

  public static final int MIN_QUERY_LENGTH = 3; // TODO add to repos and javascript

  private String baseDn = "dc=eixe,dc=bremersee,dc=org";

  private String cryptoSecret = "change-it";

  private String emailRegex = EMAIL_REGEX;

  @NestedConfigurationProperty
  private RegexFlags emailRegexFlags = new RegexFlags();

  @NestedConfigurationProperty
  private CliProperties cli = new CliProperties();

  @NestedConfigurationProperty
  private DomainUserProperties user = new DomainUserProperties();

  @NestedConfigurationProperty
  private DomainGroupProperties group = new DomainGroupProperties();

  @NestedConfigurationProperty
  private DomainComputerProperties computer = new DomainComputerProperties();

  @NestedConfigurationProperty
  private DomainProperties domain = new DomainProperties(); // move back?


  private String personalName = "Anna Livia"; // mail with credentials

  private String companyName = "example.org"; // mail with credentials

  private String companyUrl = "http://example.org"; // mail with credentials


  // private String ip4Regex = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";

  // private String macRegex = "^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$";


  private MailWithCredentialsProperties mailWithCredentials = new MailWithCredentialsProperties();

  public RegexFlags getEmailRegexFlags() {
    if (Objects.equals(getEmailRegex(), EMAIL_REGEX) && !emailRegexFlags.isCaseInsensitive()) {
      emailRegexFlags.setCaseInsensitive(true);
    }
    return emailRegexFlags;
  }

  /**
   * The mail with credentials properties.
   *
   * @author Christian Bremer
   */
  @Data
  public static class MailWithCredentialsProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String sender = "no-reply@example.org";

    private String templateBasename = "personal-mail-with-credentials";

    private String loginUrl = "http://localhost:4200/change-password";

    private List<MailInlineAttachment> inlineAttachments = new ArrayList<>();
  }

  /**
   * The mail inline attachment.
   */
  @Data
  public static class MailInlineAttachment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String contentId;

    private String location;

    private String mimeType;
  }
}
