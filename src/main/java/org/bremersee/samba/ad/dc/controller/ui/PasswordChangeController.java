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

import static java.util.Objects.requireNonNullElse;
import static org.springframework.util.ObjectUtils.isEmpty;

import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.model.PasswordChangeModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.DomainUserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The password change controller.
 *
 * @author Christian Bremer
 */
@Controller
public class PasswordChangeController extends UiController {

  private static final String HTML_TEMPLATE = "passwd/password-change";

  private final DomainUserService domainUserService;

  public PasswordChangeController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainUserService domainUserService) {
    super(properties, localeResolver, domainService);
    this.domainUserService = domainUserService;
  }

  @ModelAttribute("passwordPattern")
  public String getPasswordPattern() {
    return getDomainService().getPasswordInformation().getPasswordRegex();
  }

  @ModelAttribute("passwordDescription")
  public String getPasswordDescription() {
    return getDomainService().getPasswordInformation()
        .getPasswordDescription(getMessageSource(), getResolvedLocale());
  }

  @GetMapping(path = {"/passwd", "/passwd/"})
  public String displayChangePassword() {
    return "redirect:/passwd/password-change";
  }

  @GetMapping(path = "/passwd/password-change")
  public String displayChangePassword(
      @RequestParam(value = "username", required = false) String username,
      ModelMap model) {

    PasswordChangeModel changePasswordModel = new PasswordChangeModel();
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (isFullyAuthenticated()) {
      changePasswordModel.setUsername(authentication.getName());
    } else if (!isEmpty(username)) {
      changePasswordModel.setUsername(username);
    }
    model.addAttribute("changePasswordModel", changePasswordModel);
    return HTML_TEMPLATE;
  }

  @PostMapping(path = "/passwd/password-change")
  public String changePassword(
      @ModelAttribute(name = "changePasswordModel") PasswordChangeModel changePasswordModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    String netbiosPrefix = (getDomainInfo().getNetbiosDomain() + "\\").toLowerCase();
    String username = changePasswordModel.getUsername();
    if (!isEmpty(username) && username.toLowerCase().startsWith(netbiosPrefix)) {
      username = username.substring(netbiosPrefix.length());
    }
    if (isEmpty(username)) {
      bindingResult.rejectValue(
          "username",
          "controller.ui.password-change-c.username.required",
          "Username is required.");
    }
    String oldPassword = changePasswordModel.getOldPassword();
    String newPassword = requireNonNullElse(changePasswordModel.getNewPassword(), "");
    String newPasswordRepetition = changePasswordModel.getNewPasswordRepetition();
    if (!newPassword.equals(newPasswordRepetition)) {
      bindingResult.rejectValue(
          "newPasswordRepetition",
          "controller.ui.password-change-c.passwords-not-equal",
          "Passwords must be equal.");
    }
    if (bindingResult.hasErrors()) {
      return HTML_TEMPLATE;
    }
    try {
      domainUserService.updateUserPassword(username, oldPassword, newPassword);

    } catch (AuthenticationException ae) {
      bindingResult.rejectValue(
          "oldPassword",
          "controller.ui.password-change-c.authentication-failed",
          "Authentication failed.");
      return HTML_TEMPLATE;

    } catch (ServiceException se) {
      if (EC_PASSWORD_RESTRICTIONS.equals(se.getErrorCode())) {
        bindingResult.rejectValue(
            "newPassword",
            "ec.password-restrictions",
            "Password doesn't match the required pattern.");
        return HTML_TEMPLATE;
      }
      throw se;
    }

    model.clear();
    RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS,
        "Your password was successfully changed.",
        "controller.ui.password-change-c.password-changed");
    redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);
    return "redirect:password-change";
  }

  private boolean isFullyAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return !isEmpty(authentication)
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }

}
