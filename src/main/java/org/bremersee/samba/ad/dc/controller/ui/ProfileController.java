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
import java.util.Optional;
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

  private final DomainUserService domainUserService;

  private final CryptoService<EmailChange, AesEncValue> cryptoService;

  private final ApplicationEventPublisher eventPublisher;

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

  @GetMapping(path = "/user/profile")
  public String displayProfile(ModelMap model) {
    DomainUser user = getCurrentUser();
    model.addAttribute("user", user);
    model.addAttribute("editModel", new ProfileEditModel(user));
    return "user/profile";
  }

  @GetMapping(path = "/user/profile", params = {"req", "s"})
  public String displayProfile(
      @RequestParam(value = "req") String emailChangeEnc,
      @RequestParam(value = "s") String salt,
      ModelMap model) {

    DomainUser user = getCurrentUser();
    RedirectMessage rmsg = decryptAndValidate(user, emailChangeEnc, salt)
        .map(emailChange -> {
          DomainUser newUser = domainUserService.updateUser(
              user.getSamAccountName(),
              DomainUser.builder()
                  .from(user)
                  .email(emailChange.getNewEmail())
                  .build(),
              null);
          model.addAttribute("user", newUser);
          model.addAttribute("editModel", new ProfileEditModel(newUser));
          String defaultMsg = "Your new email address was successfully saved.";
          return getRedirectMessage(RedirectMessageType.SUCCESS, defaultMsg, "todo");
        })
        .orElseGet(() -> {
          model.addAttribute("user", user);
          model.addAttribute("editModel", new ProfileEditModel(user));
          String defaultMsg = "Your email change request is invalid or has timed out.";
          return getRedirectMessage(RedirectMessageType.WARNING, defaultMsg, "todo");
        });
    model.addAttribute("rmsg", rmsg);
    return "user/profile";
  }

  @PostMapping(path = "/user/profile")
  public String processEmailChangeRequest(
      @ModelAttribute(name = "editModel") ProfileEditModel editModel,
      ModelMap model,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    DomainUser user = getCurrentUser();
    if (isEmpty(editModel.getNewEmail())
        || editModel.getNewEmail().equalsIgnoreCase(user.getEmail())) {
      model.clear();
      return "redirect:profile";
    }

    eventPublisher.publishEvent(new EmailChangeEvent(user, editModel.getNewEmail(), getBaseUri()));
    model.clear();
    String defaultMsg = String
        .format("The verification mail was sent to %s.", editModel.getNewEmail());
    RedirectMessage rmsg = getRedirectMessage(RedirectMessageType.SUCCESS, defaultMsg,
        "todo", editModel.getNewEmail());
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
    OffsetDateTime now = OffsetDateTime.now();
    if (until.isBefore(now)) {
      return Optional.empty();
    }
    return Optional.of(change);
  }

}
