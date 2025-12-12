/*
 * Copyright 2024 the original author or authors.
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

package org.bremersee.samba.ad.dc.oldmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bremersee.ldaptive.transcoder.UserAccountControl;

/**
 * The domain user's account control.
 *
 * @author Christian Bremer
 */
@Schema(description = "Domain user's account control.")
@ToString
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class DomainUserAccountControl {

  @Schema(
      description = "Specifies whether the account of the domain user is a normal one or not.",
      defaultValue = "true")
  @JsonProperty(value = "normalAccount", defaultValue = "true")
  private Boolean normalAccount = true;

  @Schema(
      description = "Specifies whether the domain user is enabled or not.",
      defaultValue = "true")
  @JsonProperty(value = "enabled", defaultValue = "true")
  private Boolean enabled = true;

  @Schema(
      description = "Specifies whether the password expiration is enabled or not.",
      defaultValue = "false")
  @JsonProperty(value = "passwordExpirationEnabled", defaultValue = "false")
  private Boolean passwordExpirationEnabled = false;

  @Builder(toBuilder = true)
  public DomainUserAccountControl(
      Boolean normalAccount,
      Boolean enabled,
      Boolean passwordExpirationEnabled) {
    this.normalAccount = normalAccount;
    this.enabled = enabled;
    this.passwordExpirationEnabled = passwordExpirationEnabled;
  }

  public Boolean getEnabled() {
    return Optional.ofNullable(enabled).orElse(true);
  }

  public Boolean getPasswordExpirationEnabled() {
    return Boolean.TRUE.equals(passwordExpirationEnabled);
  }

  public UserAccountControl toUserAccountControl() {
    UserAccountControl userAccountControl = new UserAccountControl();
    userAccountControl.setNormalAccount(normalAccount);
    userAccountControl.setEnabled(enabled);
    userAccountControl.setPasswordExpirationEnabled(passwordExpirationEnabled);
    return userAccountControl;
  }

  public static DomainUserAccountControl from(UserAccountControl userAccountControl) {
    if (Objects.isNull(userAccountControl)) {
      return new DomainUserAccountControl();
    }
    return new DomainUserAccountControl(
        userAccountControl.isNormalAccount(),
        userAccountControl.isEnabled(),
        userAccountControl.isPasswordExpirationEnabled());
  }
}
