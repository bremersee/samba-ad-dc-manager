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

package org.bremersee.samba.ad.dc.controller.ui;

import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.shared.CurrentPageNameProvider;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.model.OrganizationalUnitPage;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

/**
 * The organization units controller.
 *
 * @author Christian Bremer
 */
@Controller
public class OrganizationUnitsController extends UiController
    implements PageableComponent, CurrentPageNameProvider {

  private final OrganizationalUnitService organizationalUnitService;

  public OrganizationUnitsController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      OrganizationalUnitService organizationalUnitService) {
    super(properties, localeResolver);
    this.organizationalUnitService = organizationalUnitService;
  }

  @Override
  public String getDefaultSort() {
    return OU_SORT;
  }

  @Override
  public String getCurrentPageName() {
    return "organizational-units";
  }

  @GetMapping(path = "/management/organizational-units")
  public String displayOrganizationalUnits(
      @RequestParam(name = PAGE, defaultValue = PAGE_DEFAULT) int page,
      @RequestParam(name = SIZE, defaultValue = SIZE_DEFAULT) int size,
      @RequestParam(name = QUERY, required = false) String query,
      ModelMap model) {

    Pageable pageable = PageRequest.of(page, size);
    OrganizationalUnitPage ouPage = new OrganizationalUnitPage(
        organizationalUnitService.getOrganizationalUnits(pageable, query));
    model.put("ouPage", ouPage);
    return "management/organizational-units";
  }

}
