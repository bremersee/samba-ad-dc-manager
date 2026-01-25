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

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Optional;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.model.SamAccountDeleteModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.service.DomainComputerService;
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
 * The computer delete controller.
 *
 * @author Christian Bremer
 */
@Controller
public class ComputerDeleteController extends UiController implements PageableComponent,
    OrganizationalUnitComponent {

  private final DomainComputerService domainComputerService;

  /**
   * Instantiates a new computer delete controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param domainComputerService the domain computer service
   */
  public ComputerDeleteController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainComputerService domainComputerService) {
    super(properties, localeResolver, domainService);
    this.domainComputerService = domainComputerService;
  }

  @Override
  public String getDefaultSort() {
    return COMPUTER_SORT;
  }

  /**
   * Display computer delete.
   *
   * @param computerName the computer name
   * @param ou the ou
   * @param searchScope the search scope
   * @param model the model
   * @param redirectAttributes the redirect attributes
   * @return the string
   */
  @GetMapping(path = "/management/computer-delete")
  public String displayComputerDelete(
      @RequestParam(value = "name", required = false) String computerName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    return Optional.ofNullable(computerName)
        .flatMap(name -> domainComputerService.getComputer(computerName, ou, searchScope))
        .map(computer -> {
          model.addAttribute(COMPUTER, computer);
          model.addAttribute("deleteModel", new SamAccountDeleteModel(computer));
          return "management/computer-delete";
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes,
            "Computer",
            "computer.not-found",
            computerName,
            PAGE_AND_OU_PARAMS,
            COMPUTERS));
  }

  /**
   * Delete computer.
   *
   * @param ou the ou
   * @param searchScope the search scope
   * @param deleteModel the delete model
   * @param model the model
   * @param bindingResult the binding result
   * @param redirectAttributes the redirect attributes
   * @return the string
   */
  @PostMapping(path = "/management/computer-delete")
  public String deleteComputer(
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      @ModelAttribute("deleteModel") SamAccountDeleteModel deleteModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    getLogger().debug("deleteComputer({})", deleteModel);
    return Optional.ofNullable(deleteModel.getSamAccountName())
        .flatMap(name -> domainComputerService.getComputer(name, ou, searchScope))
        .map(computer -> {
          if (isVerified(computer, deleteModel)) {
            return deleteComputer(computer, model, redirectAttributes);
          }
          bindingResult.rejectValue(
              "verificationName",
              "computer-delete.name-does-not-match",
              "The name doesn't match.");
          model.addAttribute(COMPUTER, computer);
          return "management/computer-delete";
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes,
            COMPUTER,
            "computer.not-found",
            deleteModel.getSamAccountName(),
            PAGE_AND_OU_PARAMS,
            COMPUTERS));
  }

  private String deleteComputer(
      DomainComputer computer,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    boolean result = domainComputerService.deleteComputer(computer.getSamAccountName());
    model.clear();
    RedirectMessage redirectMessage;
    if (result) {
      redirectMessage = getRedirectMessage(
          RedirectMessageType.SUCCESS,
          String.format("Computer '%s' was successfully deleted.", computer.getName()),
          "computer-delete.success",
          computer.getName());
    } else {
      redirectMessage = getRedirectMessage(
          RedirectMessageType.WARNING,
          String.format("Somehow the computer '%s' was not deleted.", computer.getName()),
          "computer-delete.failure",
          computer.getName());
    }
    redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, redirectMessage);
    String redirect = getRedirectUri(
        COMPUTERS,
        PAGE_AND_OU_PARAMS,
        getParameterMap());
    logRedirectTo("Computer deletion message.", redirect);
    return redirect;
  }

  private boolean isVerified(DomainComputer computer, SamAccountDeleteModel deleteModel) {
    if (isEmpty(computer) || isEmpty(deleteModel)) {
      return false;
    }
    return Optional.ofNullable(computer.getName())
        .filter(name -> name.equalsIgnoreCase(deleteModel.getVerificationName()))
        .isPresent();
  }

}
