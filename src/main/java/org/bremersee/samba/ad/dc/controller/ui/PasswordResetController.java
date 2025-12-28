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
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.config.DomainUserProperties.DefaultLoginPage;
import org.bremersee.samba.ad.dc.controller.ui.model.PasswordResetModel;
import org.bremersee.samba.ad.dc.controller.ui.model.PasswordResetRequestModel;
import org.bremersee.samba.ad.dc.model.AesEncValue;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.PasswordReset;
import org.bremersee.samba.ad.dc.model.event.PasswordResetEvent;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.DomainUserService;
import org.bremersee.samba.ad.dc.service.PasswordResetCryptoService;
import org.springframework.context.ApplicationEventPublisher;
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

  private final ApplicationEventPublisher eventPublisher;

  private final PasswordResetCryptoService<AesEncValue> passwordResetCryptoService;

  private final DomainService domainService;

  private final DomainUserService domainUserService;

  private final Pattern usernamePattern;

  private Pattern passwordPattern;

  public PasswordResetController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      ApplicationEventPublisher eventPublisher,
      PasswordResetCryptoService<AesEncValue> passwordResetCryptoService,
      DomainService domainService,
      DomainUserService domainUserService) {
    super(properties, localeResolver);
    this.eventPublisher = eventPublisher;
    this.passwordResetCryptoService = passwordResetCryptoService;
    this.domainService = domainService;
    this.domainUserService = domainUserService;
    this.usernamePattern = Pattern.compile(properties.getUser().getNewSamAccountNameRegex());
  }

  @ModelAttribute("usernamePattern")
  public String getUsernamePattern() {
    return usernamePattern.pattern();
  }

  @ModelAttribute("passwordPattern")
  public String getPasswordPattern() {
    if (isEmpty(passwordPattern)) {
      passwordPattern = Pattern.compile(domainService.getPasswordInformation().getPasswordRegex());
    }
    return passwordPattern.pattern();
  }

  @ModelAttribute("passwordDescription")
  public String getPasswordDescription() {
    return domainService.getPasswordInformation()
        .getPasswordDescription(getMessageSource(), getResolvedLocale());
  }

  @ModelAttribute("domain")
  public String getDomain() {
    return domainService.getDomainInfo().getDomain();
  }

  @ModelAttribute("netbiosDomain")
  public String getNetbiosDomain() {
    return domainService.getDomainInfo().getNetbiosDomain();
  }

  @ModelAttribute("defaultLoginPage")
  public DefaultLoginPage getDefaultLoginPage() {
    return getProperties().getUser().getDefaultLoginPage();
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
        .ifPresent(user -> eventPublisher.publishEvent(new PasswordResetEvent(user)));
    model.clear();
    return "redirect:password-reset-requested";
  }

  @GetMapping(path = "/passwd/password-reset-requested")
  public String displayResetPasswordRequested() {
    return "passwd/password-reset-request-response";
  }

  @GetMapping(path = "/passwd/password-reset")
  public String displayResetPassword(
      @RequestParam(value = "req") String passwordResetEnc,
      @RequestParam(value = "s") String salt,
      ModelMap model) {

    PasswordReset passwordReset = passwordResetCryptoService
        .decrypt(new AesEncValue(passwordResetEnc, salt));
    return getValidatedDomainUser(passwordReset)
        .map(user -> {
          model.addAttribute("req", passwordResetEnc);
          model.addAttribute("salt", salt);
          model.addAttribute("user", user);
          model.addAttribute("isInvitation", passwordReset.isInvitation());
          model.addAttribute("passwordResetModel", new PasswordResetModel(user));
          return "passwd/password-reset";
        })
        .orElse("passwd/password-reset-invalid");
  }

  @PostMapping(path = "/passwd/password-reset")
  public String resetPassword(
      @RequestParam(value = "req") String passwordResetEnc,
      @RequestParam(value = "s") String salt,
      @ModelAttribute(name = "passwordResetModel") PasswordResetModel passwordResetModel,
      ModelMap model,
      BindingResult bindingResult) {

    PasswordReset passwordReset = passwordResetCryptoService
        .decrypt(new AesEncValue(passwordResetEnc, salt));
    return getValidatedDomainUser(passwordReset)
        .map(user -> {
          if (isEmpty(passwordResetModel.getUsername())) {
            passwordResetModel.setUsername(user.getSamAccountName());
          }
          if (!usernamePattern.matcher(passwordResetModel.getUsername()).matches()) {
            bindingResult.rejectValue("username", "todo",
                "Username is not valid. Please try another username.");
          }
          String newPassword = requireNonNullElse(passwordResetModel.getNewPassword(), "");
          String newPasswordRepetition = passwordResetModel.getNewPasswordRepetition();
          if (!newPassword.equals(newPasswordRepetition)) {
            bindingResult.rejectValue("newPasswordRepetition", "todo", "Passwords must be equal.");
          } else if (!passwordPattern.matcher(newPassword).matches()) {
            bindingResult.rejectValue("newPassword", "todo",
                "Password is too weak. Please try a stronger password.");
          }
          DomainUser newUser = passwordReset.isInvitation()
              ? updateUser(user, passwordResetModel.getUsername(), bindingResult)
              : user;
          updatePassword(newUser, passwordResetModel.getNewPassword(), bindingResult);
          if (bindingResult.hasErrors()) {
            AesEncValue encValue = updateAesEncValue(
                user, newUser, new AesEncValue(passwordResetEnc, salt), passwordReset);
            model.addAttribute("req", encValue.encryptedValue());
            model.addAttribute("salt", encValue.salt());
            model.addAttribute("user", newUser);
            model.addAttribute("isInvitation", passwordReset.isInvitation());
            return "passwd/password-reset";
          }
          model.addAttribute("user", newUser);
          // TODO set login page, like data and whether to use netbios
          return "passwd/password-reset-success";
        })
        .orElse("passwd/password-reset-invalid");
  }

  private Optional<DomainUser> getValidatedDomainUser(PasswordReset passwordReset) {
    try {
      // TODO lifetime property
      if (passwordReset.getRequestDateTime().plusDays(7L).isBefore(OffsetDateTime.now())) {
        getLogger().debug("Password reset request has expired.");
        return Optional.empty();
      }
      return domainUserService.getUser(passwordReset.getUsername(), null, null)
          .filter(user -> Objects
              .equals(user.getPasswordLastSet(), passwordReset.getPwdLastSetDateTime()));

    } catch (RuntimeException e) {
      getLogger().error("Getting user to reset password failed.", e);
      return Optional.empty();
    }
  }

  private DomainUser updateUser(
      DomainUser user,
      String newUsername,
      BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      return user;
    }
    String oldUsername = user.getSamAccountName();
    if (oldUsername.equals(newUsername)) {
      return user;
    }
    DomainUser newUser = DomainUser.builder()
        .from(user)
        .samAccountName(newUsername)
        .uid(replace(user.getUid(), oldUsername, newUsername))
        .userPrincipalName(replace(user.getUserPrincipalName(), oldUsername, newUsername))
        .unixHomeDirectory(replace(user.getUnixHomeDirectory(), oldUsername, newUsername))
        .build();
    try {
      return domainUserService.updateUser(user.getSamAccountName(), newUser, null);

    } catch (ServiceException se) {
      handleException(bindingResult, se);
    }
    return user;
  }

  private String replace(String oldValue, String oldUsername, String newUsername) {
    if (isEmpty(oldValue)) {
      return oldValue;
    }
    return oldUsername.replace(oldUsername, newUsername);
  }

  private void updatePassword(DomainUser user, String newPassword, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return;
    }
    try {
      domainUserService.updateUserPassword(user.getSamAccountName(), newPassword);
    } catch (ServiceException se) {
      handleException(bindingResult, se);
    }
  }

  private void handleException(BindingResult bindingResult, ServiceException serviceException) {

    String errorCode = requireNonNullElse(serviceException.getErrorCode(), "");
    switch (errorCode) {
      case EC_SAM_ACCOUNT_NAME_REQUIRED: {
        bindingResult.rejectValue("username", "code",
            "Username is required.");
        break;
      }
      case EC_SAM_ACCOUNT_ALREADY_EXISTS, EC_PRINCIPAL_ALREADY_EXISTS, EC_UID_ALREADY_EXISTS: {
        bindingResult.rejectValue("username", "code",
            "Username already exists.");
        break;
      }
      case EC_ILLEGAL_SAM_ACCOUNT_NAME: {
        bindingResult.rejectValue("username", "code",
            "Username contains illegal characters.");
        break;
      }
      case EC_PASSWORD_RESTRICTIONS, EC_SAVING_PASSWORD_FAILED: {
        bindingResult.rejectValue("newPassword", "code",
            "Resetting password failed. Try another password.");
        break;
      }
      default: {
        getLogger()
            .error("Resetting password failed with a not mapped exception.", serviceException);
        throw serviceException;
      }
    }
  }

  private AesEncValue updateAesEncValue(
      DomainUser oldUser,
      DomainUser newUser,
      AesEncValue oldAesEncValue,
      PasswordReset passwordReset) {

    if (oldUser.getSamAccountName().equals(newUser.getSamAccountName())) {
      return oldAesEncValue;
    }
    return passwordResetCryptoService.encrypt(PasswordReset.builder()
        .from(passwordReset)
        .username(newUser.getSamAccountName())
        .build());
  }

}
