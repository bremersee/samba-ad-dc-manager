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

import java.time.Duration;
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
import org.bremersee.samba.ad.dc.service.CryptoService;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.DomainUserService;
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

  private static final String USERNAME = "username";

  private static final String HTML_TEMPLATE_INVALID = "passwd/password-reset-invalid";

  private final DomainUserService domainUserService;

  private final CryptoService<PasswordReset, AesEncValue> cryptoService;

  private final ApplicationEventPublisher eventPublisher;

  private final Pattern usernamePattern;

  private Pattern passwordPattern;

  public PasswordResetController(
      ApplicationProperties properties,
      LocaleResolver localeResolver,
      DomainService domainService,
      DomainUserService domainUserService,
      CryptoService<PasswordReset, AesEncValue> cryptoService,
      ApplicationEventPublisher eventPublisher) {
    super(properties, localeResolver, domainService);
    this.domainUserService = domainUserService;
    this.cryptoService = cryptoService;
    this.eventPublisher = eventPublisher;
    this.usernamePattern = Pattern.compile(properties.getUser().getNewSamAccountNameRegex());
  }

  @ModelAttribute("usernamePattern")
  public String getUsernamePattern() {
    return usernamePattern.pattern();
  }

  @ModelAttribute("passwordPattern")
  public String getPasswordPattern() {
    if (isEmpty(passwordPattern)) {
      passwordPattern = Pattern
          .compile(getDomainService().getPasswordInformation().getPasswordRegex());
    }
    return passwordPattern.pattern();
  }

  @ModelAttribute("passwordDescription")
  public String getPasswordDescription() {
    return getDomainService().getPasswordInformation()
        .getPasswordDescription(getMessageSource(), getResolvedLocale());
  }

  @ModelAttribute("defaultLoginPage")
  public DefaultLoginPage getDefaultLoginPage() {
    DefaultLoginPage defaultLoginPage = getProperties().getUser().getDefaultLoginPage();
    if (!isEmpty(defaultLoginPage.getUrl())) {
      return defaultLoginPage;
    }
    DefaultLoginPage fallback = new DefaultLoginPage();
    fallback.setUrl(getBaseUri() + "/login");
    fallback.setName("LOGIN");
    fallback.setUsingNetbiosDomainPrefix(false);
    return fallback;
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
      bindingResult.rejectValue(USERNAME, "todo", "Username must not be blank.");
      return "passwd/password-reset-request";
    }
    domainUserService.getUser(username, null, null)
        .ifPresent(user -> eventPublisher
            .publishEvent(new PasswordResetEvent(user, getBaseUri())));
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

    PasswordReset passwordReset;
    try {
      passwordReset = cryptoService.decrypt(new AesEncValue(passwordResetEnc, salt));
    } catch (RuntimeException e) {
      return HTML_TEMPLATE_INVALID;
    }
    return getValidatedDomainUser(passwordReset)
        .map(user -> {
          model.addAttribute("req", passwordResetEnc);
          model.addAttribute("salt", salt);
          model.addAttribute("user", user);
          model.addAttribute("isInvitation", passwordReset.isInvitation());
          model.addAttribute("passwordResetModel", new PasswordResetModel(user));
          return "passwd/password-reset";
        })
        .orElse(HTML_TEMPLATE_INVALID);
  }

  @PostMapping(path = "/passwd/password-reset")
  public String resetPassword(
      @RequestParam(value = "req") String passwordResetEnc,
      @RequestParam(value = "s") String salt,
      @ModelAttribute(name = "passwordResetModel") PasswordResetModel passwordResetModel,
      ModelMap model,
      BindingResult bindingResult) {

    PasswordReset passwordReset;
    try {
      passwordReset = cryptoService.decrypt(new AesEncValue(passwordResetEnc, salt));
    } catch (RuntimeException e) {
      return HTML_TEMPLATE_INVALID;
    }
    return getValidatedDomainUser(passwordReset)
        .map(user -> {
          if (isEmpty(passwordResetModel.getUsername())) {
            passwordResetModel.setUsername(user.getSamAccountName());
          }
          if (!usernamePattern.matcher(passwordResetModel.getUsername()).matches()) {
            bindingResult.rejectValue(
                USERNAME,
                "controller.ui.password-reset-c.username.pattern",
                "The username is not valid. Please try another one.");
          }
          String newPassword = requireNonNullElse(passwordResetModel.getNewPassword(), "");
          String newPasswordRepetition = passwordResetModel.getNewPasswordRepetition();
          if (!newPassword.equals(newPasswordRepetition)) {
            bindingResult.rejectValue(
                "newPasswordRepetition",
                "controller.ui.password-reset-c.passwords-not-equal",
                "Passwords must be equal.");
          } else if (!passwordPattern.matcher(newPassword).matches()) {
            bindingResult.rejectValue(
                "newPassword",
                "ec.password-restrictions",
                "The password is too weak. Please try a stronger one.");
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
          model.addAttribute(
              "netbiosUsername",
              getDomainInfo().getNetbiosDomain() + '\\' + newUser.getSamAccountName());
          return "passwd/password-reset-success";
        })
        .orElse(HTML_TEMPLATE_INVALID);
  }

  private Optional<DomainUser> getValidatedDomainUser(PasswordReset passwordReset) {
    Duration requestLifetime;
    if (passwordReset.isInvitation()) {
      requestLifetime = getProperties().getUser().getInvitationLifetime();
    } else {
      requestLifetime = getProperties().getUser().getPasswordResetRequestLifetime();
    }
    OffsetDateTime until = passwordReset.getRequestDateTime().plus(requestLifetime);
    OffsetDateTime now = OffsetDateTime.now();
    if (until.isBefore(now)) {
      getLogger().debug("Password reset request has expired.");
      return Optional.empty();
    }
    return domainUserService.getUser(passwordReset.getUsername(), null, null)
        .filter(user -> Objects
            .equals(user.getPasswordLastSet(), passwordReset.getPwdLastSetDateTime()));
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
        bindingResult.rejectValue(
            USERNAME,
            "ec.sam-account-name.required",
            "A username is required.");
        break;
      }
      case EC_SAM_ACCOUNT_ALREADY_EXISTS, EC_PRINCIPAL_ALREADY_EXISTS, EC_UID_ALREADY_EXISTS: {
        bindingResult.rejectValue(
            USERNAME,
            "ec.sam-account-name.already-exists",
            "The username already exists. Please try another one.");
        break;
      }
      case EC_ILLEGAL_SAM_ACCOUNT_NAME: {
        bindingResult.rejectValue(
            USERNAME,
            "ec.sam-account-name.illegal",
            "The username contains illegal characters.");
        break;
      }
      case EC_PASSWORD_RESTRICTIONS, EC_SAVING_PASSWORD_FAILED: {
        bindingResult.rejectValue(
            "newPassword",
            "ec.password-restrictions",
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
    return cryptoService.encrypt(PasswordReset.builder()
        .from(passwordReset)
        .username(newUser.getSamAccountName())
        .build());
  }

}
