/*
 * Copyright 2019-2020 the original author or authors.
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
import java.util.Locale;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.TemplateEngineContextSupplier;
import org.bremersee.samba.ad.dc.model.AesEncValue;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.PasswordReset;
import org.bremersee.samba.ad.dc.model.event.InvitationEvent;
import org.bremersee.samba.ad.dc.model.event.PasswordResetEvent;
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
 * The email service.
 *
 * @author Christian Bremer
 */
@Service("emailService")
@ConditionalOnProperty(name = "spring.mail.host")
@Slf4j
public class EmailService {

  private final ApplicationProperties properties;

  private final MessageSource messageSource;

  private final TemplateEngine templateEngine;

  private final List<TemplateEngineContextSupplier> contextSuppliers;

  private final JavaMailSender javaMailSender;

  private final PasswordResetCryptoService<AesEncValue> passwordResetCryptoService;

  public EmailService(ApplicationProperties properties, MessageSource messageSource,
      TemplateEngine templateEngine, List<TemplateEngineContextSupplier> contextSuppliers,
      JavaMailSender javaMailSender,
      PasswordResetCryptoService<AesEncValue> passwordResetCryptoService) {
    this.properties = properties;
    this.messageSource = messageSource;
    this.templateEngine = templateEngine;
    this.contextSuppliers = contextSuppliers;
    this.javaMailSender = javaMailSender;
    this.passwordResetCryptoService = passwordResetCryptoService;
    log.info("Email service initialized.");
  }

  @EventListener
  @Async
  public void onPasswordResetEvent(PasswordResetEvent passwordResetEvent) {
    sendPasswordResetEmail(passwordResetEvent.getSource());
  }

  public void sendPasswordResetEmail(DomainUser user) {
    sendEmail(user, false);
  }

  @EventListener
  @Async
  public void onInvitationEvent(InvitationEvent event) {
    sendInvitationEmail(event.getSource());
  }

  public void sendInvitationEmail(DomainUser user) {
    sendEmail(user, true);
  }

  private void sendEmail(DomainUser user, boolean isInvitation) {
    if (isEmpty(user) || isEmpty(user.getEmail())) {
      return;
    }
    MimeMessagePreparator preparator = mimeMessage -> {
      MimeMessageHelper helper = new MimeMessageHelper(
          mimeMessage, true, StandardCharsets.UTF_8.name());
      helper.setFrom(properties.getEmail().getSender());
      helper.setTo(Objects.requireNonNull(user.getEmail()));
      helper.setSubject(getEmailSubject(user, isInvitation));
      helper.setText(getEmailText(user, isInvitation), true);
      /*
      for (MailInlineAttachment attachment : inlineAttachments) {
        helper.addInline(attachment.getContentId(),
            resourceLoader.getResource(attachment.getLocation()),
            attachment.getMimeType());
      }
      */
    };
    javaMailSender.send(preparator);
  }

  private String getEmailSubject(DomainUser user, boolean isInvitation) {
    return messageSource.getMessage(
        "todo",
        new Object[]{user.getFirstName()},
        "Welcome",
        user.getLocale());
  }

  private String getEmailText(DomainUser user, boolean isInvitation) {
    Locale locale = user.getLocale();
    Context ctx = new Context(locale);
    contextSuppliers.forEach(contextSupplier -> contextSupplier
        .getTemplateEngineContext().forEach(ctx::setVariable));
    ctx.setVariable("user", user);
    ctx.setVariable("properties", properties);
    ctx.setVariable("resetUri", getPasswordResetUri(user, isInvitation));
    String template = isInvitation
        ? "email/invitation-email"
        : "email/password-reset-email";
    return templateEngine.process(template, ctx);
  }

  private String getPasswordResetUri(DomainUser user, boolean isInvitation) {
    AesEncValue encValue = passwordResetCryptoService.encrypt(PasswordReset.builder()
        .username(user.getSamAccountName())
        .pwdLastSetDateTime(user.getPasswordLastSet())
        .invitation(isInvitation)
        .build());
    return new DefaultUriBuilderFactory(properties.getEmail().getBaseUri())
        .builder()
        .path("/passwd/password-reset")
        .queryParam("req", encValue.encryptedValue())
        .queryParam("s", encValue.salt())
        .build()
        .toString();
  }

}
