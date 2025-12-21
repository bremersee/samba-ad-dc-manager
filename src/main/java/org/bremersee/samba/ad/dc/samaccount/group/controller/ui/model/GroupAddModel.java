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

package org.bremersee.samba.ad.dc.samaccount.group.controller.ui.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bremersee.samba.ad.dc.common.DnTool;
import org.bremersee.samba.ad.dc.samaccount.group.model.DomainGroupType;
import org.bremersee.samba.ad.dc.samaccount.group.model.DomainGroupType.Purpose;
import org.bremersee.samba.ad.dc.samaccount.group.model.DomainGroupType.Scope;
import org.ldaptive.dn.Dn;

/**
 * The type DomainGroupAddRequest.
 *
 * @author Christian Bremer
 */
@Data
@NoArgsConstructor
public class GroupAddModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String newOu;

  private String samAccountName;

  private String groupScope;

  private String groupPurpose;

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

  public GroupAddModel(String newOu) {
    this.newOu = newOu;
    this.groupScope = Scope.GLOBAL.name();
    this.groupPurpose = Purpose.SECURITY.name();
  }

  public Dn getNewOuDn() {
    if (DnTool.isValidDn(newOu)) {
      return new Dn(newOu);
    }
    return null;
  }

  private Scope getSelectedGroupScope() {
    return Optional.ofNullable(groupScope)
        .map(Scope::fromString)
        .orElse(Scope.GLOBAL);
  }

  private Purpose getSelectedGroupPurpose() {
    return Optional.ofNullable(groupPurpose)
        .map(Purpose::fromString)
        .orElse(Purpose.SECURITY);
  }

  public DomainGroupType getSelectedGroupType() {
    return DomainGroupType.from(
        getSelectedGroupScope(),
        getSelectedGroupPurpose());
  }

}
