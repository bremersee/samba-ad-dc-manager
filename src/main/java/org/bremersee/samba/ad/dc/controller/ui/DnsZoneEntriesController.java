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

import org.bremersee.comparator.model.SortOrder;
import org.bremersee.comparator.spring.web.SortOrderRequestParam;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.shared.CurrentPageNameProvider;
import org.bremersee.samba.ad.dc.controller.ui.shared.DnsZoneTypeComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.model.DnsEntryPage;
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
 * The dns zone entries controller.
 *
 * @author Christian Bremer
 */
@Controller
public class DnsZoneEntriesController extends UiController
    implements CurrentPageNameProvider, PageableComponent, DnsZoneTypeComponent {

  private final DnsService dnsService;

  /**
   * Instantiates a new Dns zone entries controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param dnsService the dns service
   */
  public DnsZoneEntriesController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DnsService dnsService) {
    super(properties, localeResolver, domainService);
    this.dnsService = dnsService;
  }

  @Override
  public String getCurrentPageName() {
    return "dns-zone-entries";
  }

  @Override
  public String getDefaultSort() {
    return DNS_ENTRY_SORT;
  }

  /**
   * Display dns zone entries view.
   *
   * @param zoneName the zone name
   * @param page the page
   * @param size the size
   * @param sort the sort
   * @param query the query
   * @param model the model
   * @return the view
   */
  @GetMapping(path = "/management/dns-zone-entries")
  public String displayDnsZoneEntries(
      @RequestParam(name = ZONE_NAME) String zoneName,
      @RequestParam(name = PAGE, defaultValue = PAGE_DEFAULT) int page,
      @RequestParam(name = SIZE, defaultValue = SIZE_DEFAULT) int size,
      @SortOrderRequestParam(defaultSort = DNS_ENTRY_SORT) SortOrder sort,
      @RequestParam(name = QUERY, required = false) String query,
      ModelMap model) {

    Pageable pageable = PageRequest.of(page, size, getSortMapper().toSort(sort));
    DnsEntryPage dnsEntryPage = new DnsEntryPage(
        dnsService.getDnsEntries(zoneName, pageable, query));
    model.addAttribute("dnsEntryPage", dnsEntryPage);
    model.addAttribute("zoneName", zoneName);
    return "management/dns-zone-entries";
  }

}
