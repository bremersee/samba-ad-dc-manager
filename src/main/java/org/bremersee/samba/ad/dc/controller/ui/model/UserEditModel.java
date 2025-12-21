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
import org.springframework.web.multipart.MultipartFile;

/**
 * The user edit model.
 *
 * @author Christian Bremer
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UserEditModel extends UserModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private transient MultipartFile avatar;

  private boolean removeAvatar;

  private boolean renameNamesAutomatically = true;

  private boolean enabled = true;

  private boolean passwordExpirationEnabled = false;

  private boolean noExpiry = true;

  private OffsetDateTime accountExpires;

  private String userPrincipalName;

  private Integer primaryGroupId;

  /**
   * Instantiates a new user edit model.
   */
  public UserEditModel() {
    super();
  }
}
