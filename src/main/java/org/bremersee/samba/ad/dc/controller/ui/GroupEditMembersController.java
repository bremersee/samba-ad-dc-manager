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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import org.bremersee.comparator.model.SortOrder;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.controller.ui.model.GroupEditMembersModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.CurrentPageNameProvider;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganisationalUnitsComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainGroupMember;
import org.bremersee.samba.ad.dc.model.DomainGroupMemberType;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.service.DomainGroupService;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.ldaptive.dn.Dn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The type UsersController.
 *
 * @author Christian Bremer
 */
@Controller
public class GroupEditMembersController extends UiController implements PageableComponent,
    CurrentPageNameProvider, OrganizationalUnitComponent, OrganisationalUnitsComponent {

  private final DomainService domainService;

  private final DomainGroupService domainGroupService;

  @Getter
  private final OrganizationalUnitService organizationalUnitService;

  private final SortMapper sortMapper;

  public GroupEditMembersController(
      DomainControllerProperties domainControllerProperties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainGroupService domainGroupService,
      OrganizationalUnitService organizationalUnitService,
      SortMapper sortMapper) {
    super(domainControllerProperties, localeResolver);
    this.domainService = domainService;
    this.domainGroupService = domainGroupService;
    this.organizationalUnitService = organizationalUnitService;
    this.sortMapper = sortMapper;
  }

  @Override
  public String getDefaultSort() {
    return GROUP_SORT;
  }

  @Override
  public String getCurrentPageName() {
    return "group-edit-members";
  }

  @ModelAttribute("rfc2307Enabled")
  public boolean isRfc2307Enabled() {
    return domainService.isRfc2307Enabled();
  }

  /*
  @ModelAttribute("possibleMembers")
  public DomainGroupMembers addMemberSelectOptions(
      @RequestParam(value = "name", required = false) String groupName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope) {
    return Optional.ofNullable(groupName)
        .map(name -> domainGroupService.findPossibleMembers(groupName, ou, searchScope))
        .orElseGet(DomainGroupMembers::empty);
  }
  */

  @GetMapping(path = "/management/group-edit-members")
  public String displayGroupEditMembers(
      @RequestParam(value = "name", required = false) String groupName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,

      @RequestParam(name = "member-" + PAGE, defaultValue = PAGE_DEFAULT) int page,
      @RequestParam(name = "member-" + SIZE, defaultValue = "10") int size,
      @RequestParam(name = "member-" + SORT, defaultValue = "displayName") SortOrder sort,
      @RequestParam(name = "member-" + QUERY, defaultValue = "") String query,
      @RequestParam(name = "member-type-computer", defaultValue = "true") boolean typeComputer,
      @RequestParam(name = "member-type-group", defaultValue = "true") boolean typeGroup,
      @RequestParam(name = "member-type-user", defaultValue = "true") boolean typeUser,

      ModelMap model,
      RedirectAttributes redirectAttributes) {

    return Optional.ofNullable(groupName)
        .flatMap(name -> domainGroupService.getGroup(name, ou, searchScope))
        .map(group -> {
          model.addAttribute("group", group);
          model.addAttribute("memberPageNo", page);
          model.addAttribute("memberPageSize", size);
          model.addAttribute("memberSort", sortMapper.getSortOrderText(sort, ""));
          model.addAttribute("memberQuery", query);
          Set<DomainGroupMemberType> memberTypes = getMemberTypes(
              typeComputer, typeGroup, typeUser);
          model.addAttribute("memberTypeComputer",
              memberTypes.contains(DomainGroupMemberType.COMPUTER));
          model.addAttribute("memberTypeGroup",
              memberTypes.contains(DomainGroupMemberType.GROUP));
          model.addAttribute("memberTypeUser",
              memberTypes.contains(DomainGroupMemberType.USER));
          Pageable pageable = PageRequest.of(page, size, sortMapper.toSort(sort));
          Page<DomainGroupMember> memberPage = domainGroupService.getMemberSelection(
              pageable, query, memberTypes, true, group.getSamAccountName(), ou, searchScope);
          getLogger().info("Members found: {}", memberPage.getContent());
          model.addAttribute("memberPage", memberPage);
          return "management/group-edit-members";
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "Group", "todo", groupName, PAGE_AND_OU_PARAMS, GROUPS));
  }

  private Set<DomainGroupMemberType> getMemberTypes(
      boolean typeComputer,
      boolean typeGroup,
      boolean typeUser) {

    Set<DomainGroupMemberType> memberTypes = new HashSet<>(3);
    if (typeComputer) {
      memberTypes.add(DomainGroupMemberType.COMPUTER);
    }
    if (typeGroup) {
      memberTypes.add(DomainGroupMemberType.GROUP);
    }
    if (typeUser) {
      memberTypes.add(DomainGroupMemberType.USER);
    }
    if (memberTypes.isEmpty()) {
      memberTypes.addAll(List.of(
          DomainGroupMemberType.COMPUTER,
          DomainGroupMemberType.GROUP,
          DomainGroupMemberType.USER));
    }
    return memberTypes;
  }

  @PostMapping(path = "/management/group-edit-members")
  public String updateGroupMembers(
      @RequestParam(value = "name", required = false) String groupName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      @ModelAttribute(name = "editRequest") GroupEditMembersModel editRequest,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    getLogger().debug("updateGroupMembers({}, {})", groupName, editRequest);
    return Optional.ofNullable(groupName)
        .flatMap(name -> domainGroupService.getGroup(name, ou, searchScope))
        .map(existingGroup -> {
          DomainGroup updatedGroup = domainGroupService.updateGroup(
              existingGroup.getSamAccountName(),
              DomainGroup.builder()
                  .from(existingGroup)
                  .members(editRequest.getMembers())
                  .build(),
              null);
          model.clear();
          String msg = String.format("Members of group '%s' were successfully updated.",
              updatedGroup.getName());
          RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS, msg,
              "todo", updatedGroup.getName());
          redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

          String redirect = getRedirectUri("group-edit-members?name={{group.samAccountName}}",
              PAGE_AND_OU_PARAMS, putToParameterMap(getParamterMap(), "group", updatedGroup));
          logRedirectTo("Members of group successfully updated.", redirect);
          return redirect;
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "Group", "todo", groupName, PAGE_AND_OU_PARAMS, GROUPS));
  }

}
