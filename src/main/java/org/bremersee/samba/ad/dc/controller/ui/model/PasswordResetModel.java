/*
 * Copyright 2025-2026  the original author or authors.
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
import lombok.Data;
import org.bremersee.samba.ad.dc.model.DomainUser;

/**
 * The password reset model.
 */
@Data
public class PasswordResetModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The username.
   */
  private String username;

  /**
   * The new password.
   */
  private String newPassword;

  /**
   * The repetition of the new password.
   */
  private String newPasswordRepetition;

  /**
   * Instantiates a new password reset model.
   */
  public PasswordResetModel() {
    super();
  }

  /**
   * Instantiates a new password reset model.
   *
   * @param domainUser the domain user
   */
  public PasswordResetModel(DomainUser domainUser) {
    setUsername(domainUser.getSamAccountName());
  }

  @Override
  public String toString() {
    return "PasswordResetModel{"
        + "username='" + getUsername() + '\''
        + '}';
  }

}
