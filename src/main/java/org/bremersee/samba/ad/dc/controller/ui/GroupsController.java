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

import lombok.Getter;
import org.bremersee.comparator.model.SortOrder;
import org.bremersee.comparator.spring.web.SortOrderRequestParam;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitDropdown;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitNavigationComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.model.DomainGroupPage;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.service.DomainGroupService;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.ldaptive.dn.Dn;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

/**
 * The group list controller.
 *
 * @author Christian Bremer
 */
@Controller
public class GroupsController extends UiController
    implements PageableComponent, OrganizationalUnitNavigationComponent {

  private final DomainGroupService domainGroupService;

  @Getter
  private final OrganizationalUnitService organizationalUnitService;

  /**
   * Instantiates a new group list controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param domainGroupService the domain group service
   * @param organizationalUnitService the organizational unit service
   */
  public GroupsController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainGroupService domainGroupService,
      OrganizationalUnitService organizationalUnitService) {
    super(properties, localeResolver, domainService);
    this.domainGroupService = domainGroupService;
    this.organizationalUnitService = organizationalUnitService;
  }

  @Override
  public TreeSearchScope getDefaultSearchScope() {
    return getProperties().getGroup().getDefaultSearchScope();
  }

  @Override
  public String getDefaultSort() {
    return GROUP_SORT;
  }

  @Override
  public String getCurrentPageName() {
    return "groups";
  }

  /**
   * Display groups view.
   *
   * @param page the page
   * @param size the size
   * @param sort the sort
   * @param query the query
   * @param ou the ou
   * @param scope the scope
   * @param model the model
   * @return the view
   */
  @GetMapping(path = "/management/groups")
  public String displayGroups(
      @RequestParam(name = PAGE, defaultValue = PAGE_DEFAULT) int page,
      @RequestParam(name = SIZE, defaultValue = SIZE_DEFAULT) int size,
      @SortOrderRequestParam(defaultSort = GROUP_SORT) SortOrder sort,
      @RequestParam(name = QUERY, required = false) String query,
      @RequestParam(name = OU, required = false) Dn ou,
      @RequestParam(name = SCOPE, required = false) TreeSearchScope scope,
      ModelMap model) {

    OrganizationalUnitDropdown ouDropdown = getOrganizationalUnitDropdown(ou, scope);
    addOrganizationalUnitDropdown(model, ouDropdown);
    Pageable pageable = PageRequest.of(page, size, getSortMapper().toSort(sort));
    DomainGroupPage groupPage = new DomainGroupPage(domainGroupService.getGroups(
        pageable, query, ou, ouDropdown.getSelectedScope()));
    model.addAttribute("groups", groupPage);
    return "management/groups";
  }

}
