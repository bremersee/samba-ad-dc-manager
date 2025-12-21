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

import static java.util.Objects.requireNonNullElse;

import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.controller.AbstractController;
import org.bremersee.samba.ad.dc.controller.ui.mapper.GroupEditModelMapper;
import org.bremersee.samba.ad.dc.controller.ui.model.GroupEditModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganisationalUnitsComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.service.DomainGroupService;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.ldaptive.dn.Dn;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The group edit controller.
 *
 * @author Christian Bremer
 */
@Controller
public class GroupEditController extends AbstractEditController implements PageableComponent,
    OrganizationalUnitComponent, OrganisationalUnitsComponent {

  private final DomainService domainService;

  private final DomainGroupService domainGroupService;

  @Getter
  private final OrganizationalUnitService organizationalUnitService;

  public GroupEditController(
      DomainControllerProperties domainControllerProperties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainGroupService domainGroupService,
      OrganizationalUnitService organizationalUnitService) {
    super(domainControllerProperties, localeResolver);
    this.domainService = domainService;
    this.domainGroupService = domainGroupService;
    this.organizationalUnitService = organizationalUnitService;
  }

  @Override
  public String getDefaultSort() {
    return GROUP_SORT;
  }

  @ModelAttribute("rfc2307Enabled")
  public boolean isRfc2307Enabled() {
    return domainService.isRfc2307Enabled();
  }

  @GetMapping(path = "/management/group-edit")
  public String displayGroupEdit(
      @RequestParam(value = "name", required = false) String groupName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    return Optional.ofNullable(groupName)
        .flatMap(name -> domainGroupService.getGroup(name, ou, searchScope))
        .map(group -> {
          model.addAttribute(GROUP, group);
          GroupEditModel editModel = GroupEditModelMapper.INSTANCE.map(group);
          model.addAttribute("editModel", editModel);
          return "group/group-edit";
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes,
            "Group",
            "todo",
            groupName,
            PAGE_AND_OU_PARAMS,
            GROUPS));
  }

  @PostMapping(path = "/management/group-edit")
  public String updateGroup(
      @RequestParam(value = "name", required = false) String oldSamAccountName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      @ModelAttribute(name = "editModel") GroupEditModel editModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    getLogger().debug("updateGroup({}, {})", oldSamAccountName, editModel);

    return Optional.ofNullable(oldSamAccountName)
        .or(() -> Optional.ofNullable(editModel.getSamAccountName()))
        .flatMap(oldName -> domainGroupService.getGroup(oldName, ou, searchScope))
        .map(existingGroup -> updateGroup(
            existingGroup, editModel, model, bindingResult, redirectAttributes))
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes,
            "Group",
            "todo",
            oldSamAccountName,
            PAGE_AND_OU_PARAMS,
            GROUPS));
  }

  private String updateGroup(
      DomainGroup existingGroup,
      GroupEditModel editModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    String oldSamAccountName = existingGroup.getSamAccountName();
    DomainGroup newGroup = GroupEditModelMapper.INSTANCE.merge(editModel, existingGroup);
    try {
      Dn newOu = editModel.getNewOuDn()
          .map(ou -> getDnTool().addBaseDn(ou))
          .filter(ou -> !DnTool.isSameDn(ou, existingGroup.getDn().getParent()))
          .orElse(null);
      DomainGroup updatedGroup = domainGroupService
          .updateGroup(oldSamAccountName, newGroup, newOu);
      model.clear();
      String msg = String.format("Group '%s' was successfully updated.", updatedGroup.getName());
      RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS, msg,
          "i18n.group.edited", updatedGroup.getName());
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

      Map<String, Object> parameters = getParamterMap(updatedGroup.getDn().getParent());
      String redirect = getRedirectUri("group-edit?name={{group.samAccountName}}",
          PAGE_AND_OU_PARAMS,
          putToParameterMap(parameters, GROUP, updatedGroup));
      logRedirectTo("Group successfully updated.", redirect);
      return redirect;

    } catch (ServiceException e) {
      handleException(bindingResult, e);
      String newSamAccountName = newGroup.getSamAccountName();
      DomainGroup partialUpdatedGroup = domainGroupService.getGroup(oldSamAccountName, null, null)
          .or(() -> domainGroupService.getGroup(newSamAccountName, null, null))
          .orElse(existingGroup);
      model.addAttribute(
          AbstractController.OU,
          partialUpdatedGroup.getDn().getParent().format());
      model.addAttribute(GROUP, partialUpdatedGroup);
      return "group/group-edit";
    }
  }

  private void handleException(BindingResult bindingResult, ServiceException serviceException) {

    Object bindTarget = bindingResult.getTarget();
    getLogger().debug("handleException of bind target '{}'", bindTarget, serviceException);
    Assert.isTrue(bindTarget instanceof GroupEditModel, "Illegal bind target.");
    String errorCode = requireNonNullElse(serviceException.getErrorCode(), "");
    switch (errorCode) {
      case EC_SAM_ACCOUNT_NAME_REQUIRED: {
        bindingResult.rejectValue(SAM_ACCOUNT_NAME, "code",
            "Group name is required.");
        break;
      }
      case EC_ILLEGAL_SAM_ACCOUNT_NAME: {
        bindingResult.rejectValue(SAM_ACCOUNT_NAME, "code",
            "Group name contains illegal characters.");
        break;
      }
      case EC_SAM_ACCOUNT_ALREADY_EXISTS: {
        bindingResult.rejectValue(SAM_ACCOUNT_NAME, "code",
            "Group name already exists.");
        break;
      }
      case EC_GID_NUMBER_ALREADY_EXISTS: {
        bindingResult.rejectValue("gidNumber", "code",
            "Unix GID number already exists.");
        break;
      }
      case EC_DN_ALREADY_EXISTS: {
        bindingResult.rejectValue(SAM_ACCOUNT_NAME, "code",
            "Distinguished name already exists.");
        break;
      }
      case EC_EMPTY_OU_RDN: {
        bindingResult.rejectValue("newOu", "code",
            "Organizational unit is empty.");
        break;
      }
      case EC_OU_NOT_FOUND: {
        bindingResult.rejectValue("newOu", "code",
            "Organizational unit was not found.");
        break;
      }
      default: {
        getLogger().error("Editing group failed with a not mapped exception.", serviceException);
        throw serviceException;
      }
    }
  }
}
