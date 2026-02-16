/*
 * Copyright 2025-2026 the original author or authors.
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
import java.util.Optional;
import lombok.Data;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.ldaptive.dn.Dn;

/**
 * The group edit model.
 *
 * @author Christian Bremer
 */
@Data
public class GroupEditModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The new organizational unit.
   */
  private String newOu;

  /**
   * The sam account name.
   */
  private String samAccountName;

  /**
   * The email address of the group.
   */
  private String email;

  /**
   * A description of the domain group.
   */
  private String description;

  /**
   * Group's Unix/RFC2307 GID number.
   */
  private Integer gidNumber;

  /**
   * Group's Unix/RFC2307 NIS domain.
   */
  private String nisDomain;

  /**
   * Instantiates a new group edit model.
   */
  public GroupEditModel() {
    super();
  }

  /**
   * Gets new distinguished name of the organizational unit.
   *
   * @return the new distinguished name of the organizational unit
   */
  public Optional<Dn> getNewOuDn() {
    return Optional.ofNullable(newOu)
        .filter(DnTool::isValidDn)
        .map(Dn::new);
  }

}
