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

package org.bremersee.samba.ad.dc.domain.service;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.springframework.validation.annotation.Validated;

/**
 * The domain service interface.
 *
 * @author Christian Bremer
 */
@Validated
public interface DomainService {

  /**
   * Gets domain info.
   *
   * @return the domain info
   */
  DomainInfo getDomainInfo();

  /**
   * Gets domain info.
   *
   * @param ipOrHostname the ip or hostname
   * @return the domain info
   */
  @NotNull
  DomainInfo getDomainInfo(@NotEmpty String ipOrHostname);

  /**
   * Specifies whether NIS extensions (rfc2307) are installed on the AD Domain Controller. See <a
   * href="https://wiki.samba.org/index.php/Setting_up_RFC2307_in_AD">Setting up RFC2307 in AD</a>
   *
   * @return the boolean
   */
  boolean isRfc2307Enabled();

  /**
   * Gets password information.
   *
   * @return the password information
   */
  PasswordInformation getPasswordInformation();

  /**
   * Create random password.
   *
   * @return the random password
   */
  String createRandomPassword();

}
