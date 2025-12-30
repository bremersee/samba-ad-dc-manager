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

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.LocaleResolver;

/**
 * The login controller.
 *
 * @author Christian Bremer
 */
@Controller("loginController")
public class LoginController extends UiController {

  /**
   * Instantiates a new login controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   */
  public LoginController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService) {
    super(properties, localeResolver, domainService);
  }

  /**
   * Determines whether login failed or not.
   *
   * @param request the http request
   * @return {@code true} if login failed, otherwise {@code false}
   */
  @ModelAttribute("error")
  public boolean hasError(HttpServletRequest request) {
    Enumeration<String> params = request.getParameterNames();
    while (params.hasMoreElements()) {
      String name = params.nextElement();
      if ("error".equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Display login view.
   *
   * @return the view template
   */
  @GetMapping(path = "/login")
  public String displayLoginView() {
    return "login";
  }

}
