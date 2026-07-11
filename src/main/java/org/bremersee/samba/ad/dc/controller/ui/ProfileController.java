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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.regex.Pattern;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.controller.ui.model.ProfileEditModel;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessage;
import org.bremersee.samba.ad.dc.controller.ui.shared.RedirectMessageType;
import org.bremersee.samba.ad.dc.model.AesEncValue;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.EmailChange;
import org.bremersee.samba.ad.dc.model.event.EmailChangeEvent;
import org.bremersee.samba.ad.dc.service.CryptoService;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.DomainUserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
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
 * The profile controller.
 *
 * @author Christian Bremer
 */
@Controller
public class ProfileController extends UiController {

  private static final String USER = "user";

  private static final String EDIT_MODEL = "editModel";

  private static final String USER_SLASH_PROFILE = "user/profile";

  private final DomainUserService domainUserService;

  private final CryptoService<EmailChange, AesEncValue> cryptoService;

  private final ApplicationEventPublisher eventPublisher;

  /**
   * Instantiates a new profile controller.
   *
   * @param properties the properties
   * @param localeResolver the locale resolver
   * @param domainService the domain service
   * @param domainUserService the domain user service
   * @param cryptoService the crypto service
   * @param eventPublisher the event publisher
   */
  public ProfileController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainUserService domainUserService,
      CryptoService<EmailChange, AesEncValue> cryptoService,
      ApplicationEventPublisher eventPublisher) {
    super(properties, localeResolver, domainService);
    this.domainUserService = domainUserService;
    this.cryptoService = cryptoService;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Is user able to change email.
   *
   * @return the boolean
   */
  @ModelAttribute("userAbleToChangeEmail")
  public boolean isUserAbleToChangeEmail() {
    return getProperties().getUser().isUserAbleToChangeEmail();
  }

  /**
   * Display profile.
   *
   * @param model the model
   * @return the string
   */
  @GetMapping(path = "/user/profile")
  public String displayProfile(ModelMap model) {
    DomainUser user = getCurrentUser();
    model.addAttribute(USER, user);
    model.addAttribute(EDIT_MODEL, new ProfileEditModel(user));
    return USER_SLASH_PROFILE;
  }

  /**
   * Display profile.
   *
   * @param emailChangeEnc the email change enc
   * @param salt the salt
   * @param model the model
   * @return the string
   */
  @GetMapping(path = "/user/profile", params = {"req", "s"})
  public String displayProfile(
      @RequestParam(value = "req") String emailChangeEnc,
      @RequestParam(value = "s") String salt,
      ModelMap model) {

    DomainUser domainUser = getCurrentUser();
    RedirectMessage rmsg = decryptAndValidate(domainUser, emailChangeEnc, salt)
        .map(emailChange -> {
          DomainUser newDomainUser = domainUserService
              .updateUser(
                  domainUser.getSamAccountName(),
                  domainUser.withEmail(emailChange.getNewEmail()),
              null);
          model.addAttribute(USER, newDomainUser);
          model.addAttribute(EDIT_MODEL, new ProfileEditModel(newDomainUser));
          String defaultMsg = "Your new email address was successfully changed.";
          return getRedirectMessage(RedirectMessageType.SUCCESS, defaultMsg,
              "user-profile.change-email.success");
        })
        .orElseGet(() -> {
          model.addAttribute(USER, domainUser);
          model.addAttribute(EDIT_MODEL, new ProfileEditModel(domainUser));
          String defaultMsg = "Your request to change your email address is invalid.";
          return getRedirectMessage(RedirectMessageType.WARNING, defaultMsg,
              "user-profile.change-email.invalid");
        });
    model.addAttribute("rmsg", rmsg);
    return USER_SLASH_PROFILE;
  }

  /**
   * Process email change request.
   *
   * @param editModel the edit model
   * @param model the model
   * @param bindingResult the binding result
   * @param redirectAttributes the redirect attributes
   * @return the string
   */
  @PostMapping(path = "/user/profile")
  public String processEmailChangeRequest(
      @ModelAttribute(name = EDIT_MODEL) ProfileEditModel editModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    DomainUser domainUser = getCurrentUser();
    if (isEmpty(editModel.getNewEmail())
        || editModel.getNewEmail().equalsIgnoreCase(domainUser.getEmail())
        || !isUserAbleToChangeEmail()) {
      model.clear();
      return "redirect:profile";
    }

    Pattern emailPattern = getProperties().getEmail().getEmailRegexFlags()
        .compile(getProperties().getEmail().getEmailRegex());
    if (!emailPattern.matcher(editModel.getNewEmail()).matches()) {
      bindingResult.rejectValue(
          "newEmail",
          "user-profile.email.invalid",
          null,
          "The email address is not accepted.");
      model.addAttribute(USER, domainUser);
      return USER_SLASH_PROFILE;
    }

    eventPublisher
        .publishEvent(new EmailChangeEvent(domainUser, editModel.getNewEmail(), getBaseUri()));
    model.clear();
    String defaultMsg = String
        .format("The confirmation mail was sent to %s.", editModel.getNewEmail());
    RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS, defaultMsg,
        "user-profile.change-email.confirmation-sent", editModel.getNewEmail());
    redirectAttributes.addFlashAttribute(RedirectMessage.ATTRIBUTE_NAME, rmsg);
    return "redirect:profile";
  }

  private DomainUser getCurrentUser() {
    return Optional.ofNullable(SecurityContextHolder.getContext())
        .map(SecurityContext::getAuthentication)
        .map(Authentication::getName)
        .flatMap(username -> domainUserService.getUser(username, null, null))
        .orElseThrow(ServiceException::notFound);
  }

  private Optional<EmailChange> decryptAndValidate(DomainUser user, String env, String salt) {
    EmailChange change;
    try {
      change = cryptoService.decrypt(new AesEncValue(env, salt));
    } catch (RuntimeException e) {
      getLogger().error("Decrypting email change failed.", e);
      return Optional.empty();
    }
    if (!change.getUsername().equalsIgnoreCase(user.getSamAccountName())) {
      return Optional.empty();
    }
    if (!change.getOldEmail().equalsIgnoreCase(user.getEmail())) {
      return Optional.empty();
    }
    OffsetDateTime until = change.getRequestDateTime()
        .plus(getProperties().getUser().getChangeEmailRequestLifetime());
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    if (until.isBefore(now)) {
      return Optional.empty();
    }
    return Optional.of(change);
  }

}
