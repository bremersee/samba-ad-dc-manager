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
import org.bremersee.comparator.spring.web.SortOrderRequestParam;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.shared.CurrentPageNameProvider;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitsComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The group edit members controller.
 *
 * @author Christian Bremer
 */
@Controller
public class GroupEditMembersController extends UiController implements PageableComponent,
    CurrentPageNameProvider, OrganizationalUnitComponent, OrganizationalUnitsComponent {

  private final DomainGroupService domainGroupService;

  @Getter
  private final OrganizationalUnitService organizationalUnitService;

  /**
   * Instantiates a new group edit members controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param domainGroupService the domain group service
   * @param organizationalUnitService the organizational unit service
   */
  public GroupEditMembersController(
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
  public String getDefaultSort() {
    return GROUP_SORT;
  }

  @Override
  public String getCurrentPageName() {
    return "group-edit-members";
  }

  /**
   * Modify member.
   *
   * @param groupName the group name
   * @param member the member
   * @param add the add
   * @return the http response
   */
  @PostMapping(path = "/management/group-edit-members")
  public ResponseEntity<Void> modifyMember(
      @RequestParam(value = "name") String groupName,
      @RequestParam(value = "member") String member,
      @RequestParam(value = "add-member") boolean add) {

    getLogger().info("Modify group member: {} {} of group {}",
        add ? "Adding" : "Removing", member, groupName);
    Set<String> memberSet = Set.of(member);
    if (add) {
      domainGroupService.modifyMembers(groupName, null, null, memberSet, Set.of());
    } else {
      domainGroupService.modifyMembers(groupName, null, null, Set.of(), memberSet);
    }
    return ResponseEntity.ok().build();
  }

  /**
   * Display group edit members view.
   *
   * @param groupName the group name
   * @param ou the ou
   * @param searchScope the search scope
   * @param page the page
   * @param size the size
   * @param sort the sort
   * @param query the query
   * @param withPrimaryMembers the with primary members
   * @param typeComputer the type computer
   * @param typeGroup the type group
   * @param typeUser the type user
   * @param model the model
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @GetMapping(path = "/management/group-edit-members")
  public String displayGroupEditMembers(
      @RequestParam(value = "name", required = false) String groupName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,

      @RequestParam(name = "member-" + PAGE, defaultValue = PAGE_DEFAULT) int page,
      @RequestParam(name = "member-" + SIZE, defaultValue = "10") int size,
      @SortOrderRequestParam(name = "member-" + SORT, defaultSort = "member,desc;displayName")
      SortOrder sort,
      @RequestParam(name = "member-" + QUERY, defaultValue = "") String query,
      @RequestParam(name = "member-primary", defaultValue = "true") boolean withPrimaryMembers,
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
          model.addAttribute("memberSort", getSortMapper().getSortOrderText(sort));
          model.addAttribute("memberQuery", query);
          model.addAttribute("withPrimaryMembers", withPrimaryMembers);
          Set<DomainGroupMemberType> memberTypes = getMemberTypes(
              typeComputer, typeGroup, typeUser);
          model.addAttribute("memberTypeComputer",
              memberTypes.contains(DomainGroupMemberType.COMPUTER));
          model.addAttribute("memberTypeGroup",
              memberTypes.contains(DomainGroupMemberType.GROUP));
          model.addAttribute("memberTypeUser",
              memberTypes.contains(DomainGroupMemberType.USER));
          Pageable pageable = PageRequest.of(page, size, getSortMapper().toSort(sort));
          Page<DomainGroupMember> memberPage = domainGroupService.getMemberSelection(
              pageable,
              query,
              memberTypes,
              withPrimaryMembers,
              group.getSamAccountName(),
              ou,
              searchScope);
          model.addAttribute("memberPage", memberPage);
          return "management/group-edit-members";
        })
        .orElseGet(
            () -> entityNotFoundRedirect(redirectAttributes, "Group", "group.not-found", groupName,
                PAGE_AND_OU_PARAMS, GROUPS));
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

}
