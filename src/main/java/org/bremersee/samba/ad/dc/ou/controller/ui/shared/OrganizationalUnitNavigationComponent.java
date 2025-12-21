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

package org.bremersee.samba.ad.dc.ou.controller.ui.shared;

import java.util.List;
import java.util.Optional;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.controller.DnToolProvider;
import org.bremersee.samba.ad.dc.controller.ui.shared.CurrentPageNameProvider;
import org.bremersee.samba.ad.dc.controller.ui.shared.MessageProvider;
import org.bremersee.samba.ad.dc.ou.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.ou.service.OrganizationalUnitService;
import org.ldaptive.dn.Dn;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;

/**
 * The interface OrganizationalUnitSelector.
 *
 * @author Christian Bremer
 */
@Validated
public interface OrganizationalUnitNavigationComponent extends DnToolProvider,
    CurrentPageNameProvider, PageableComponent, MessageProvider {

  OrganizationalUnitService getOrganizationalUnitService();

  TreeSearchScope getDefaultSearchScope();

  default void addOrganizationalUnitDropdown(ModelMap model, OrganizationalUnitDropdown selector) {
    model.addAttribute("ouDropdown", selector);
  }

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

  default boolean isBaseOu(OrganizationalUnit ou) {
    return DnTool.isSameDn(getDnTool().getBaseDn(), new Dn(ou.getDistinguishedName()));
  }

  default String getDisplayValue(TreeSearchScope scope) {
    return getMessage(scope.getDefaultDisplayName(), scope.getI18nCode());
  }

}
