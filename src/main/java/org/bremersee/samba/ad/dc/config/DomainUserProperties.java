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

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import org.bremersee.samba.ad.dc.common.model.TreeSearchScope;

/**
 * The type DomainUserProperties.
 *
 * @author Christian Bremer
 */
@Data
public class DomainUserProperties implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  public static final String DEFAULT_USER_OU = "CN=Users";

  @NotNull
  private String defaultOu = DEFAULT_USER_OU;

  @NotNull
  private TreeSearchScope defaultSearchScope = TreeSearchScope.ONELEVEL;

  @Min(1)
  private int minQueryLength = 2;

  /**
   * Specifies whether the username should be used for attribute 'cn' or firstname and lastname.
   */
  private boolean useUsernameAsCn = true;

  private String defaultCompany;

  private String defaultDisplayName = "{{user.firstName}} {{user.lastName}}";

  private String defaultEmail = "{{user.samAccountName}}@{{properties.domainName}}";

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

  private String gravatarUrl = "https://www.gravatar.com/avatar/{hash}?d={default}&s={size}";

}
