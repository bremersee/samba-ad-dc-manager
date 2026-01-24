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

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.model.DnsEntryEditModel;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The dns entry edit controller.
 *
 * @author Christian Bremer
 */
@Controller
@Slf4j
public class DnsEntryEditController extends UiController
    implements PageableComponent, DnsZoneTypeComponent {

  private final DnsService dnsService;

  /**
   * Instantiates a new dns entry edit controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param dnsService the dns service
   */
  public DnsEntryEditController(
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
   * Display edit dns entry view.
   *
   * @param zoneName the zone name
   * @param name the name
   * @param type the type
   * @param value the value
   * @param model the model
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @GetMapping(path = "/management/dns-entry-edit")
  public String displayEditDnsEntry(
      @RequestParam(name = ZONE_NAME) String zoneName,
      @RequestParam(name = DNS_ENTRY_NAME) String name,
      @RequestParam(name = DNS_ENTRY_TYPE) DnsEntryType type,
      @RequestParam(name = DNS_ENTRY_VALUE) String value,
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
            parameters = putToParameterMap(parameters, DNS_ENTRY_NAME, name);
            parameters = putToParameterMap(parameters, DNS_ENTRY_TYPE, type);
            parameters = putToParameterMap(parameters, DNS_ENTRY_VALUE, value);
            return getRedirectUri("dns-entry-conflict", PAGE_AND_DNS_ENTRY_PARAMS, parameters);
          }
          model.addAttribute("dnsEntry", entry);
          model.addAttribute("types", DnsEntryType.getSupportedUpdateTypes(entry));
          DnsEntryEditModel editModel = dnsService.findReverseDnsEntry(entry)
              .map(reverseDnsEntry -> {
                model.addAttribute("reverseDnsEntryExists", true);
                model.addAttribute("reverseDnsEntry", reverseDnsEntry);
                return new DnsEntryEditModel(entry, reverseDnsEntry);
              })
              .orElseGet(() -> {
                model.addAttribute("reverseDnsEntryExists", false);
                return new DnsEntryEditModel(entry);
              });
          model.addAttribute("dnsEntryEditModel", editModel);
          return "management/dns-entry-edit";
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "Dns Entry", "dns-entry.not-found", name,
            PAGE_AND_ZONE_NAME_PARAMS, "dns-zone-entries"));
  }

  /**
   * Update dns entry.
   *
   * @param zoneName the zone name
   * @param name the name
   * @param type the type
   * @param value the value
   * @param reverseZoneName the reverse zone name
   * @param reverseName the reverse name
   * @param reverseType the reverse type
   * @param reverseValue the reverse value
   * @param editModel the edit model
   * @param model the model
   * @param redirectAttributes the redirect attributes
   * @return the view
   */
  @PostMapping(path = "/management/dns-entry-edit")
  public String updateDnsEntry(
      @RequestParam(name = ZONE_NAME) String zoneName,
      @RequestParam(name = DNS_ENTRY_NAME) String name,
      @RequestParam(name = DNS_ENTRY_TYPE) DnsEntryType type,
      @RequestParam(name = DNS_ENTRY_VALUE) String value,
      @RequestParam(name = "reverse-zone-name", required = false) String reverseZoneName,
      @RequestParam(name = "reverse-name", required = false) String reverseName,
      @RequestParam(name = "reverse-type", required = false) DnsEntryType reverseType,
      @RequestParam(name = "reverse-value", required = false) String reverseValue,
      @ModelAttribute(name = "dnsEntryEditModel") DnsEntryEditModel editModel,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    log.debug("updateDnsEntry({}, {}, {}, {}, {})",
        zoneName, name, type, value, editModel);

    model.clear();
    Map<String, Object> parameters = getParamterMap();

    try {
      DnsEntry dnsEntry = DnsEntry.builder()
          .zoneName(zoneName)
          .name(name)
          .type(type)
          .value(value)
          .build();
      if (name.equals(editModel.getNewName())
          && type.equals(editModel.getNewType())) {
        dnsService.updateDnsEntry(dnsEntry, editModel.getNewValue());
      } else {
        dnsService.deleteDnsEntry(dnsEntry);
        dnsService.addDnsEntry(editModel.toNewDnsEntry(zoneName));
      }
      if (editModel.isUpdateReverseEntry() && !isEmpty(reverseZoneName)
          && !isEmpty(reverseName) && !isEmpty(reverseType) && !isEmpty(reverseValue)
          && !isEmpty(editModel.getNewNameOfReverseEntry())
          && !isEmpty(editModel.getNewValueOfReverseEntry())
          && type.equals(editModel.getNewType())) {
        DnsEntry reverseDnsEntry = DnsEntry.builder()
            .zoneName(reverseZoneName)
            .name(reverseName)
            .type(reverseType)
            .value(reverseValue)
            .build();
        if (reverseName.equals(editModel.getNewNameOfReverseEntry())) {
          dnsService.updateDnsEntry(
              reverseDnsEntry, editModel.getNewValueOfReverseEntry());
        } else {
          dnsService.deleteDnsEntry(reverseDnsEntry);
          dnsService.addDnsEntry(DnsEntry.builder()
              .from(reverseDnsEntry)
              .name(editModel.getNewNameOfReverseEntry())
              .value(editModel.getNewValueOfReverseEntry())
              .build());
        }
      }

      String msg = String.format("Dns entry '%s' was successfully updated.",
          editModel.getNewName());
      RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS, msg,
          "dns-entry-edit.success", editModel.getNewName());
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);

      parameters = putToParameterMap(parameters, DNS_ENTRY_NAME, editModel.getNewName());
      parameters = putToParameterMap(parameters, DNS_ENTRY_TYPE, editModel.getNewType());
      parameters = putToParameterMap(parameters, DNS_ENTRY_VALUE, editModel.getNewValue());
      String redirect = getRedirectUri("dns-entry-edit",
          PAGE_AND_DNS_ENTRY_PARAMS, parameters);
      logRedirectTo("Dns entry successfully updated.", redirect);
      return redirect;

    } catch (ServiceException e) {

      log.error("Updating dns entry failed.", e);

      String msg = String.format("Updating of dns entry '%s' failed.", name);
      RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.WARNING, msg,
          "dns-entry-edit.failure", name);
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);
      String redirect = getRedirectUri("dns-zone-entries",
          PAGE_AND_ZONE_NAME_PARAMS, parameters);
      logRedirectTo("Updating dns entry failed.", redirect);
      return redirect;
    }
  }

}
