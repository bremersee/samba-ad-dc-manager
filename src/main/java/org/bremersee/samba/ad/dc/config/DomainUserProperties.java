/*
 * Copyright 2025 the original author or authors.
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

import static org.springframework.util.ObjectUtils.isEmpty;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The domain user properties.
 *
 * @author Christian Bremer
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DomainUserProperties extends SamAccountProperties implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  public static final String DEFAULT_USER_OU = "CN=Users";

  /**
   * Specifies whether the username should be used for attribute 'cn' or firstname and lastname.
   */
  private boolean useUsernameAsCn = true;

  private Duration invitationLifetime = Duration.ofDays(30L);

  private Duration passwordResetRequestLifetime = Duration.ofDays(7L);

  private Duration changeEmailRequestLifetime = Duration.ofDays(7L);

  private String defaultCompany;

  private String defaultDisplayName = "{{user.firstName}} {{user.lastName}}";

  private String defaultEmail = "{{user.samAccountName}}@{{domain.domainInfo.domain}}";

  private String defaultUserPrincipalName = "{{user.samAccountName}}@{{domain.domainInfo.domain}}";

  private String defaultGecos = "{{user.firstName}} {{user.lastName}}";

  private Integer defaultGidNumber; // = 100; // = Domain Users

  private String defaultHomeDirectory;

  private String defaultHomeDrive;

  private String defaultLoginShell = "/bin/bash";

  private String defaultNisDomain;

  private String defaultLanguage = "de-DE";

  private String defaultProfilePath;

  private String defaultScriptPath;

  private String defaultUid = "{{user.samAccountName}}";

  private String defaultUnixHomeDirectory = "/home/{{user.samAccountName}}";

  private DefaultLoginPage defaultLoginPage = new DefaultLoginPage();

  private String gravatarUrl = "https://www.gravatar.com/avatar/{hash}?d={default}&s={size}";

  public DomainUserProperties() {
    setDefaultOu(DEFAULT_USER_OU);
  }

  @Data
  public static class DefaultLoginPage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String url = "https://data.eixe.bremersee.org";

    private String name = "DATA";

    private boolean usingNetbiosDomainPrefix = true;

    public String getName() {
      if (isEmpty(name)) {
        return url;
      }
      return name;
    }
  }

}
