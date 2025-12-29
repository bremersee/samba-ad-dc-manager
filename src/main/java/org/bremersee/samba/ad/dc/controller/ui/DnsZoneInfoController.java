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

import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.shared.CurrentPageNameProvider;
import org.bremersee.samba.ad.dc.controller.ui.shared.DnsZoneTypeComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.service.DnsService;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The dns zone info controller.
 *
 * @author Christian Bremer
 */
@Controller
public class DnsZoneInfoController extends UiController
    implements CurrentPageNameProvider, PageableComponent, DnsZoneTypeComponent {

  private final DnsService dnsService;

  public DnsZoneInfoController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DnsService dnsService) {
    super(properties, localeResolver, domainService);
    this.dnsService = dnsService;
  }

  @Override
  public String getCurrentPageName() {
    return "dns-zone-info";
  }

  @Override
  public String getDefaultSort() {
    return DNS_ENTRY_SORT;
  }

  @GetMapping(path = "/management/dns-zone-info")
  public String displayDnsZoneInfo(
      @RequestParam(name = ZONE_NAME) String zoneName,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    try {
      model.addAttribute("zone", dnsService.getDnsZone(zoneName));
      return "management/dns-zone-info";

    } catch (ServiceException serviceException) {

      return entityNotFoundRedirect(
          redirectAttributes, "DNS Zone", "todo", zoneName, PAGE_AND_ZONE_TYPE_PARAMS,
          "dns-zones");
    }
  }

}
