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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.AbstractController;
import org.bremersee.samba.ad.dc.controller.DnToolProvider;
import org.bremersee.samba.ad.dc.controller.ui.shared.LoggerProvider;
import org.bremersee.samba.ad.dc.controller.ui.shared.MessageProvider;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.misc.DefaultDnTool;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The base ui controller.
 *
 * @author Christian Bremer
 */
@Getter
public abstract class UiController extends AbstractController implements LoggerProvider,
    DnToolProvider, MessageProvider, RedirectComponent, ErrorCode {

  protected static final String COMPUTER = "computer";

  protected static final String COMPUTERS = "computers";

  protected static final String GROUP = "group";

  protected static final String GROUPS = "groups";

  protected static final String SAM_ACCOUNT_NAME = "samAccountName";

  private final Logger logger;

  private final ApplicationProperties properties;

  private final DnTool dnTool;

  private final LocaleResolver localeResolver;

  @Getter(AccessLevel.PROTECTED)
  private final DomainService domainService;

  @Setter
  private MessageSource messageSource;

  protected UiController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService) {
    this.logger = LoggerFactory.getLogger(getClass());
    this.properties = properties;
    this.dnTool = new DefaultDnTool(properties);
    this.localeResolver = localeResolver;
    this.domainService = domainService;
  }

  @ModelAttribute("properties")
  @Override
  public ApplicationProperties getProperties() {
    return properties;
  }

  @ModelAttribute("domainInfo")
  public DomainInfo getDomainInfo() {
    return getDomainService().getDomainInfo();
  }

  @ModelAttribute("companyUrl")
  public String getCompanyUrl() {
    String companyUrl = getProperties().getCompanyUrl();
    if (isEmpty(companyUrl) || "#".equals(companyUrl)) {
      return getBaseUri();
    }
    return companyUrl;
  }

  protected String getBaseUri() {
    return getBaseUri(getProperties().getEmail().getBaseUri());
  }

  protected String entityNotFoundRedirect(
      RedirectAttributes redirectAttributes,
      String entityType,
      String i18nCode,
      String entityName,
      String parameterTemplate,
      String redirect) {

    String msg = String.format("%s '%s' was not found.", entityType, entityName);
    RedirectMessage redirectMessage = getRedirectMessage(RedirectMessageType.WARNING, msg,
        i18nCode, String.valueOf(entityName));
    redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, redirectMessage);
    String redirectUri = getRedirectUri(redirect, parameterTemplate, getParamterMap());
    logRedirectTo(msg, redirectUri);
    return redirectUri;
  }

  protected void logRedirectTo(String msg, String redirect) {
    if (isEmpty(msg)) {
      getLogger().debug("Redirecting to {}", redirect);
    } else {
      getLogger().debug("{} Redirecting to {}", msg, redirect);
    }
  }

  /*
  @ExceptionHandler(Exception.class)
  public ModelAndView handleError(HttpServletRequest req, Exception ex) {
    getLogger().error("Request {} raised exception", req.getRequestURL(), ex);
    // send email? with hostname!
    ModelAndView mav = new ModelAndView();
    mav.addObject("exception", ex);
    mav.addObject("url", req.getRequestURL());
    mav.setViewName("error");
    return mav;
  }
  */

}
