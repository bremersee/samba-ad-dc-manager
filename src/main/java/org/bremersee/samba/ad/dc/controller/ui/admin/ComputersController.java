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

package org.bremersee.samba.ad.dc.controller.ui.admin;

import lombok.Getter;
import org.bremersee.comparator.model.SortOrder;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.common.controller.ui.UiController;
import org.bremersee.samba.ad.dc.controller.ui.components.OrganizationalUnitNavigationComponent;
import org.bremersee.samba.ad.dc.controller.ui.components.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.model.OrganizationalUnitDropdown;
import org.bremersee.samba.ad.dc.samaccount.computer.model.DomainComputerPage;
import org.bremersee.samba.ad.dc.common.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.samaccount.computer.service.DomainComputerService;
import org.bremersee.samba.ad.dc.ou.service.OrganizationalUnitService;
import org.ldaptive.dn.Dn;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

/**
 * The type UsersController.
 *
 * @author Christian Bremer
 */
@Controller
public class ComputersController extends UiController
    implements PageableComponent, OrganizationalUnitNavigationComponent {

  private final SortMapper sortMapper;

  private final DomainComputerService domainComputerService;

  @Getter
  private final OrganizationalUnitService organizationalUnitService;

  public ComputersController(
      DomainControllerProperties domainControllerProperties,
      LocaleResolver localeResolver,
      SortMapper sortMapper,
      DomainComputerService domainComputerService,
      OrganizationalUnitService organizationalUnitService) {
    super(domainControllerProperties, localeResolver);
    this.sortMapper = sortMapper;
    this.domainComputerService = domainComputerService;
    this.organizationalUnitService = organizationalUnitService;
  }

  @Override
  public TreeSearchScope getDefaultSearchScope() {
    return getProperties().getComputer().getDefaultSearchScope();
  }

  @Override
  public String getDefaultSort() {
    return COMPUTER_SORT;
  }

  @Override
  public String getCurrentPageName() {
    return "computers";
  }

  @RequestMapping(path = "/admin/computers", method = RequestMethod.GET)
  public String displayComputers(
      @RequestParam(name = PAGE, defaultValue = PAGE_DEFAULT) int page,
      @RequestParam(name = SIZE, defaultValue = SIZE_DEFAULT) int size,
      @RequestParam(name = SORT, defaultValue = COMPUTER_SORT) SortOrder sort,
      @RequestParam(name = QUERY, required = false) String query,
      @RequestParam(name = OU, required = false) Dn ou,
      @RequestParam(name = SCOPE, required = false) TreeSearchScope scope,
      ModelMap model) {

    OrganizationalUnitDropdown ouDropdown = getOrganizationalUnitDropdown(ou, scope);
    addOrganizationalUnitDropdown(model, ouDropdown);
    Pageable pageable = PageRequest.of(page, size, sortMapper.toSort(sort));
    DomainComputerPage computerPage = new DomainComputerPage(domainComputerService.getComputers(
        pageable, query, ou, ouDropdown.getSelectedScope()));
    model.addAttribute("computers", computerPage);
    return "admin/computers";
  }

}
