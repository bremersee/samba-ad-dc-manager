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

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.model.UserResetPasswordModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.OrganizationalUnitComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.PageableComponent;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.model.event.InvitationEvent;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.DomainUserService;
import org.ldaptive.dn.Dn;
import org.springframework.context.ApplicationEventPublisher;
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
 * The user reset password controller.
 *
 * @author Christian Bremer
 */
@Controller
public class UserResetPasswordController extends UiController
    implements PageableComponent, OrganizationalUnitComponent {

  private final DomainUserService domainUserService;

  private final ApplicationEventPublisher eventPublisher;

  public UserResetPasswordController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainUserService domainUserService,
      ApplicationEventPublisher eventPublisher) {
    super(properties, localeResolver, domainService);
    this.domainUserService = domainUserService;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public String getDefaultSort() {
    return USER_SORT;
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

  @GetMapping(path = "/management/user-reset-password")
  public String displayUserResetPassword(
      @RequestParam(value = "user", required = false) String userName,
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      ModelMap model,
      RedirectAttributes redirectAttributes) {

    return Optional.ofNullable(userName)
        .flatMap(name -> domainUserService.getUser(userName, ou, searchScope))
        .map(user -> {
          model.addAttribute("user", user);
          model.addAttribute("passwordRequest", new UserResetPasswordModel(user));
          return "management/user-reset-password";
        })
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "User", "todo", userName, PAGE_AND_OU_PARAMS, "users"));
  }

  @PostMapping(path = "/management/user-reset-password")
  public String resetPassword(
      @RequestParam(value = OU, required = false) Dn ou,
      @RequestParam(value = SCOPE, required = false) TreeSearchScope searchScope,
      @ModelAttribute("passwordRequest") UserResetPasswordModel passwordRequest,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    getLogger().debug("resetPassword({})", passwordRequest);
    return Optional.ofNullable(passwordRequest.getSamAccountName())
        .flatMap(name -> domainUserService.getUser(name, ou, searchScope))
        .map(user -> resetPassword(
            user, passwordRequest, model, bindingResult, redirectAttributes))
        .orElseGet(() -> entityNotFoundRedirect(
            redirectAttributes, "User", "todo", passwordRequest.getSamAccountName(),
            PAGE_AND_OU_PARAMS, "users"));
  }

  private String resetPassword(
      DomainUser user,
      UserResetPasswordModel passwordRequest,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    String password;
    if (passwordRequest.isGenerateRandomPassword()) {
      password = getDomainService().createRandomPassword();
    } else if (!isEmpty(passwordRequest.getPassword())) {
      password = passwordRequest.getPassword();
      if (!Pattern.compile(getPasswordPattern()).matcher(password).matches()) {
        String defaultMsg = "Password doesn't match the required pattern.";
        bindingResult.rejectValue("password", "todo", defaultMsg);
        model.addAttribute("user", user);
        return "management/user-reset-password";
      }
    } else {
      String defaultMsg = "Resetting password failed. Password is required.";
      bindingResult.rejectValue("password", "todo", defaultMsg);
      model.addAttribute("user", user);
      return "management/user-reset-password";
    }
    domainUserService.updateUserPassword(user.getSamAccountName(), password);
    if (passwordRequest.isGenerateRandomPassword()) {
      domainUserService.getUser(user.getDistinguishedName(), null, null)
          .ifPresent(freshUser -> eventPublisher
              .publishEvent(new InvitationEvent(freshUser, getBaseUri())));
    }

    String defaultMsg = "Password was successfully changed.";
    RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS, defaultMsg, "todo");
    redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);
    Map<String, Object> parameters = getParamterMap();
    String redirect = getRedirectUri("user-reset-password?user={{userName}}",
        PAGE_AND_OU_PARAMS, putToParameterMap(parameters, "userName", user.getSamAccountName()));
    logRedirectTo(defaultMsg, redirect);
    return redirect;
  }

}
