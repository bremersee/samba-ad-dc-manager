package org.bremersee.samba.ad.dc.controller.ui;

import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public abstract class AbstractEditController extends UiController
    implements RedirectComponent {
  // TODO move methods into UiController

  protected AbstractEditController(
      DomainControllerProperties properties,
      LocaleResolver localeResolver) {
    super(properties, localeResolver);
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
}
