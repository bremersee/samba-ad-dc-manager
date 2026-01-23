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

import java.util.Optional;
import java.util.stream.Stream;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.service.DomainComputerService;
import org.bremersee.samba.ad.dc.service.DomainGroupService;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.ldaptive.dn.Dn;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The computer memberships controller.
 *
 * @author Christian Bremer
 */
@Controller
public class ComputerMembershipsController extends UiController
    implements PageableComponent, OrganizationalUnitComponent {

  private final DomainComputerService domainComputerService;

  private final DomainGroupService domainGroupService;

  /**
   * Instantiates a new computer memberships controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param domainComputerService the domain computer service
   * @param domainGroupService the domain group service
   */
  public ComputerMembershipsController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainComputerService domainComputerService,
      DomainGroupService domainGroupService) {
    super(properties, localeResolver, domainService);
    this.domainComputerService = domainComputerService;
    this.domainGroupService = domainGroupService;
  }

  @Override
  public String getDefaultSort() {
    return COMPUTER_SORT;
  }

  /**
   * Display computer edit memberships direct view.
   *
   * @param computerName the computer name
   * @param ou the ou
   * @param searchScope the search scope
   * @param model the model
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @GetMapping(path = "/management/computer-memberships-direct")
  public String displayComputerEditMembershipsDirect(
      @RequestParam(value = "name", required = false) String computerName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    return displayComputerEditMemberships(
        true, computerName, ou, searchScope, model, redirectAttributes);
  }

  /**
   * Display computer edit memberships resolved view.
   *
   * @param computerName the computer name
   * @param ou the ou
   * @param searchScope the search scope
   * @param model the model
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @GetMapping(path = "/management/computer-memberships-resolved")
  public String displayComputerEditMembershipsResolved(
      @RequestParam(value = "name", required = false) String computerName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    return displayComputerEditMemberships(
        false, computerName, ou, searchScope, model, redirectAttributes);
  }

  private String displayComputerEditMemberships(
      boolean direct,
      String computerName,
      Dn ou,
      TreeSearchScope searchScope,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    return Optional.ofNullable(computerName)
        .flatMap(name -> domainComputerService.getComputer(computerName, ou, searchScope))
        .map(computer -> {
          model.addAttribute(COMPUTER, computer);
          Stream<DomainGroup> memberships;
          String page;
          if (direct) {
            memberships = domainGroupService.getMemberships(computerName, ou, searchScope);
            page = "management/computer-memberships-direct";
          } else {
            memberships = domainGroupService.resolveMemberships(computerName, ou, searchScope);
            page = "management/computer-memberships-resolved";
          }
          model.addAttribute("memberships", memberships.toList());
          return page;
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes,
            "Computer",
            "computer.not-found",
            computerName,
            PAGE_AND_OU_PARAMS,
            COMPUTERS));
  }

}
