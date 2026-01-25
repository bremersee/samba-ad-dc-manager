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

import java.util.List;
import java.util.Optional;
import org.bremersee.samba.ad.dc.controller.DnToolProvider;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.ldaptive.dn.Dn;
import org.springframework.ui.ModelMap;

/**
 * The organizational unit selector.
 *
 * @author Christian Bremer
 */
public interface OrganizationalUnitNavigationComponent extends DnToolProvider,
    CurrentPageNameProvider, PageableComponent, MessageProvider {

  /**
   * Gets organizational unit service.
   *
   * @return the organizational unit service
   */
  OrganizationalUnitService getOrganizationalUnitService();

  /**
   * Gets default search scope.
   *
   * @return the default search scope
   */
  TreeSearchScope getDefaultSearchScope();

  /**
   * Add organizational unit dropdown to model.
   *
   * @param model the model
   * @param selector the selector
   */
  default void addOrganizationalUnitDropdown(ModelMap model, OrganizationalUnitDropdown selector) {
    model.addAttribute("ouDropdown", selector);
  }

  /**
   * Gets organizational unit dropdown.
   *
   * @param ou the ou
   * @param scope the scope
   * @return the organizational unit dropdown
   */
  default OrganizationalUnitDropdown getOrganizationalUnitDropdown(
      Dn ou,
      TreeSearchScope scope) {

    Dn selectedOuDn = getDnTool().addBaseDn(Optional.ofNullable(ou)
        .filter(dn -> getOrganizationalUnitService().organisationUnitExists(dn))
        .orElse(null));
    List<OrganizationalUnit> orgUnits = getOrganizationalUnitService()
        .getOrganizationalUnitsWithSystemOusAndBase()
        .toList();
    OrganizationalUnitDropdown ouDropdown = new OrganizationalUnitDropdown();
    for (OrganizationalUnit orgUnit : orgUnits) {
      if (selectedOuDn.isSame(new Dn(orgUnit.getDistinguishedName()))) {
        ouDropdown.setSelectedOu(orgUnit);
      } else {
        ouDropdown.getSelectableOus().add(orgUnit);
      }
    }
    if (isBaseOu(ouDropdown.getSelectedOu())) {
      ouDropdown.setSelectedScope(TreeSearchScope.SUBTREE);
      ouDropdown.setSelectedScopeDisplayValue(getDisplayValue(TreeSearchScope.SUBTREE));
      ouDropdown.setSelectableScope(TreeSearchScope.ONELEVEL);
      ouDropdown.setSelectableScopeDisplayValue(getDisplayValue(TreeSearchScope.ONELEVEL));
      ouDropdown.setScopeSelectable(false);
    } else {
      TreeSearchScope selectedScope = Optional.ofNullable(scope)
          .or(() -> Optional.ofNullable(getDefaultSearchScope()))
          .filter(s -> s == TreeSearchScope.SUBTREE || s == TreeSearchScope.ONELEVEL)
          .orElse(TreeSearchScope.ONELEVEL);
      TreeSearchScope selectableScope = selectedScope == TreeSearchScope.SUBTREE
          ? TreeSearchScope.ONELEVEL
          : TreeSearchScope.SUBTREE;
      ouDropdown.setSelectedScope(selectedScope);
      ouDropdown.setSelectedScopeDisplayValue(getDisplayValue(selectedScope));
      ouDropdown.setSelectableScope(selectableScope);
      ouDropdown.setSelectableScopeDisplayValue(getDisplayValue(selectableScope));
      ouDropdown.setScopeSelectable(true);
    }
    return ouDropdown;
  }

  /**
   * Determines whether the given organization unit is the base.
   *
   * @param ou the organization unit
   * @return {@code true} if the organizational unit is the base, otherwise {@code false}
   */
  default boolean isBaseOu(OrganizationalUnit ou) {
    return DnTool.isSameDn(getDnTool().getBaseDn(), new Dn(ou.getDistinguishedName()));
  }

  /**
   * Gets the display value of the given search scope.
   *
   * @param scope the search scope
   * @return the display value
   */
  default String getDisplayValue(TreeSearchScope scope) {
    return getMessage(scope.getDefaultDisplayName(), scope.getI18nCode());
  }

}
