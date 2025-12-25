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

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.controller.ui.model.PasswordResetModel;
import org.bremersee.samba.ad.dc.controller.ui.model.PasswordResetRequestModel;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.DomainUserService;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

/**
 * The password reset controller.
 *
 * @author Christian Bremer
 */
@Controller
public class PasswordResetController extends UiController {

  private final DomainService domainService;

  private final DomainUserService domainUserService;

  public PasswordResetController(
      DomainControllerProperties domainControllerProperties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainUserService domainUserService) {
    super(domainControllerProperties, localeResolver);
    this.domainService = domainService;
    this.domainUserService = domainUserService;
  }

  @ModelAttribute("passwordPattern")
  public String getPasswordPattern() {
    return domainService.getPasswordInformation().getPasswordRegex();
  }

  @ModelAttribute("domain")
  public String getDomain() {
    return domainService.getDomainInfo().getDomain();
  }

  @ModelAttribute("netbiosDomain")
  public String getNetbiosDomain() {
    return domainService.getDomainInfo().getNetbiosDomain();
  }

  @GetMapping(path = "/passwd/password-reset-request")
  public String displayResetPasswordRequest(ModelMap model) {
    model.addAttribute("resetPasswordRequestModel", new PasswordResetRequestModel());
    return "passwd/password-reset-request";
  }

  @PostMapping(path = "/passwd/password-reset-request")
  public String processResetPasswordRequest(
      @ModelAttribute(name = "resetPasswordRequestModel")
      PasswordResetRequestModel resetPasswordRequestModel,
      ModelMap model,
      BindingResult bindingResult) {
    String username = resetPasswordRequestModel.getUsername();
    if (isEmpty(username)) {
      bindingResult.rejectValue("username", "todo", "Username must not be blank.");
      return "passwd/password-reset-request";
    }
    domainUserService.getUser(username, null, null)
        .filter(user -> !isEmpty(user.getEmail()))
        .ifPresent(user -> {
          getLogger().info("Password reset request has been sent.");
          // TODO process request
        });
    return "passwd/password-reset-request-response";
  }

  @GetMapping(path = "/passwd/password-reset")
  public String displayResetPassword(
      @RequestParam(value = "usernameEnc") String usernameEnc,
      @RequestParam(value = "requestDateTimeEnc") String requestDateTimeEnc,
      @RequestParam(value = "pwdLastSetEnc") String pwdLastSetEnc,
      @RequestParam(value = "", required = false) String isInvitationEnc,
      @RequestParam(value = "s") String salt,
      ModelMap model) {

    return getValidatedDomainUser(usernameEnc, requestDateTimeEnc, pwdLastSetEnc, salt)
        .map(user -> {
          model.addAttribute("usernameEncrypted", usernameEnc);
          model.addAttribute("requestDateTimeEncrypted", requestDateTimeEnc);
          model.addAttribute("pwdLastSetEncrypted", pwdLastSetEnc);
          model.addAttribute("salt", salt);
          model.addAttribute("user", user);
          model.addAttribute("passwordResetModel", new PasswordResetModel());
          return "passwd/password-reset";
        })
        .orElse("passwd/password-reset-invalid");
  }

  @PostMapping(path = "/passwd/password-reset")
  public String resetPassword(
      @RequestParam(value = "usernameEnc") String usernameEnc,
      @RequestParam(value = "requestDateTimeEnc") String requestDateTimeEnc,
      @RequestParam(value = "pwdLastSetEnc") String pwdLastSetEnc,
      @RequestParam(value = "s") String salt,
      @ModelAttribute(name = "passwordResetModel") PasswordResetModel passwordResetModel,
      ModelMap model,
      BindingResult bindingResult) {

    return getValidatedDomainUser(usernameEnc, requestDateTimeEnc, pwdLastSetEnc, salt)
        .map(user -> {
          String newPassword = requireNonNullElse(passwordResetModel.getNewPassword(), "");
          String newPasswordRepetition = passwordResetModel.getNewPasswordRepetition();
          if (!newPassword.equals(newPasswordRepetition)) {
            model.addAttribute("usernameEncrypted", usernameEnc);
            model.addAttribute("requestDateTimeEncrypted", requestDateTimeEnc);
            model.addAttribute("pwdLastSetEncrypted", pwdLastSetEnc);
            model.addAttribute("salt", salt);
            model.addAttribute("user", user);
            bindingResult.rejectValue("newPasswordRepetition", "todo", "Passwords must be equal.");
            return "passwd/password-reset";
          }
          domainUserService.updateUserPassword(user.getSamAccountName(), newPassword, false);
          model.clear();
          model.addAttribute("user", user);
          // TODO set login page, like data and whether to use netbios
          return "passwd/password-reset-success";
        })
        .orElse("passwd/password-reset-invalid");
  }

  private Optional<DomainUser> getValidatedDomainUser(
      String usernameEncrypted,
      String requestDateTimeEncrypted,
      String pwdLastSetEncrypted,
      String salt) {
    try {
      TextEncryptor textEncryptor = Encryptors.text("", salt);
      OffsetDateTime requestDateTime = OffsetDateTime.parse(textEncryptor
          .decrypt(requestDateTimeEncrypted), DateTimeFormatter.ISO_DATE_TIME);
      if (requestDateTime.plusDays(7L).isBefore(OffsetDateTime.now())) {
        getLogger().debug("Password reset request has expired.");
        return Optional.empty();
      }

      String username = textEncryptor.decrypt(usernameEncrypted);
      OffsetDateTime pwdLastSet = OffsetDateTime.parse(textEncryptor
          .decrypt(pwdLastSetEncrypted), DateTimeFormatter.ISO_DATE_TIME);
      return domainUserService.getUser(username, null, null)
          .filter(user -> pwdLastSet
              .isEqual(Objects
                  .requireNonNull(user.getPasswordLastSet(), "Password last set is null.")));

    } catch (RuntimeException e) {
      getLogger().error("Getting user to reset password failed.", e);
      return Optional.empty();
    }
  }

}
