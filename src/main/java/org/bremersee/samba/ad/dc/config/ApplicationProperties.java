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

  private String baseDn = "dc=eixe,dc=bremersee,dc=org";

  private String companyName = "bremersee.org";

  private String companyUrl = "https://bremersee.org";

  private String cryptoSecret = "change-it";

  @NestedConfigurationProperty
  private CliProperties cli = new CliProperties();

  @NestedConfigurationProperty
  private DomainProperties domain = new DomainProperties();

  @NestedConfigurationProperty
  private EmailProperties email = new EmailProperties();

  @NestedConfigurationProperty
  private DomainComputerProperties computer = new DomainComputerProperties();

  @NestedConfigurationProperty
  private DomainUserProperties user = new DomainUserProperties();

  @NestedConfigurationProperty
  private DomainGroupProperties group = new DomainGroupProperties();



  // private String ip4Regex = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";

  // private String macRegex = "^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$";


  private MailWithCredentialsProperties mailWithCredentials = new MailWithCredentialsProperties();

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
