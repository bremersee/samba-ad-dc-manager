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

import java.util.Map;
import java.util.Optional;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.model.SamAccountDeleteModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.service.DomainGroupService;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.ldaptive.dn.Dn;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The group delete controller.
 *
 * @author Christian Bremer
 */
@Controller
public class GroupDeleteController extends UiController implements PageableComponent,
    OrganizationalUnitComponent {

  private final DomainGroupService domainGroupService;

  /**
   * Instantiates a new group delete controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param domainGroupService the domain group service
   */
  public GroupDeleteController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainGroupService domainGroupService) {
    super(properties, localeResolver, domainService);
    this.domainGroupService = domainGroupService;
  }

  @Override
  public String getDefaultSort() {
    return GROUP_SORT;
  }

  /**
   * Display group delete view.
   *
   * @param groupName the group name
   * @param ou the ou
   * @param searchScope the search scope
   * @param model the model
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @GetMapping(path = "/management/group-delete")
  public String displayGroupDelete(
      @RequestParam(value = "name", required = false) String groupName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    return Optional.ofNullable(groupName)
        .flatMap(name -> domainGroupService.getGroup(groupName, ou, searchScope))
        .map(group -> {
          model.addAttribute("group", group);
          model.addAttribute("deleteRequest", new SamAccountDeleteModel(group));
          return "management/group-delete";
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "Group", "group.not-found", groupName, PAGE_AND_OU_PARAMS, GROUPS));
  }

  /**
   * Delete group.
   *
   * @param ou the ou
   * @param searchScope the search scope
   * @param deleteRequest the delete request
   * @param model the model
   * @param bindingResult the binding result
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @PostMapping(path = "/management/group-delete")
  public String deleteGroup(
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      @ModelAttribute("deleteRequest") SamAccountDeleteModel deleteRequest,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    getLogger().debug("deleteGroup({})", deleteRequest);
    return Optional.ofNullable(deleteRequest.getSamAccountName())
        .flatMap(name -> domainGroupService.getGroup(name, ou, searchScope))
        .map(group -> {
          if (!group.getSamAccountName().equalsIgnoreCase(deleteRequest.getVerificationName())) {
            bindingResult.rejectValue("verificationName", "group-delete.name-does-not-match",
                "The name doesn't match.");
            model.addAttribute("group", group);
            return "management/group-delete";
          }
          return deleteGroup(group, model, redirectAttributes);
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "Group", "group.not-found", deleteRequest.getSamAccountName(),
            PAGE_AND_OU_PARAMS, GROUPS));
  }

  private String deleteGroup(
      DomainGroup group,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    boolean result = domainGroupService.deleteGroup(group.getSamAccountName());
    model.clear();
    RedirectMessage rmsg;
    if (result) {
      rmsg = getRedirectMessage(RedirectMessageType.SUCCESS,
          String.format("Group '%s' was successfully deleted.", group.getName()),
          "group-delete.success", group.getName());
    } else {
      rmsg = getRedirectMessage(RedirectMessageType.WARNING,
          String.format("Somehow the group '%s' was not deleted.", group.getName()),
          "group-delete.failure", group.getName());
    }
    redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

    Map<String, Object> parameters = getParameterMap();
    String redirect = getRedirectUri("groups", PAGE_AND_OU_PARAMS, parameters);
    logRedirectTo("Group deletion message.", redirect);
    return redirect;
  }

}
