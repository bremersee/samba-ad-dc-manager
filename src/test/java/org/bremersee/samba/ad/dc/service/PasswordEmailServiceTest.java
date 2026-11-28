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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.JsonTextEncrypterFactory;
import org.bremersee.samba.ad.dc.misc.JsonTextEncryptor;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.PasswordReset;
import org.bremersee.samba.ad.dc.model.event.InvitationEvent;
import org.bremersee.samba.ad.dc.model.event.PasswordResetEvent;
import org.bremersee.samba.ad.dc.model.event.PasswordResetSuccessEvent;
import org.bremersee.samba.ad.dc.repository.DomainRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

/**
 * The password email service test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class PasswordEmailServiceTest {

  private static final String SENDER = "admin@example.org";

  private static final String BASE_URI = "https://example.org";

  private TemplateEngine templateEngine;

  private JavaMailSender javaMailSender;

  private JsonTextEncryptor<PasswordReset> encryptor;

  private PasswordEmailService target;

  private DomainUser user;

  /**
   * Sets up.
   */
  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    properties.getEmail().setSender(SENDER);
    properties.getUser().setInvitationLifetime(Duration.ofDays(30L));
    properties.getUser().setPasswordResetRequestLifetime(Duration.ofDays(7L));

    StaticMessageSource messageSource = new StaticMessageSource();
    messageSource.addMessage(
        "email.invitation-email.subject", Locale.GERMANY, "Einladung von {0}");

    templateEngine = mock(TemplateEngine.class);
    doReturn("<p>Hello</p>").when(templateEngine).process(anyString(), any(IContext.class));

    DomainRepository domainRepository = mock(DomainRepository.class);
    doReturn(DomainInfo.builder().netbiosDomain("SAMDOM").build())
        .when(domainRepository)
        .getDomainInfo();

    javaMailSender = mock(JavaMailSender.class);

    encryptor = mock(JsonTextEncryptor.class);
    doReturn("encrypted").when(encryptor).encrypt(any());
    JsonTextEncrypterFactory encrypterFactory = mock(JsonTextEncrypterFactory.class);
    doReturn(encryptor).when(encrypterFactory).newInstance(PasswordReset.class);

    target = new PasswordEmailService(
        properties,
        messageSource,
        templateEngine,
        List.of(() -> Map.of("domain", domainRepository)),
        javaMailSender,
        encrypterFactory);

    user = DomainUser.builder()
        .samAccountName("anna")
        .email("anna@example.org")
        .passwordLastSet(OffsetDateTime.parse("2026-01-01T12:00:00Z"))
        .logonCount(3)
        .build();
  }

  /**
   * Tear down.
   */
  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private MimeMessage captureMessage() throws Exception {
    ArgumentCaptor<MimeMessagePreparator> captor = ArgumentCaptor
        .forClass(MimeMessagePreparator.class);
    verify(javaMailSender).send(captor.capture());
    MimeMessage mimeMessage = new MimeMessage((Session) null);
    captor.getValue().prepare(mimeMessage);
    return mimeMessage;
  }

  private IContext captureContext(String template) {
    ArgumentCaptor<IContext> captor = ArgumentCaptor.forClass(IContext.class);
    verify(templateEngine).process(eq(template), captor.capture());
    return captor.getValue();
  }

  private PasswordReset capturePasswordReset() {
    ArgumentCaptor<PasswordReset> captor = ArgumentCaptor.forClass(PasswordReset.class);
    verify(encryptor).encrypt(captor.capture());
    return captor.getValue();
  }

  /**
   * On password reset event.
   *
   * @param softly the softly
   * @throws Exception the exception
   */
  @Test
  void onPasswordResetEvent(SoftAssertions softly) throws Exception {
    target.onPasswordResetEvent(new PasswordResetEvent(user, BASE_URI));

    MimeMessage message = captureMessage();
    softly.assertThat(message.getFrom())
        .containsExactly(new InternetAddress(SENDER));
    softly.assertThat(message.getAllRecipients())
        .containsExactly(new InternetAddress("anna@example.org"));
    softly.assertThat(message.getSubject())
        .isEqualTo("Your password reset request");

    IContext ctx = captureContext("email/password-reset-email");
    softly.assertThat(ctx.getVariable("resetUri"))
        .isEqualTo(BASE_URI + "/passwd/password-reset?req=encrypted");
    softly.assertThat(ctx.getVariable("lifetimeDays"))
        .isEqualTo(7L);
    softly.assertThat(ctx.getVariable("user"))
        .isEqualTo(user);

    PasswordReset passwordReset = capturePasswordReset();
    softly.assertThat(passwordReset.getUsername()).isEqualTo("anna");
    softly.assertThat(passwordReset.getPwdLastSetDateTime()).isEqualTo(user.getPasswordLastSet());
    softly.assertThat(passwordReset.isInvitation()).isFalse();
  }

  /**
   * On invitation event of new user.
   *
   * @param softly the softly
   * @throws Exception the exception
   */
  @Test
  void onInvitationEventOfNewUser(SoftAssertions softly) throws Exception {
    DomainUser newUser = DomainUser.builder()
        .from(user)
        .logonCount(0)
        .build();
    target.onInvitationEvent(new InvitationEvent(newUser, BASE_URI));

    MimeMessage message = captureMessage();
    softly.assertThat(message.getSubject())
        .isEqualTo("Einladung von Your domain administrator");

    IContext ctx = captureContext("email/invitation-email");
    softly.assertThat(ctx.getVariable("lifetimeDays"))
        .isEqualTo(30L);
    softly.assertThat(capturePasswordReset().isInvitation())
        .isTrue();
  }

  /**
   * On invitation event of existing user.
   *
   * @param softly the softly
   * @throws Exception the exception
   */
  @Test
  void onInvitationEventOfExistingUser(SoftAssertions softly) throws Exception {
    target.onInvitationEvent(new InvitationEvent(user, BASE_URI));

    MimeMessage message = captureMessage();
    softly.assertThat(message.getSubject())
        .isEqualTo("Your password reset request");
    captureContext("email/password-reset-email");
    softly.assertThat(capturePasswordReset().isInvitation())
        .isFalse();
  }

  /**
   * On password reset success event.
   *
   * @param softly the softly
   * @throws Exception the exception
   */
  @Test
  void onPasswordResetSuccessEvent(SoftAssertions softly) throws Exception {
    target.onPasswordResetSuccessEvent(new PasswordResetSuccessEvent(user, BASE_URI));

    MimeMessage message = captureMessage();
    softly.assertThat(message.getAllRecipients())
        .containsExactly(new InternetAddress("anna@example.org"));
    softly.assertThat(message.getSubject())
        .isEqualTo("You reset your password successfully");

    IContext ctx = captureContext("email/password-reset-success-email");
    softly.assertThat(ctx.getVariable("netbiosUsername"))
        .isEqualTo("SAMDOM\\anna");
    verifyNoInteractions(encryptor);
  }

  /**
   * No email is sent without email address or base uri.
   */
  @Test
  void noEmailWithoutEmailAddressOrBaseUri() {
    DomainUser userWithoutEmail = DomainUser.builder()
        .samAccountName("bert")
        .build();
    target.onPasswordResetEvent(new PasswordResetEvent(userWithoutEmail, BASE_URI));
    target.onInvitationEvent(new InvitationEvent(userWithoutEmail, BASE_URI));
    target.onPasswordResetSuccessEvent(new PasswordResetSuccessEvent(userWithoutEmail, BASE_URI));
    target.onPasswordResetEvent(new PasswordResetEvent(user, ""));
    target.onPasswordResetSuccessEvent(new PasswordResetSuccessEvent(user, null));
    verifyNoInteractions(javaMailSender);
  }

}
