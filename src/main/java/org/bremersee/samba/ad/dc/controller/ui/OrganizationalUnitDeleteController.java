/*
 * Copyright 2025-2026 the original author or authors.
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
import org.bremersee.samba.ad.dc.controller.ui.model.OrganizationalUnitDeleteModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.NameValue;
import org.ldaptive.dn.RDn;
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
 * The organizational unit delete controller.
 *
 * @author Christian Bremer
 */
@Controller
public class OrganizationalUnitDeleteController extends UiController
    implements PageableComponent, RedirectComponent {

  private static final String ORGANIZATIONAL_UNITS = "organizational-units";

  private final OrganizationalUnitService organizationalUnitService;

  /**
   * Instantiates a new organizational unit delete controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param organizationalUnitService the organizational unit service
   */
  public OrganizationalUnitDeleteController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      OrganizationalUnitService organizationalUnitService) {
    super(properties, localeResolver, domainService);
    this.organizationalUnitService = organizationalUnitService;
  }

  @Override
  public String getDefaultSort() {
    return OU_SORT;
  }

  /**
   * Display organizational unit delete view.
   *
   * @param ouDn the ou dn
   * @param model the model
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @GetMapping(path = "/management/organizational-unit-delete")
  public String displayOrganizationalUnitDelete(
      @RequestParam(value = "name", required = false) Dn ouDn,
      ModelMap model,
      RedirectAttributes redirectAttributes) {
    getLogger().debug("displayOrganizationalUnitDelete({})", ouDn);
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
          boolean hasChildren = organizationalUnitService
              .hasChildren(ou.getDn());
          model.addAttribute("hasChildren", hasChildren);
          OrganizationalUnitDeleteModel deleteModel = new OrganizationalUnitDeleteModel(
              ou);
          model.put("deleteModel", deleteModel);
          return "management/organizational-unit-delete";
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "Organizational Unit", "organizational-unit.not-found", name,
            PAGE_AND_OU_PARAMS, ORGANIZATIONAL_UNITS));
  }

  /**
   * Delete organizational unit.
   *
   * @param deleteModel the delete model
   * @param model the model
   * @param bindingResult the binding result
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @PostMapping(path = "/management/organizational-unit-delete")
  public String deleteOrganizationalUnit(
      @ModelAttribute(name = "deleteModel") OrganizationalUnitDeleteModel deleteModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    getLogger().debug("deleteOrganizationalUnit({})", deleteModel);
    String name = Optional.ofNullable(deleteModel.getOu())
        .map(Dn::new)
        .map(Dn::getRDn)
        .map(RDn::getNameValue)
        .map(NameValue::getStringValue)
        .orElse("null");
    return Optional.ofNullable(deleteModel.getOu())
        .map(Dn::new)
        .flatMap(organizationalUnitService::getOrganizationalUnit)
        .map(ou -> {
          if (!ou.getName().equalsIgnoreCase(deleteModel.getVerificationName())) {
            bindingResult.rejectValue("verificationName",
                "organization-unit-delete.name-does-not-match", "The name doesn't match.");
            model.addAttribute("organizationalUnit", ou);
            boolean hasChildren = organizationalUnitService.hasChildren(ou.getDn());
            model.addAttribute("hasChildren", hasChildren);
            return "management/organizational-unit-delete";
          }

          boolean result = organizationalUnitService.delete(ou.getDn());

          model.clear();
          RedirectMessage rmsg;
          if (result) {
            rmsg = getRedirectMessage(RedirectMessageType.SUCCESS,
                String.format("Organizational unit '%s' was successfully deleted.", name),
                "organization-unit-delete.success", name);
          } else {
            rmsg = getRedirectMessage(RedirectMessageType.WARNING,
                String.format("Somehow the organizational unit '%s' was not deleted.", name),
                "organization-unit-delete.failure", name);
          }
          redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

          Map<String, Object> parameters = getParameterMap();
          String redirect = getRedirectUri(ORGANIZATIONAL_UNITS, PAGE_AND_OU_PARAMS, parameters);
          logRedirectTo("Organizational unit successfully deleted.", redirect);
          return redirect;
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "Organizational Unit", "organizational-unit.not-found", name,
            PAGE_AND_OU_PARAMS, ORGANIZATIONAL_UNITS));
  }

}
