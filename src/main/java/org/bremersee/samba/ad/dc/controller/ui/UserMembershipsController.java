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
import org.bremersee.samba.ad.dc.service.DomainGroupService;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.DomainUserService;
import org.ldaptive.dn.Dn;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The user memberships controller.
 *
 * @author Christian Bremer
 */
@Controller
public class UserMembershipsController extends UiController
    implements PageableComponent, OrganizationalUnitComponent {

  private final DomainUserService domainUserService;

  private final DomainGroupService domainGroupService;

  /**
   * Instantiates a new user memberships controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param domainUserService the domain user service
   * @param domainGroupService the domain group service
   */
  public UserMembershipsController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainUserService domainUserService,
      DomainGroupService domainGroupService) {
    super(properties, localeResolver, domainService);
    this.domainUserService = domainUserService;
    this.domainGroupService = domainGroupService;
  }

  @Override
  public String getDefaultSort() {
    return USER_SORT;
  }

  /**
   * Display user edit memberships direct view.
   *
   * @param userName the username
   * @param ou the ou
   * @param searchScope the search scope
   * @param model the model
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @GetMapping(path = "/management/user-memberships-direct")
  public String displayUserEditMembershipsDirect(
      @RequestParam(value = "user", required = false) String userName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    return displayUserEditMemberships(
        true, userName, ou, searchScope, model, redirectAttributes);
  }

  /**
   * Display user edit memberships resolved view.
   *
   * @param userName the username
   * @param ou the ou
   * @param searchScope the search scope
   * @param model the model
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @GetMapping(path = "/management/user-memberships-resolved")
  public String displayUserEditMembershipsResolved(
      @RequestParam(value = "user", required = false) String userName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    return displayUserEditMemberships(
        false, userName, ou, searchScope, model, redirectAttributes);
  }

  private String displayUserEditMemberships(
      boolean direct,
      String userName,
      Dn ou,
      TreeSearchScope searchScope,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    return Optional.ofNullable(userName)
        .flatMap(name -> domainUserService.getUser(userName, ou, searchScope))
        .map(user -> {
          model.addAttribute("user", user);
          Stream<DomainGroup> memberships;
          String page;
          if (direct) {
            memberships = domainGroupService.getMemberships(userName, ou, searchScope);
            page = "management/user-memberships-direct";
          } else {
            memberships = domainGroupService.resolveMemberships(userName, ou, searchScope);
            page = "management/user-memberships-resolved";
          }
          model.addAttribute("memberships", memberships.toList());
          return page;
        })
        .orElseGet(
            () -> entityNotFoundRedirect(redirectAttributes, "User", "user.not-found", userName,
                PAGE_AND_OU_PARAMS, "users"));
  }

}
