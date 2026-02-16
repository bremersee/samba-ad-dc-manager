/*
 * Copyright 2019-2026 the original author or authors.
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

package org.bremersee.samba.ad.dc.service;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.TemplateEngineContextSupplier;
import org.bremersee.samba.ad.dc.model.AesEncValue;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.PasswordReset;
import org.bremersee.samba.ad.dc.model.event.InvitationEvent;
import org.bremersee.samba.ad.dc.model.event.PasswordResetEvent;
import org.bremersee.samba.ad.dc.model.event.PasswordResetSuccessEvent;
import org.bremersee.samba.ad.dc.repository.DomainRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * The password email service.
 *
 * @author Christian Bremer
 */
@Service("passwordEmailService")
@ConditionalOnProperty(name = "spring.mail.host")
@Slf4j
public class PasswordEmailService extends AbstractEmailService {

  private final CryptoService<PasswordReset, AesEncValue> passwordResetCryptoService;

  /**
   * Instantiates a new password email service.
   *
   * @param properties the properties
   * @param messageSource the message source
   * @param templateEngine the template engine
   * @param contextSuppliers the context suppliers
   * @param javaMailSender the java mail sender
   * @param passwordResetCryptoService the password reset crypto service
   */
  public PasswordEmailService(
      ApplicationProperties properties,
      MessageSource messageSource,
      TemplateEngine templateEngine,
      List<TemplateEngineContextSupplier> contextSuppliers,
      JavaMailSender javaMailSender,
      CryptoService<PasswordReset, AesEncValue> passwordResetCryptoService) {
    super(properties, messageSource, templateEngine, contextSuppliers, javaMailSender);
    this.passwordResetCryptoService = passwordResetCryptoService;
  }

  /**
   * On password reset event.
   *
   * @param passwordResetEvent the password reset event
   */
  @EventListener
  @Async
  public void onPasswordResetEvent(PasswordResetEvent passwordResetEvent) {
    sendResetEmail(passwordResetEvent.getSource(), passwordResetEvent.getBaseUri(), false);
  }

  /**
   * On invitation event.
   *
   * @param event the event
   */
  @EventListener
  @Async
  public void onInvitationEvent(InvitationEvent event) {
    sendResetEmail(event.getSource(), event.getBaseUri(), true);
  }

  /**
   * On password reset success event.
   *
   * @param event the event
   */
  @EventListener
  @Async
  public void onPasswordResetSuccessEvent(PasswordResetSuccessEvent event) {
    sendSuccessEmail(event.getSource(), event.getBaseUri());
  }

  private void sendResetEmail(DomainUser user, String baseUri, boolean isInvitation) {
    if (isEmpty(user) || isEmpty(user.getEmail()) || isEmpty(baseUri)) {
      return;
    }
    MimeMessagePreparator preparator = mimeMessage -> {
      MimeMessageHelper helper = new MimeMessageHelper(
          mimeMessage, true, StandardCharsets.UTF_8.name());
      helper.setFrom(getProperties().getEmail().getSender());
      helper.setTo(Objects.requireNonNull(user.getEmail()));
      helper.setSubject(getResetSubject(user, isInvitation));
      helper.setText(getResetText(user, baseUri, isInvitation), true);
    };
    getJavaMailSender().send(preparator);
  }

  private String getResetSubject(DomainUser user, boolean isInvitation) {
    if (isInvitation) {
      return getMessageSource().getMessage(
          "email.invitation-email.subject",
          new Object[]{getEmailRegards()},
          "Invitation",
          user.getLocale());
    } else {
      return getMessageSource().getMessage(
          "email.password-reset-email.subject",
          null,
          "Your password reset request",
          user.getLocale());
    }
  }

  private String getResetText(DomainUser user, String baseUri, boolean isInvitation) {
    Context ctx = createContext(user, baseUri);
    ctx.setVariable("resetUri", getResetUri(user, baseUri, isInvitation));
    long lifetimeDays;
    if (isInvitation) {
      lifetimeDays = getProperties().getUser().getInvitationLifetime().toDays();
    } else {
      lifetimeDays = getProperties().getUser().getPasswordResetRequestLifetime().toDays();
    }
    ctx.setVariable("lifetimeDays", lifetimeDays);
    String template = isInvitation
        ? "email/invitation-email"
        : "email/password-reset-email";
    return getTemplateEngine().process(template, ctx);
  }

  private String getResetUri(DomainUser user, String baseUri, boolean isInvitation) {
    AesEncValue encValue = passwordResetCryptoService.encrypt(PasswordReset.builder()
        .username(user.getSamAccountName())
        .pwdLastSetDateTime(user.getPasswordLastSet())
        .invitation(isInvitation)
        .build());
    return new DefaultUriBuilderFactory(baseUri)
        .builder()
        .path("/passwd/password-reset")
        .queryParam("req", encValue.encryptedValue())
        .queryParam("s", encValue.salt())
        .build()
        .toString();
  }

  private void sendSuccessEmail(DomainUser user, String baseUri) {
    if (isEmpty(user) || isEmpty(user.getEmail()) || isEmpty(baseUri)) {
      return;
    }
    MimeMessagePreparator preparator = mimeMessage -> {
      MimeMessageHelper helper = new MimeMessageHelper(
          mimeMessage, true, StandardCharsets.UTF_8.name());
      helper.setFrom(getProperties().getEmail().getSender());
      helper.setTo(Objects.requireNonNull(user.getEmail()));
      helper.setSubject(getSuccessSubject(user));
      helper.setText(getSuccessText(user, baseUri), true);
    };
    getJavaMailSender().send(preparator);
  }

  private String getSuccessSubject(DomainUser user) {
    return getMessageSource().getMessage(
        "email.password-reset-success-email.subject",
        null,
        "You reset your password successfully",
        user.getLocale());
  }

  private String getSuccessText(DomainUser user, String baseUri) {
    Context ctx = createContext(user, baseUri);
    ctx.setVariable("netbiosUsername", getNetbiosUsername(user, ctx));
    String template = "email/password-reset-success-email";
    return getTemplateEngine().process(template, ctx);
  }

  private String getNetbiosUsername(DomainUser user, Context context) {
    return getDomainRepository(context).getDomainInfo().getNetbiosDomain() + '\\'
        + user.getSamAccountName();
  }

  private DomainRepository getDomainRepository(Context context) {
    return (DomainRepository) context.getVariable("domain");
  }

}
