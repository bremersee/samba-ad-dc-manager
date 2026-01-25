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
import lombok.Data;
import org.bremersee.samba.ad.dc.model.DomainUser;

/**
 * The user reset password model.
 */
@Data
public class UserResetPasswordModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The sam account name.
   */
  private String samAccountName;

  /**
   * The password.
   */
  private String password;

  /**
   * The generate password flag.
   */
  private boolean generateRandomPassword;

  /**
   * Instantiates a new user reset password model.
   *
   * @param user the user
   */
  public UserResetPasswordModel(DomainUser user) {
    this.samAccountName = user.getSamAccountName();
  }

}
