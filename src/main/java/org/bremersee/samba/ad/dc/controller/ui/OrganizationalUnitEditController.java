/*
 * Copyright 2025 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.mapper.OrganizationalUnitEditModelMapper;
import org.bremersee.samba.ad.dc.controller.ui.model.OrganizationalUnitEditModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.NameValue;
import org.ldaptive.dn.RDn;
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
 * The organizational unit edit controller.
 *
 * @author Christian Bremer
 */
@Controller
public class OrganizationalUnitEditController extends UiController
    implements PageableComponent, RedirectComponent {

  private final OrganizationalUnitService organizationalUnitService;

  public OrganizationalUnitEditController(
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

  @ModelAttribute("ous")
  public List<OrganizationalUnit> addOrganisationalUnits() {
    Stream<OrganizationalUnit> baseStream = Stream.of(organizationalUnitService.getBase());
    Stream<OrganizationalUnit> otherParentsStream = organizationalUnitService
        .getOrganizationalUnits()
        .filter(ou -> !ou.isSystemOu());
    return Stream.concat(baseStream, otherParentsStream).toList();
  }

  @GetMapping(path = "/management/organizational-unit-edit")
  public String displayOrganizationalUnitEdit(
      @RequestParam(value = "name", required = false) Dn ouDn,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    getLogger().debug("displayOrganizationalUnitEdit({})", ouDn);
    String name = Optional.ofNullable(ouDn)
        .map(Dn::getRDn)
        .map(RDn::getNameValue)
        .map(NameValue::getStringValue)
        .orElse("null");

    return Optional.ofNullable(ouDn)
        .filter(dn -> !dn.isEmpty())
        .flatMap(organizationalUnitService::getOrganizationalUnit)
        .map(ou -> {
          model.addAttribute("organizationalUnit", ou);
          OrganizationalUnitEditModel editModel = OrganizationalUnitEditModelMapper.INSTANCE
              .map(ou);
          model.put("editModel", editModel);
          return "management/organizational-unit-edit";
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "Organizational Unit", "todo", name,
            PAGE_AND_OU_PARAMS, "organizational-units"));
  }

  @PostMapping(path = "/management/organizational-unit-edit")
  public String updateOrganizationalUnit(
      @ModelAttribute(name = "editModel") OrganizationalUnitEditModel editModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    getLogger().debug("editOrganizationalUnit({})", editModel);

    String name = Optional.ofNullable(editModel.getOu())
        .map(Dn::new)
        .map(Dn::getRDn)
        .map(RDn::getNameValue)
        .map(NameValue::getStringValue)
        .orElse("null");

    return Optional.ofNullable(editModel.getOu())
        .map(Dn::new)
        .flatMap(organizationalUnitService::getOrganizationalUnit)
        .map(ou -> updateOrganizationalUnit(
            ou, editModel, model, bindingResult, redirectAttributes))
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "Organizational Unit", "todo", name,
            PAGE_AND_OU_PARAMS, "organizational-units"));
  }

  private String updateOrganizationalUnit(
      OrganizationalUnit ou,
      OrganizationalUnitEditModel editModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    OrganizationalUnit newOu = OrganizationalUnitEditModelMapper.INSTANCE.merge(editModel, ou);
    OrganizationalUnit updatedOu;
    try {
      updatedOu = organizationalUnitService.update(newOu, editModel.getParentOuDn());

    } catch (ServiceException e) {
      handleException(bindingResult, e);
      getLogger().debug("Updating organizational unit failed. Some fields were invalid.");
      return "management/organizational-unit-edit";
    }

    model.clear();
    String newName = updatedOu.getName();
    RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS,
        String.format("Organizational unit '%s' was successfully updated.", newName),
        "todo", newName);
    redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

    Map<String, Object> parameters = getParamterMap();
    String redirect = getRedirectUri(
        "organizational-unit-edit?name={{name}}",
        PAGE_AND_OU_PARAMS,
        putToParameterMap(parameters, "name", updatedOu.getDistinguishedName()));
    logRedirectTo("Organizational unit successfully added.", redirect);
    return redirect;
  }

  private void handleException(BindingResult bindingResult, ServiceException serviceException) {

    Object bindTarget = bindingResult.getTarget();
    getLogger().debug("handleException of bind target '{}'", bindTarget, serviceException);
    Assert.isTrue(bindTarget instanceof OrganizationalUnitEditModel, "Illegal bind target.");
    String errorCode = Objects.requireNonNullElse(serviceException.getErrorCode(), "");
    switch (errorCode) {
      case EC_OU_NAME_REQUIRED: {
        bindingResult.rejectValue("name", "code",
            "Name of organizational unit is required.");
        break;
      }
      case EC_ILLEGAL_OU_NAME: {
        bindingResult.rejectValue("name", "code",
            "Name of organizational unit contains illegal characters.");
        break;
      }
      case EC_OU_ALREADY_EXISTS: {
        bindingResult.rejectValue("name", "code",
            "Organizational unit already exists.");
        bindingResult.rejectValue("parentOu", "code",
            "Organizational unit already exists.");
        break;
      }
      case EC_ILLEGAL_SYSTEM_ENTITY_OPERATION: {
        bindingResult.rejectValue("name", "code",
            "Organizational unit is a critical system object. Moving and renaming is not permitted.");
        bindingResult.rejectValue("parentOu", "code",
            "Organizational unit is a critical system object. Moving and renaming is not permitted.");
        break;
      }
      default: {
        getLogger().error("Adding group failed with a not mapped exception.", serviceException);
        throw serviceException;
      }
    }
  }

}
