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
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.AbstractController;
import org.bremersee.samba.ad.dc.controller.ui.mapper.ComputerEditModelMapper;
import org.bremersee.samba.ad.dc.controller.ui.model.ComputerEditModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganisationalUnitsComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.service.DomainComputerService;
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
 * The computer edit controller.
 *
 * @author Christian Bremer
 */
@Controller
public class ComputerEditController extends UiController implements PageableComponent,
    OrganizationalUnitComponent, OrganisationalUnitsComponent {

  private final DomainComputerService domainComputerService;

  private final DomainGroupService domainGroupService;

  @Getter
  private final OrganizationalUnitService organizationalUnitService;

  /**
   * Instantiates a new computer edit controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param domainComputerService the domain computer service
   * @param domainGroupService the domain group service
   * @param organizationalUnitService the organizational unit service
   */
  public ComputerEditController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainComputerService domainComputerService,
      DomainGroupService domainGroupService,
      OrganizationalUnitService organizationalUnitService) {
    super(properties, localeResolver, domainService);
    this.domainComputerService = domainComputerService;
    this.domainGroupService = domainGroupService;
    this.organizationalUnitService = organizationalUnitService;
  }

  @Override
  public String getDefaultSort() {
    return COMPUTER_SORT;
  }

  /**
   * Display computer edit view.
   *
   * @param computerName the computer name
   * @param ou the ou
   * @param searchScope the search scope
   * @param model the model
   * @param redirectAttributes the redirect attributes
   * @return the string
   */
  @GetMapping(path = "/management/computer-edit")
  public String displayComputerEdit(
      @RequestParam(value = "name", required = false) String computerName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    return Optional.ofNullable(computerName)
        .flatMap(name -> domainComputerService.getComputer(name, ou, searchScope))
        .map(computer -> {
          model.addAttribute(COMPUTER, computer);
          addPrimaryGroupToModel(model, computer);
          ComputerEditModel editModel = ComputerEditModelMapper.INSTANCE.map(computer);
          model.addAttribute("editModel", editModel);
          return "management/computer-edit";
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes,
            "Computer",
            "controller.ui.computer.not-found",
            computerName,
            PAGE_AND_OU_PARAMS,
            COMPUTERS));
  }

  /**
   * Update computer.
   *
   * @param samAccountName the sam account name
   * @param ou the ou
   * @param searchScope the search scope
   * @param editModel the edit model
   * @param model the model
   * @param bindingResult the binding result
   * @param redirectAttributes the redirect attributes
   * @return the new view
   */
  @PostMapping(path = "/management/computer-edit")
  public String updateComputer(
      @RequestParam(value = "samAccountName", required = false) String samAccountName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      @ModelAttribute(name = "editModel") ComputerEditModel editModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    getLogger().debug("updateComputer({}, {})", samAccountName, editModel);

    return Optional.ofNullable(samAccountName)
        .flatMap(name -> domainComputerService.getComputer(name, ou, searchScope))
        .map(existingComputer -> updateComputer(
            existingComputer, editModel, model, bindingResult, redirectAttributes))
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes,
            "Computer",
            "controller.ui.computer.not-found",
            samAccountName,
            PAGE_AND_OU_PARAMS,
            COMPUTERS));
  }

  private String updateComputer(
      DomainComputer existingComputer,
      ComputerEditModel computerEditModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    Dn newOu = computerEditModel.getNewOuDn()
        .map(ou -> getDnTool().addBaseDn(ou))
        .filter(ou -> !DnTool.isSameDn(ou, existingComputer.getDn().getParent()))
        .orElse(null);
    try {
      DomainComputer newComputer = ComputerEditModelMapper.INSTANCE
          .merge(computerEditModel, existingComputer);
      DomainComputer updatedComputer = domainComputerService
          .updateComputer(newComputer, newOu);

      model.clear();
      String msg = String.format("Computer '%s' was successfully updated.",
          updatedComputer.getName());
      RedirectMessage rmsg = getRedirectMessage(
          RedirectMessageType.SUCCESS,
          msg,
          "controller.ui.computer-edit-c.updated-message",
          updatedComputer.getName());
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

      Map<String, Object> parameters = getParamterMap(updatedComputer.getDn().getParent());
      String redirect = getRedirectUri("computer-edit?name={{computer.samAccountName}}",
          PAGE_AND_OU_PARAMS,
          putToParameterMap(
              parameters,
              COMPUTER,
              updatedComputer));
      logRedirectTo("Computer successfully updated.", redirect);
      return redirect;

    } catch (ServiceException e) {
      handleException(bindingResult, e);
      DomainComputer partialUpdatedComputer = Optional.ofNullable(newOu)
          .flatMap(ou -> domainComputerService
              .getComputer(existingComputer.getSamAccountName(), null, null))
          .orElse(existingComputer);
      model.addAttribute(
          AbstractController.OU,
          partialUpdatedComputer.getDn().getParent().format());
      model.addAttribute(COMPUTER, partialUpdatedComputer);
      addPrimaryGroupToModel(model, partialUpdatedComputer);
      return "management/computer-edit";
    }
  }

  private void handleException(BindingResult bindingResult, ServiceException serviceException) {

    Object bindTarget = bindingResult.getTarget();
    getLogger().debug("handleException of bind target '{}'", bindTarget, serviceException);
    Assert.isTrue(bindTarget instanceof ComputerEditModel, "Illegal bind target.");
    String errorCode = Objects.requireNonNullElse(serviceException.getErrorCode(), "");
    switch (errorCode) {
      case EC_EMPTY_OU_RDN: {
        bindingResult.rejectValue("newOu", "ec.empty-ou-rdn",
            "Organizational unit is empty.");
        break;
      }
      case EC_OU_NOT_FOUND: {
        bindingResult.rejectValue("newOu", "ec.ou-not-found",
            "Organizational unit was not found.");
        break;
      }
      default: {
        getLogger().error("Editing computer failed with a not mapped exception.", serviceException);
        throw serviceException;
      }
    }
  }

  private void addPrimaryGroupToModel(ModelMap model, DomainComputer computer) {
    Optional.ofNullable(computer)
        .map(DomainComputer::getPrimaryGroupId)
        .flatMap(domainGroupService::getGroupByPrimaryGroupId)
        .ifPresent(group -> model.addAttribute("primaryGroup", group));
  }

}
