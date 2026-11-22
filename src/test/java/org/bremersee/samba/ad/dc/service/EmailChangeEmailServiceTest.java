/*
 * Copyright 2025-2026 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.JsonTextEncrypterFactory;
import org.bremersee.samba.ad.dc.misc.JsonTextEncryptor;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.EmailChange;
import org.bremersee.samba.ad.dc.model.event.EmailChangeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

/**
 * The email change email service test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class EmailChangeEmailServiceTest {

  private static final String SENDER = "admin@example.org";

  private static final String BASE_URI = "https://example.org";

  private TemplateEngine templateEngine;

  private JavaMailSender javaMailSender;

  private JsonTextEncryptor<EmailChange> encryptor;

  private EmailChangeEmailService target;

  private DomainUser user;

  /**
   * Sets up.
   */
  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    properties.getEmail().setSender(SENDER);
    properties.getUser().setChangeEmailRequestLifetime(Duration.ofDays(3L));

    StaticMessageSource messageSource = new StaticMessageSource();
    messageSource.addMessage(
        "email.confirm-email.subject", Locale.US, "Confirm your new email address");

    templateEngine = mock(TemplateEngine.class);
    doReturn("<p>Hello</p>").when(templateEngine).process(anyString(), any(IContext.class));

    javaMailSender = mock(JavaMailSender.class);

    encryptor = mock(JsonTextEncryptor.class);
    doReturn("encrypted").when(encryptor).encrypt(any());
    JsonTextEncrypterFactory encrypterFactory = mock(JsonTextEncrypterFactory.class);
    doReturn(encryptor).when(encrypterFactory).newInstance(EmailChange.class);

    target = new EmailChangeEmailService(
        properties,
        messageSource,
        templateEngine,
        List.of(),
        javaMailSender,
        encrypterFactory);

    user = DomainUser.builder()
        .samAccountName("anna")
        .email("anna@example.org")
        .preferredLanguage("en-US")
        .build();
  }

  /**
   * On email change event.
   *
   * @param softly the softly
   * @throws Exception the exception
   */
  @Test
  void onEmailChangeEvent(SoftAssertions softly) throws Exception {
    target.onEmailChangeEvent(new EmailChangeEvent(user, "anna@example.com", BASE_URI));

    ArgumentCaptor<MimeMessagePreparator> preparatorCaptor = ArgumentCaptor
        .forClass(MimeMessagePreparator.class);
    verify(javaMailSender).send(preparatorCaptor.capture());
    MimeMessage message = new MimeMessage((Session) null);
    preparatorCaptor.getValue().prepare(message);
    softly.assertThat(message.getFrom())
        .containsExactly(new InternetAddress(SENDER));
    softly.assertThat(message.getAllRecipients())
        .containsExactly(new InternetAddress("anna@example.com"));
    softly.assertThat(message.getSubject())
        .isEqualTo("Confirm your new email address");

    ArgumentCaptor<IContext> ctxCaptor = ArgumentCaptor.forClass(IContext.class);
    verify(templateEngine).process(eq("email/confirm-email"), ctxCaptor.capture());
    IContext ctx = ctxCaptor.getValue();
    softly.assertThat(ctx.getVariable("confirmationUri"))
        .isEqualTo(BASE_URI + "/user/profile?req=encrypted");
    softly.assertThat(ctx.getVariable("lifetimeDays"))
        .isEqualTo(3L);

    ArgumentCaptor<EmailChange> emailChangeCaptor = ArgumentCaptor.forClass(EmailChange.class);
    verify(encryptor).encrypt(emailChangeCaptor.capture());
    softly.assertThat(emailChangeCaptor.getValue().getUsername()).isEqualTo("anna");
    softly.assertThat(emailChangeCaptor.getValue().getOldEmail()).isEqualTo("anna@example.org");
    softly.assertThat(emailChangeCaptor.getValue().getNewEmail()).isEqualTo("anna@example.com");
  }

  /**
   * Default subject is used if there is no message.
   *
   * @throws Exception the exception
   */
  @Test
  void defaultSubject() throws Exception {
    DomainUser germanUser = DomainUser.builder()
        .from(user)
        .preferredLanguage("de-DE")
        .build();
    target.onEmailChangeEvent(new EmailChangeEvent(germanUser, "anna@example.com", BASE_URI));
    ArgumentCaptor<MimeMessagePreparator> captor = ArgumentCaptor
        .forClass(MimeMessagePreparator.class);
    verify(javaMailSender).send(captor.capture());
    MimeMessage message = new MimeMessage((Session) null);
    captor.getValue().prepare(message);
    assertThat(message.getSubject())
        .isEqualTo("Please confirm your email address");
  }

  /**
   * No email is sent without new email or base uri.
   */
  @Test
  void noEmailWithoutNewEmailOrBaseUri() {
    target.onEmailChangeEvent(new EmailChangeEvent(user, "", BASE_URI));
    target.onEmailChangeEvent(new EmailChangeEvent(user, "anna@example.com", null));
    verifyNoInteractions(javaMailSender);
  }

}
