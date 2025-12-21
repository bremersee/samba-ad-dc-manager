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
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.controller.ui.model.DnsEntryAddRequest;
import org.bremersee.samba.ad.dc.controller.ui.shared.DnsZoneTypeComponent;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsEntryType;
import org.bremersee.samba.ad.dc.model.DnsZoneType;
import org.bremersee.samba.ad.dc.service.DnsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The type DnsEntryAddController.
 *
 * @author Christian Bremer
 */
@Controller
@Slf4j
public class DnsEntryAddController extends AbstractEditController implements PageableComponent,
    DnsZoneTypeComponent {

  private final DnsService dnsService;

  public DnsEntryAddController(
      DomainControllerProperties properties,
      LocaleResolver localeResolver, DnsService dnsService) {
    super(properties, localeResolver);
    this.dnsService = dnsService;
  }

  @Override
  public String getDefaultSort() {
    return DNS_ENTRY_SORT;
  }

  @GetMapping(path = "/admin/dns-entry-add")
  public String displayAddDnsEntry(
      @RequestParam(name = ZONE_NAME) String zoneName,
      ModelMap model) {

    log.debug("displayAddDnsEntry({})", zoneName);
    model.addAttribute("zoneName", zoneName);
    model.addAttribute("types", DnsEntryType.getSupportedAddOrDeleteTypes());
    DnsEntryAddRequest addRequest = new DnsEntryAddRequest(zoneName);
    List<String> reverseZones = dnsService.getDnsZoneNames(DnsZoneType.REVERSE);
    if (!reverseZones.isEmpty()) {
      addRequest.setReverseZoneName(reverseZones.get(0));
    }
    model.addAttribute("dnsReverseZones", reverseZones);
    model.addAttribute("dnsEntryAddRequest", addRequest);
    return "admin/dns-entry-add";
  }

  @PostMapping(path = "/admin/dns-entry-add")
  public String addDnsEntry(
      @RequestParam(name = ZONE_NAME) String zoneName,
      @ModelAttribute(name = "dnsEntryAddRequest") DnsEntryAddRequest dnsEntryAddRequest,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    log.debug("addDnsEntry({}, {})", zoneName, dnsEntryAddRequest);
    DnsEntry dnsEntry = dnsEntryAddRequest.toDnsEntry(zoneName);

    model.clear();
    Map<String, Object> parameters = getParamterMap();

    try {
      dnsService.addDnsEntry(dnsEntry);
      dnsEntryAddRequest.toReverseDnsEntry().ifPresent(dnsService::addDnsEntry);

      String msg = String.format("Dns entry '%s' was successfully added.",
          dnsEntry.getDisplayName());
      RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS, msg,
          "todo", dnsEntry.getDisplayName());
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

      parameters = putToParameterMap(parameters, "name", dnsEntry.getName());
      parameters = putToParameterMap(parameters, "type", dnsEntry.getType());
      parameters = putToParameterMap(parameters, "value", dnsEntry.getValue());
      String redirect = getRedirectUri("dns-entry-edit",
          PAGE_AND_DNS_ENTRY_PARAMS, parameters);
      logRedirectTo("Dns entry successfully added.", redirect);
      return redirect;

    } catch (ServiceException e) {

      String msg = String.format("Adding of dns entry '%s' failed.", dnsEntryAddRequest.getName());
      log.error(msg, e);
      RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.WARNING, msg,
          "todo", dnsEntryAddRequest.getName());
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);
      String redirect = getRedirectUri("dns-zone-entries",
          PAGE_AND_ZONE_NAME_PARAMS, parameters);
      logRedirectTo("Adding dns entry failed.", redirect);
      return redirect;
    }
  }

}
