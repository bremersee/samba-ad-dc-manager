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

package org.bremersee.samba.ad.dc.controller.ui.shared;

import static java.util.Objects.isNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;

/**
 * The organizational unit selector.
 *
 * @author Christian Bremer
 */
@Data
public class OrganizationalUnitDropdown implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The selected organizational unit.
   */
  private OrganizationalUnit selectedOu;

  /**
   * The selectable organizational units.
   */
  private List<OrganizationalUnit> selectableOus;

  /**
   * Specifies whether the search scope is selectable or not.
   */
  private boolean scopeSelectable = true;

  /**
   * The selected search scope.
   */
  private TreeSearchScope selectedScope;

  /**
   * The display value of the selected search scope.
   */
  private String selectedScopeDisplayValue;

  /**
   * The selected search scope.
   */
  private TreeSearchScope selectableScope;

  /**
   * The display value of the selectable search scope.
   */
  private String selectableScopeDisplayValue;

  /**
   * Gets selectable organizational units.
   *
   * @return the selectable organizational units
   */
  public List<OrganizationalUnit> getSelectableOus() {
    if (isNull(selectableOus)) {
      selectableOus = new ArrayList<>();
    }
    return selectableOus;
  }
}
