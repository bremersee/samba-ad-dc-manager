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

package org.bremersee.samba.ad.dc.controller.ui.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bremersee.samba.ad.dc.config.DomainUserProperties;
import org.bremersee.samba.ad.dc.model.PasswordInformation;

/**
 * The user add model.
 *
 * @author Christian Bremer
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"password"}, callSuper = true)
public class UserAddModel extends UserModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The use username as common name flag.
   */
  private boolean useUsernameAsCn = true;

  /**
   * User's password.
   */
  private String password;

  /**
   * The send email flag.
   */
  private boolean sendEmail;

  /**
   * Instantiates a new user add model.
   */
  public UserAddModel() {
    super();
  }

  /**
   * Instantiates a new user add model.
   *
   * @param properties the properties
   * @param passwordInformation the password information
   * @param isRfc2307Enabled is rfc 2307 enabled
   */
  public UserAddModel(
      DomainUserProperties properties,
      PasswordInformation passwordInformation,
      boolean isRfc2307Enabled) {
    int passwordAgeInDays = passwordInformation.getMaximumPasswordAgeInDays();
    setAccountExpires(OffsetDateTime.now().plusDays(passwordAgeInDays));
    setUseUsernameAsCn(properties.isUseUsernameAsCn());
    setCompany(properties.getDefaultCompany());
    setDisplayName(properties.getDefaultDisplayName());
    setEmail(properties.getDefaultEmail());
    setUserPrincipalName(properties.getDefaultUserPrincipalName());
    setHomeDirectory(properties.getDefaultHomeDirectory());
    setHomeDrive(properties.getDefaultHomeDrive());
    setPreferredLanguage(properties.getDefaultLanguage());
    setProfilePath(properties.getDefaultProfilePath());
    setScriptPath(properties.getDefaultScriptPath());
    if (isRfc2307Enabled) {
      setGecos(properties.getDefaultGecos());
      setGidNumber(properties.getDefaultGidNumber());
      setLoginShell(properties.getDefaultLoginShell());
      setNisDomain(properties.getDefaultNisDomain());
      setUid(properties.getDefaultUid());
      setUnixHomeDirectory(properties.getDefaultUnixHomeDirectory());
    }
  }

}
