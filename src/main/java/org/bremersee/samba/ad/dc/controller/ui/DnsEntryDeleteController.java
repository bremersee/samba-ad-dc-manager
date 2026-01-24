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
import org.bremersee.samba.ad.dc.controller.ui.model.DnsEntryDeleteModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.DnsZoneTypeComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsEntryType;
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
 * The dns entry delete controller.
 *
 * @author Christian Bremer
 */
@Controller
@Slf4j
public class DnsEntryDeleteController extends UiController
    implements PageableComponent, DnsZoneTypeComponent {

  private final DnsService dnsService;

  /**
   * Instantiates a new dns entry delete controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param dnsService the dns service
   */
  public DnsEntryDeleteController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DnsService dnsService) {
    super(properties, localeResolver, domainService);
    this.dnsService = dnsService;
  }

  @Override
  public String getDefaultSort() {
    return DNS_ENTRY_SORT;
  }

  /**
   * Display delete dns entry view.
   *
   * @param zoneName the zone name
   * @param name the name
   * @param type the type
   * @param value the value
   * @param model the model
   * @return the view
   */
  @GetMapping(path = "/management/dns-entry-delete")
  public String displayDeleteDnsEntry(
      @RequestParam(name = ZONE_NAME) String zoneName,
      @RequestParam(name = DNS_ENTRY_NAME) String name,
      @RequestParam(name = DNS_ENTRY_TYPE) DnsEntryType type,
      @RequestParam(name = DNS_ENTRY_VALUE) String value,
      ModelMap model) {

    log.debug("displayDeleteDnsEntry({}, {}, {}, {})", zoneName, name, type, value);
    DnsEntry dnsEntry = DnsEntry.builder()
        .zoneName(zoneName)
        .name(name)
        .type(type)
        .value(value)
        .build();
    model.addAttribute("dnsEntry", dnsEntry);
    DnsEntryDeleteModel deleteModel = new DnsEntryDeleteModel();
    boolean mayHaveReverseEntry = mayHaveReverseEntry(dnsEntry);
    model.addAttribute("mayHaveReverseEntry", mayHaveReverseEntry);
    deleteModel.setDeleteReverseEntry(mayHaveReverseEntry);
    model.addAttribute("dnsEntryDeleteModel", deleteModel);
    return "management/dns-entry-delete";
  }

  /**
   * Delete dns entry.
   *
   * @param zoneName the zone name
   * @param name the name
   * @param type the type
   * @param value the value
   * @param deleteModel the delete model
   * @param model the model
   * @param bindingResult the binding result
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @PostMapping(path = "/management/dns-entry-delete")
  public String deleteDnsEntry(
      @RequestParam(name = ZONE_NAME) String zoneName,
      @RequestParam(name = "name") String name,
      @RequestParam(name = "type") DnsEntryType type,
      @RequestParam(name = "value") String value,
      @ModelAttribute(name = "dnsEntryDeleteModel") DnsEntryDeleteModel deleteModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    log.debug("deleteDnsEntry({}, {}, {}, {}, {})", zoneName, name, type, value, deleteModel);

    DnsEntry dnsEntry = DnsEntry.builder()
        .zoneName(zoneName)
        .name(name)
        .type(type)
        .value(value)
        .build();
    if (!name.equalsIgnoreCase(deleteModel.getVerificationName())) {
      bindingResult.rejectValue("verificationName", "dns-entry-delete.name-does-not-match",
          "The name doesn't match.");
      model.addAttribute("dnsEntry", dnsEntry);
      model.addAttribute("mayHaveReverseEntry", mayHaveReverseEntry(dnsEntry));
      return "management/dns-entry-delete";
    }

    model.clear();
    try {
      dnsService.deleteDnsEntry(dnsEntry);

      if (deleteModel.isDeleteReverseEntry() && (DnsEntryType.A.equals(type)
          || DnsEntryType.AAAA.equals(type) || DnsEntryType.PTR.equals(type))) {
        dnsService.findReverseDnsEntry(dnsEntry)
            .ifPresent(dnsService::deleteDnsEntry);
      }

      String msg = String.format("Dns entry '%s' was successfully deleted.", name);
      RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS, msg,
          "dns-entry-delete.success", name);
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

    } catch (ServiceException e) {

      String msg = String.format("Deletion of dns entry '%s' failed.", name);
      log.error(msg, e);
      RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.WARNING, msg,
          "dns-entry-delete.failure", name);
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);
    }

    Map<String, Object> parameters = getParamterMap();
    String redirect = getRedirectUri("dns-zone-entries",
        PAGE_AND_ZONE_NAME_PARAMS, parameters);
    logRedirectTo("Dns deletion redirect.", redirect);
    return redirect;
  }

  private boolean mayHaveReverseEntry(DnsEntry dnsEntry) {
    return DnsEntryType.A.equals(dnsEntry.getType())
        || DnsEntryType.AAAA.equals(dnsEntry.getType())
        || DnsEntryType.PTR.equals(dnsEntry.getType());
  }

}
