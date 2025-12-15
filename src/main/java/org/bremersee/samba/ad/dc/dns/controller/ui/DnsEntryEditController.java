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

package org.bremersee.samba.ad.dc.dns.controller.ui;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.common.controller.ui.AbstractEditController;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.dns.controller.ui.shared.DnsZoneTypeComponent;
import org.bremersee.samba.ad.dc.common.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.dns.controller.ui.model.DnsEntryEditRequest;
import org.bremersee.samba.ad.dc.common.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.common.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.dns.model.DnsEntry;
import org.bremersee.samba.ad.dc.dns.model.DnsEntryType;
import org.bremersee.samba.ad.dc.dns.servive.DnsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The type DnsEntryEditController.
 *
 * @author Christian Bremer
 */
@Controller
@Slf4j
public class DnsEntryEditController extends AbstractEditController implements PageableComponent,
    DnsZoneTypeComponent {

  private final DnsService dnsService;

  public DnsEntryEditController(
      DomainControllerProperties properties,
      LocaleResolver localeResolver, DnsService dnsService) {
    super(properties, localeResolver);
    this.dnsService = dnsService;
  }

  @Override
  public String getDefaultSort() {
    return DNS_ENTRY_SORT;
  }

  @GetMapping(path = "/admin/dns-entry-edit")
  public String displayEditDnsEntry(
      @RequestParam(name = ZONE_NAME) String zoneName,
      @RequestParam(name = "name") String name,
      @RequestParam(name = "type") DnsEntryType type,
      @RequestParam(name = "value") String value,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    log.debug("displayEditDnsEntry({}, {}, {}, {})", zoneName, name, type, value);
    return dnsService.findDnsEntry(DnsEntry.builder()
            .zoneName(zoneName)
            .name(name)
            .type(type)
            .value(value)
            .build())
        .map(entry -> {
          if (entry.isConflict()) {
            Map<String, Object> parameters = getParamterMap();
            parameters = putToParameterMap(parameters, "name", name);
            parameters = putToParameterMap(parameters, "type", type);
            parameters = putToParameterMap(parameters, "value", value);
            return getRedirectUri("dns-entry-conflict", PAGE_AND_DNS_ENTRY_PARAMS, parameters);
          }
          model.addAttribute("dnsEntry", entry);
          model.addAttribute("types", DnsEntryType.getSupportedUpdateTypes(entry));
          DnsEntryEditRequest entryEditRequest = dnsService.findReverseDnsEntry(entry)
              .map(reverseDnsEntry -> {
                model.addAttribute("reverseDnsEntryExists", true);
                model.addAttribute("reverseDnsEntry", reverseDnsEntry);
                return new DnsEntryEditRequest(entry, reverseDnsEntry);
              })
              .orElseGet(() -> {
                model.addAttribute("reverseDnsEntryExists", false);
                return new DnsEntryEditRequest(entry);
              });
          model.addAttribute("dnsEntryEditRequest", entryEditRequest);
          return "admin/dns-entry-edit";
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "Dns Entry", "todo", name, PAGE_AND_ZONE_NAME_PARAMS,
            "dns-zone-entries"));
  }

  @PostMapping(path = "/admin/dns-entry-edit")
  public String updateDnsEntry(
      @RequestParam(name = ZONE_NAME) String zoneName,
      @RequestParam(name = "name") String name,
      @RequestParam(name = "type") DnsEntryType type,
      @RequestParam(name = "value") String value,
      @RequestParam(name = "reverse-zone-name", required = false) String reverseZoneName,
      @RequestParam(name = "reverse-name", required = false) String reverseName,
      @RequestParam(name = "reverse-type", required = false) DnsEntryType reverseType,
      @RequestParam(name = "reverse-value", required = false) String reverseValue,
      @ModelAttribute(name = "dnsEntryEditRequest") DnsEntryEditRequest dnsEntryEditRequest,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    log.debug("updateDnsEntry({}, {}, {}, {}, {})",
        zoneName, name, type, value, dnsEntryEditRequest);

    model.clear();
    Map<String, Object> parameters = getParamterMap();

    try {
      DnsEntry dnsEntry = DnsEntry.builder()
          .zoneName(zoneName)
          .name(name)
          .type(type)
          .value(value)
          .build();
      DnsEntry updatedDnsEntry = dnsEntryEditRequest.toNewDnsEntry(zoneName);
      if (name.equals(dnsEntryEditRequest.getNewName())
          && type.equals(dnsEntryEditRequest.getNewType())) {
        dnsService.updateDnsEntry(dnsEntry, dnsEntryEditRequest.getNewValue());
      } else {
        dnsService.deleteDnsEntry(dnsEntry);
        dnsService.addDnsEntry(dnsEntryEditRequest.toNewDnsEntry(zoneName));
      }
      if (dnsEntryEditRequest.isUpdateReverseEntry() && !isEmpty(reverseZoneName)
          && !isEmpty(reverseName) && !isEmpty(reverseType) && !isEmpty(reverseValue)
          && !isEmpty(dnsEntryEditRequest.getNewNameOfReverseEntry())
          && !isEmpty(dnsEntryEditRequest.getNewValueOfReverseEntry())
          && type.equals(updatedDnsEntry.getType())) {
        DnsEntry reverseDnsEntry = DnsEntry.builder()
            .zoneName(reverseZoneName)
            .name(reverseName)
            .type(reverseType)
            .value(reverseValue)
            .build();
        if (reverseName.equals(dnsEntryEditRequest.getNewNameOfReverseEntry())) {
          dnsService.updateDnsEntry(
              reverseDnsEntry, dnsEntryEditRequest.getNewValueOfReverseEntry());
        } else {
          dnsService.deleteDnsEntry(reverseDnsEntry);
          dnsService.addDnsEntry(DnsEntry.builder()
              .from(reverseDnsEntry)
              .name(dnsEntryEditRequest.getNewNameOfReverseEntry())
              .value(dnsEntryEditRequest.getNewValueOfReverseEntry())
              .build());
        }
      }

      String msg = String.format("Dns entry '%s' was successfully updated.",
          updatedDnsEntry.getName());
      RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS, msg,
          "todo", updatedDnsEntry.getName());
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

      parameters = putToParameterMap(parameters, "name", updatedDnsEntry.getName());
      parameters = putToParameterMap(parameters, "type", updatedDnsEntry.getType());
      parameters = putToParameterMap(parameters, "value", updatedDnsEntry.getValue());
      String redirect = getRedirectUri("dns-entry-edit",
          PAGE_AND_DNS_ENTRY_PARAMS, parameters);
      logRedirectTo("Dns entry successfully updated.", redirect);
      return redirect;

    } catch (ServiceException e) {

      log.error("Updating dns entry failed.", e);

      String msg = String.format("Updating of dns entry '%s' failed.", name);
      RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.WARNING, msg,
          "todo", name);
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);
      String redirect = getRedirectUri("dns-zone-entries",
          PAGE_AND_ZONE_NAME_PARAMS, parameters);
      logRedirectTo("Updating dns entry failed.", redirect);
      return redirect;
    }
  }

}
