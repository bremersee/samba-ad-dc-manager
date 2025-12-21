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

import java.util.Map;
import java.util.Optional;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.controller.ui.model.OrganizationalUnitDeleteModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
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
 * The type OrganizationalUnitAddController.
 *
 * @author Christian Bremer
 */
@Controller
public class OrganizationalUnitDeleteController extends UiController
    implements PageableComponent, RedirectComponent {

  private final OrganizationalUnitService organizationalUnitService;

  @Override
  public String getDefaultSort() {
    return OU_SORT;
  }

  public OrganizationalUnitDeleteController(
      DomainControllerProperties properties,
      LocaleResolver localeResolver,
      OrganizationalUnitService organizationalUnitService) {
    super(properties, localeResolver);
    this.organizationalUnitService = organizationalUnitService;
  }

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
          OrganizationalUnitDeleteModel ouDeleteRequest = new OrganizationalUnitDeleteModel(
              ou);
          model.put("ouDeleteRequest", ouDeleteRequest);
          return "management/organizational-unit-delete";
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "Organizational Unit", "todo", name, PAGE_AND_OU_PARAMS, "organizational-units"));
  }

  @PostMapping(path = "/management/organizational-unit-delete")
  public String deleteOrganizationalUnit(
      @ModelAttribute(name = "ouDeleteRequest") OrganizationalUnitDeleteModel ouDeleteRequest,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    getLogger().debug("deleteOrganizationalUnit({})", ouDeleteRequest);
    String name = Optional.ofNullable(ouDeleteRequest.getOu())
        .map(Dn::new)
        .map(Dn::getRDn)
        .map(RDn::getNameValue)
        .map(NameValue::getStringValue)
        .orElse("null");
    return Optional.ofNullable(ouDeleteRequest.getOu())
        .map(Dn::new)
        .flatMap(organizationalUnitService::getOrganizationalUnit)
        .map(ou -> {
          if (!ou.getName().equalsIgnoreCase(ouDeleteRequest.getVerificationName())) {
            bindingResult.rejectValue("verificationName", "todo", "The name doesn't match.");
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
                "todo", name);
          } else {
            rmsg = getRedirectMessage(RedirectMessageType.WARNING,
                String.format("Somehow the organizational unit '%s' was not deleted.", name),
                "todo", name);
          }
          redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

          Map<String, Object> parameters = getParamterMap();
          String redirect = getRedirectUri("organizational-units", PAGE_AND_OU_PARAMS, parameters);
          logRedirectTo("Organizational unit successfully deleted.", redirect);
          return redirect;
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "Organizational Unit", "todo", name, PAGE_AND_OU_PARAMS, "organizational-units"));
  }

}
