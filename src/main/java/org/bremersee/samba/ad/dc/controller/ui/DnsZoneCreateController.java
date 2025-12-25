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

import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.service.DnsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The dns zone create controller.
 *
 * @author Christian Bremer
 */
@Controller
@Slf4j
public class DnsZoneCreateController extends UiController {

  private final DnsService dnsService;

  public DnsZoneCreateController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DnsService dnsService) {
    super(properties, localeResolver);
    this.dnsService = dnsService;
  }

  @GetMapping(path = "/management/dns-zone-create")
  public String displayCreateZone() {
    return "management/dns-zone-create";
  }

  @PostMapping(path = "/management/dns-zone-create")
  public String createZone(
      @RequestParam(name = "name") String name,
      RedirectAttributes redirectAttributes) {

    try {
      DnsZone zone = dnsService.createDnsZone(name);
      String msg = String.format("Dns zone '%s' was successfully created.",
          zone.getName());
      RedirectMessage redirectMessage = getRedirectMessage(RedirectMessageType.SUCCESS, msg,
          "todo", zone.getName());
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, redirectMessage);
      return "redirect:dns-zones";

    } catch (ServiceException e) {

      String msg = String.format("Creation of dns zone '%s' failed.", name);
      log.error(msg, e);
      RedirectMessage redirectMessage = getRedirectMessage(RedirectMessageType.WARNING, msg,
          "todo", name);
      redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, redirectMessage);
      return "redirect:dns-zones";
    }
  }

}
