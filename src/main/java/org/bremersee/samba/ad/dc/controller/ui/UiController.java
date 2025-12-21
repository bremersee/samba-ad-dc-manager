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

import lombok.Getter;
import lombok.Setter;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.misc.DefaultDnTool;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.controller.AbstractController;
import org.bremersee.samba.ad.dc.controller.DnToolProvider;
import org.bremersee.samba.ad.dc.controller.SortOrderConstants;
import org.bremersee.samba.ad.dc.controller.ui.shared.LoggerProvider;
import org.bremersee.samba.ad.dc.controller.ui.shared.MessageProvider;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The type UiController.
 *
 * @author Christian Bremer
 */
@Getter
public abstract class UiController extends AbstractController implements LoggerProvider,
    DnToolProvider, MessageProvider, RedirectComponent, ErrorCode, SortOrderConstants {

  protected static final String COMPUTER = "computer";

  protected static final String COMPUTERS = "computers";

  private final Logger logger;

  private final DomainControllerProperties properties;

  private final DnTool dnTool;

  private final LocaleResolver localeResolver;

  @Setter
  private MessageSource messageSource;

  protected UiController(
      DomainControllerProperties properties,
      LocaleResolver localeResolver) {
    this.logger = LoggerFactory.getLogger(getClass());
    this.properties = properties;
    this.dnTool = new DefaultDnTool(properties);
    this.localeResolver = localeResolver;
  }

  @Deprecated
  protected String entityNotFoundRedirect(
      RedirectAttributes redirectAttributes,
      String entityType,
      String i18nCode,
      String entityName,
      String redirect) {

    return entityNotFoundRedirect(redirectAttributes, entityType, i18nCode, entityName,
        PAGE_AND_OU_PARAMS, redirect);
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
    String redirectUri = getRedirectUri(redirect, PAGE_AND_OU_PARAMS, getParamterMap());
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

}
