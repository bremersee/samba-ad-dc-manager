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

package org.bremersee.samba.ad.dc.samaccount.group.controller.ui;

import static java.util.Objects.requireNonNullElse;

import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.controller.ui.UiController;
import org.bremersee.samba.ad.dc.samaccount.group.controller.GroupControllerConstants;
import org.bremersee.samba.ad.dc.samaccount.group.controller.ui.mapper.GroupAddModelMapper;
import org.bremersee.samba.ad.dc.samaccount.group.controller.ui.model.GroupAddModel;
import org.bremersee.samba.ad.dc.samaccount.group.controller.ui.shared.DomainGroupTypesComponent;
import org.bremersee.samba.ad.dc.ou.controller.ui.shared.OrganisationalUnitsComponent;
import org.bremersee.samba.ad.dc.ou.controller.ui.shared.OrganizationalUnitComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.domain.service.DomainService;
import org.bremersee.samba.ad.dc.ou.service.OrganizationalUnitService;
import org.bremersee.samba.ad.dc.samaccount.group.model.DomainGroup;
import org.bremersee.samba.ad.dc.samaccount.group.service.DomainGroupService;
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
 * The group add controller.
 *
 * @author Christian Bremer
 */
@Controller
public class GroupAddController extends UiController implements PageableComponent,
    RedirectComponent, DomainGroupTypesComponent, OrganizationalUnitComponent,
    OrganisationalUnitsComponent {

  private final DomainService domainService;

  private final DomainGroupService domainGroupService;

  @Getter
  private final OrganizationalUnitService organizationalUnitService;

  public GroupAddController(
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
    return GroupControllerConstants.GROUP_SORT;
  }

  @ModelAttribute("rfc2307Enabled")
  public boolean isRfc2307Enabled() {
    return domainService.isRfc2307Enabled();
  }

  @GetMapping(path = "/management/group-add")
  public String displayGroupAdd(
      @RequestParam(name = OU, required = false) Dn ou,
      ModelMap model) {

    getLogger().debug("displayGroupAdd({})", ou);
    Dn ouDn = Optional.ofNullable(ou)
        .filter(DnTool::isValidDn)
        .filter(dn -> !dn.isSame(getDnTool().getBaseDn()))
        .orElseGet(() -> getDnTool().addBaseDn(getProperties().getGroup().getDefaultOu()));
    GroupAddModel addModel = new GroupAddModel(ouDn.format());
    model.addAttribute("addModel", addModel);
    return "group/group-add";
  }

  @PostMapping(path = "/management/group-add")
  public String addGroup(
      @ModelAttribute(name = "addModel") GroupAddModel addModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    getLogger().debug("addGroup({})", addModel);

    DomainGroup addedGroup = addGroup(bindingResult, addModel);

    if (bindingResult.hasErrors()) {
      getLogger().debug("Adding group failed. Some fields were invalid.");
      return "group/group-add";
    }

    model.clear();
    RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS,
        String.format("Group '%s' was successfully added.", addedGroup.getSamAccountName()),
        "i18n.group.added", addedGroup.getSamAccountName());
    redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

    Map<String, Object> parameters = getParamterMap(addedGroup.getDn().getParent());
    String redirect = getRedirectUri("group-edit?name={{group.samAccountName}}",
        PAGE_AND_OU_PARAMS, putToParameterMap(parameters, "group", addedGroup));
    logRedirectTo("Group successfully added.", redirect);
    return redirect;
  }

  private DomainGroup addGroup(
      BindingResult bindingResult,
      GroupAddModel addModel) {

    DomainGroup group = GroupAddModelMapper.INSTANCE.map(addModel);
    Dn ou = Optional.ofNullable(addModel.getNewOuDn())
        .orElseGet(() -> new Dn(getProperties().getGroup().getDefaultOu()));

    try {
      return domainGroupService.addGroup(group, ou);

    } catch (ServiceException e) {
      handleException(bindingResult, e);
    }
    return group;
  }

  private void handleException(BindingResult bindingResult, ServiceException serviceException) {

    Object bindTarget = bindingResult.getTarget();
    getLogger().debug("handleException of bind target '{}'", bindTarget, serviceException);
    Assert.isTrue(bindTarget instanceof GroupAddModel, "Illegal bind target.");
    String errorCode = requireNonNullElse(serviceException.getErrorCode(), "");
    switch (errorCode) {
      case EC_SAM_ACCOUNT_NAME_REQUIRED: {
        bindingResult.rejectValue(GroupControllerConstants.SAM_ACCOUNT_NAME, "code",
            "Group name is required.");
        break;
      }
      case EC_ILLEGAL_SAM_ACCOUNT_NAME: {
        bindingResult.rejectValue(GroupControllerConstants.SAM_ACCOUNT_NAME, "code",
            "Group name contains illegal characters.");
        break;
      }
      case EC_SAM_ACCOUNT_ALREADY_EXISTS: {
        bindingResult.rejectValue(GroupControllerConstants.SAM_ACCOUNT_NAME, "code",
            "Group name already exists.");
        break;
      }
      case EC_GID_NUMBER_ALREADY_EXISTS: {
        bindingResult.rejectValue("gidNumber", "code",
            "Unix GID number already exists.");
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
        getLogger().error("Adding group failed with a not mapped exception.", serviceException);
        throw serviceException;
      }
    }
  }

}
