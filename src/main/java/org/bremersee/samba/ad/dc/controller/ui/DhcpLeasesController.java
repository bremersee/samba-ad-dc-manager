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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bremersee.comparator.model.SortOrder;
import org.bremersee.comparator.spring.web.SortOrderRequestParam;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.shared.CurrentPageNameProvider;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.model.DhcpLeasePage;
import org.bremersee.samba.ad.dc.model.DnsZoneType;
import org.bremersee.samba.ad.dc.service.DnsService;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

/**
 * The dhcp leases controller.
 *
 * @author Christian Bremer
 */
@Controller
public class DhcpLeasesController extends UiController
    implements PageableComponent, CurrentPageNameProvider, RedirectComponent {

  private final DnsService dnsService;

  /**
   * Instantiates a new dhcp leases controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param dnsService the dns service
   */
  public DhcpLeasesController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DnsService dnsService) {
    super(properties, localeResolver, domainService);
    this.dnsService = dnsService;
  }

  @Override
  public String getDefaultSort() {
    return DHCP_LEASE_SORT;
  }

  @Override
  public String getCurrentPageName() {
    return "dhcp-leases";
  }

  /**
   * Display dhcp leases view.
   *
   * @param page the page
   * @param size the size
   * @param sort the sort
   * @param query the query
   * @param ipAddress the ip address
   * @param model the model
   * @return the view
   */
  @GetMapping(path = "/management/dhcp-leases")
  public String displayDhcpLeases(
      @RequestParam(name = PAGE, defaultValue = PAGE_DEFAULT) int page,
      @RequestParam(name = SIZE, defaultValue = SIZE_DEFAULT) int size,
      @SortOrderRequestParam(defaultSort = DHCP_LEASE_SORT) SortOrder sort,
      @RequestParam(name = QUERY, required = false) String query,
      @RequestParam(name = "ip", required = false) String ipAddress,
      ModelMap model) {

    return Optional.ofNullable(ipAddress)
        .filter(ip -> !ip.isEmpty())
        .flatMap(dnsService::findDnsEntry)
        .map(dnsEntry -> {
          Map<String, Object> parameters = new HashMap<>();
          parameters.put(PAGE, 0);
          parameters.put(SIZE, SIZE_DEFAULT_INT);
          parameters.put(SORT, DNS_ENTRY_SORT);
          parameters.put(QUERY, "");
          parameters.put(ZONE_TYPE, DnsZoneType.PRIMARY);
          parameters.put(ZONE_NAME, dnsEntry.getZoneName());
          parameters.put(DNS_ENTRY_NAME, dnsEntry.getName());
          parameters.put(DNS_ENTRY_TYPE, dnsEntry.getType());
          parameters.put(DNS_ENTRY_VALUE, dnsEntry.getValue());
          return getRedirectUri("dns-entry-edit", PAGE_AND_DNS_ENTRY_PARAMS, parameters);
        })
        .orElseGet(() -> {
          Pageable pageable = PageRequest.of(page, size, getSortMapper().toSort(sort));
          DhcpLeasePage dhcpLeasePage = new DhcpLeasePage(
              dnsService.getDhcpLeases(pageable, query));
          model.addAttribute("dhcpLeasePage", dhcpLeasePage);
          if (!isEmpty(ipAddress)) {
            String defaultMessage = String
                .format("No dns entry with ip address '%s' was found.", ipAddress);
            String message = getMessage(defaultMessage, "dhcp-leases.ip.not-found", ipAddress);
            model.addAttribute("rmsg",
                new RedirectMessage(message, RedirectMessageType.WARNING));
          }
          return "management/dhcp-leases";
        });
  }

}
