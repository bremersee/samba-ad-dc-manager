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
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.TemplateEngineContextSupplier;
import org.bremersee.samba.ad.dc.model.AesEncValue;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.EmailChange;
import org.bremersee.samba.ad.dc.model.event.EmailChangeEvent;
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
 * The email-change email service.
 *
 * @author Christian Bremer
 */
@Service("emailChangeEmailService")
@ConditionalOnProperty(name = "spring.mail.host")
@Slf4j
public class EmailChangeEmailService extends AbstractEmailService {

  private final CryptoService<EmailChange, AesEncValue> cryptoService;

  /**
   * Instantiates a new email-change email service.
   *
   * @param properties the properties
   * @param messageSource the message source
   * @param templateEngine the template engine
   * @param contextSuppliers the context suppliers
   * @param javaMailSender the java mail sender
   * @param cryptoService the crypto service
   */
  public EmailChangeEmailService(
      ApplicationProperties properties,
      MessageSource messageSource,
      TemplateEngine templateEngine,
      List<TemplateEngineContextSupplier> contextSuppliers,
      JavaMailSender javaMailSender,
      CryptoService<EmailChange, AesEncValue> cryptoService) {
    super(properties, messageSource, templateEngine, contextSuppliers, javaMailSender);
    this.cryptoService = cryptoService;
  }

  /**
   * On email change event.
   *
   * @param event the event
   */
  @EventListener
  @Async
  public void onEmailChangeEvent(EmailChangeEvent event) {
    sendEmail(event.getSource(), event.getNewEmail(), event.getBaseUri());
  }

  private void sendEmail(DomainUser user, String newEmail, String baseUri) {
    if (isEmpty(user) || isEmpty(newEmail) || isEmpty(baseUri)) {
      return;
    }
    String subject = getMessageSource().getMessage(
        "email.confirm-email.subject",
        null,
        "Please confirm your email address",
        user.getLocale());
    MimeMessagePreparator preparator = mimeMessage -> {
      MimeMessageHelper helper = new MimeMessageHelper(
          mimeMessage, true, StandardCharsets.UTF_8.name());
      helper.setFrom(getProperties().getEmail().getSender());
      helper.setTo(newEmail);
      helper.setSubject(Objects.requireNonNull(subject));
      helper.setText(getEmailText(user, newEmail, baseUri), true);
    };
    getJavaMailSender().send(preparator);
  }

  private String getEmailText(DomainUser user, String newEmail, String baseUri) {
    Context ctx = createContext(user, baseUri);
    ctx.setVariable("confirmationUri", getConfirmationUri(user, newEmail, baseUri));
    ctx.setVariable(
        "lifetimeDays",
        getProperties().getUser().getChangeEmailRequestLifetime().toDays());
    String template = "email/confirm-email";
    return getTemplateEngine().process(template, ctx);
  }

  private String getConfirmationUri(DomainUser user, String newEmail, String baseUri) {
    AesEncValue encValue = cryptoService.encrypt(EmailChange.of(user, newEmail));
    return new DefaultUriBuilderFactory(baseUri)
        .builder()
        .path("/user/profile")
        .queryParam("req", encValue.encryptedValue())
        .queryParam("s", encValue.salt())
        .build()
        .toString();
  }

}
