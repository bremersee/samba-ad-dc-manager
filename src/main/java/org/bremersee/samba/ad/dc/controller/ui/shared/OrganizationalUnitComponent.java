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

package org.bremersee.samba.ad.dc.controller.ui.shared;

import java.util.Optional;
import org.bremersee.samba.ad.dc.controller.AbstractController;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.ldaptive.dn.Dn;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The organizational unit component.
 */
public interface OrganizationalUnitComponent extends LoggerProvider {

  /**
   * Add organizational unit to model.
   *
   * @param ou the organizational unit
   * @return the organizational unit
   */
  @ModelAttribute(AbstractController.OU)
  default String addOrganizationalUnit(
      @RequestParam(name = AbstractController.OU, required = false) Dn ou) {
    String currentOu = Optional.ofNullable(ou).map(Dn::format).orElse("");
    getLogger().debug("Adding 'ou={}' to model.", currentOu);
    return currentOu;
  }

  /**
   * Add search scope to model.
   *
   * @param scope the search scope
   * @return the search scope
   */
  @ModelAttribute(AbstractController.SCOPE)
  default String addSearchScope(
      @RequestParam(name = AbstractController.SCOPE, required = false) TreeSearchScope scope) {
    String searchScope = Optional.ofNullable(scope)
        .map(TreeSearchScope::getParameterValue)
        .orElse("");
    getLogger().debug("Adding 'scope={}' to model.", searchScope);
    return searchScope;
  }

}
