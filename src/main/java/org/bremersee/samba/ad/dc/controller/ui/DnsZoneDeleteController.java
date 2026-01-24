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
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.model.DnsZoneDeleteModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.CurrentPageNameProvider;
import org.bremersee.samba.ad.dc.controller.ui.shared.DnsZoneTypeComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.service.DnsService;
import org.bremersee.samba.ad.dc.service.DomainService;
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
 * The dns zone delete controller.
 *
 * @author Christian Bremer
 */
@Controller
@Slf4j
public class DnsZoneDeleteController extends UiController
    implements CurrentPageNameProvider, PageableComponent, DnsZoneTypeComponent {

  private final DnsService dnsService;

  /**
   * Instantiates a new dns zone delete controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param dnsService the dns service
   */
  public DnsZoneDeleteController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DnsService dnsService) {
    super(properties, localeResolver, domainService);
    this.dnsService = dnsService;
  }

  @Override
  public String getCurrentPageName() {
    return "dns-zone-delete";
  }

  @Override
  public String getDefaultSort() {
    return DNS_ENTRY_SORT;
  }

  /**
   * Display dns zone delete view.
   *
   * @param zoneName the zone name
   * @param model the model
   * @return the view
   */
  @GetMapping(path = "/management/dns-zone-delete")
  public String displayDnsZoneDelete(
      @RequestParam(name = ZONE_NAME) String zoneName,
      ModelMap model) {

    model.addAttribute("zoneName", zoneName);
    model.addAttribute("dnsZoneDeleteModel", new DnsZoneDeleteModel());
    return "management/dns-zone-delete";
  }

  /**
   * Delete dns zone.
   *
   * @param zoneName the zone name
   * @param deleteModel the delete model
   * @param model the model
   * @param bindingResult the binding result
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @PostMapping(path = "/management/dns-zone-delete")
  public String deleteDnsZone(
      @RequestParam(name = ZONE_NAME) String zoneName,
      @ModelAttribute(name = "dnsZoneDeleteModel") DnsZoneDeleteModel deleteModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    log.debug("deleteDnsEntry({}, {})", zoneName, deleteModel);

    if (!zoneName.equalsIgnoreCase(deleteModel.getVerificationName())) {
      bindingResult.rejectValue("verificationName", "dns-zone-delete.name-does-not-match",
          "The name doesn't match.");
      model.addAttribute("zoneName", zoneName);
      return "management/dns-zone-delete";
    }

    model.clear();
    try {
      dnsService.deleteDnsZone(zoneName);

      String msg = String.format("Dns zone '%s' was successfully deleted.", zoneName);
      RedirectMessage redirectMessage = getRedirectMessage(RedirectMessageType.SUCCESS, msg,
          "dns-zone-delete.success", zoneName);
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, redirectMessage);

    } catch (ServiceException e) {

      String msg = String.format("Deletion of dns zone '%s' failed.", zoneName);
      log.error(msg, e);
      RedirectMessage redirectMessage = getRedirectMessage(RedirectMessageType.WARNING, msg,
          "dns-zone-delete.failure", zoneName);
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, redirectMessage);
    }

    Map<String, Object> parameters = getParamterMap();
    String redirect = getRedirectUri("dns-zones",
        PAGE_AND_ZONE_TYPE_PARAMS, parameters);
    logRedirectTo("Dns zone deletion redirect.", redirect);
    return redirect;
  }

}
