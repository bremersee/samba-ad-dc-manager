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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.config.DomainUserProperties.DefaultLoginPage;
import org.bremersee.samba.ad.dc.misc.JsonTextEncrypterFactory;
import org.bremersee.samba.ad.dc.misc.TemplateEngineContextSupplier;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.spring.security.core.NormalizedPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * The abstract email service test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class AbstractEmailServiceTest {

  private ApplicationProperties properties;

  private AbstractEmailService target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    properties = new ApplicationProperties();
    TemplateEngineContextSupplier contextSupplier = () -> Map.of("foo", "bar");
    target = new AbstractEmailService(
        properties,
        new StaticMessageSource(),
        mock(TemplateEngine.class),
        List.of(contextSupplier),
        mock(JavaMailSender.class),
        mock(JsonTextEncrypterFactory.class)) {
    };
  }

  /**
   * Tear down.
   */
  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  /**
   * Init with missing sender.
   */
  @Test
  void initWithMissingSender() {
    properties.getEmail().setSender("");
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> target.init());
  }

  /**
   * Init with invalid sender.
   */
  @Test
  void initWithInvalidSender() {
    properties.getEmail().setSender("not an email");
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> target.init());
  }

  /**
   * Init with valid sender.
   */
  @Test
  void initWithValidSender() {
    properties.getEmail().setSender("Admin@Example.org");
    assertThatNoException()
        .isThrownBy(() -> target.init());
  }

  /**
   * Gets default login page.
   *
   * @param softly the softly
   */
  @Test
  void getDefaultLoginPage(SoftAssertions softly) {
    DefaultLoginPage actual = target.getDefaultLoginPage("https://example.org");
    softly.assertThat(actual.getUrl()).isEqualTo("https://example.org/login");
    softly.assertThat(actual.isUsingNetbiosDomainPrefix()).isFalse();

    actual = target.getDefaultLoginPage(null);
    softly.assertThat(actual.getUrl()).isEqualTo("#");

    properties.getUser().getDefaultLoginPage().setUrl("#");
    actual = target.getDefaultLoginPage("https://example.org");
    softly.assertThat(actual.getUrl()).isEqualTo("https://example.org/login");

    properties.getUser().getDefaultLoginPage().setUrl("https://login.example.org");
    actual = target.getDefaultLoginPage("https://example.org");
    softly.assertThat(actual)
        .isSameAs(properties.getUser().getDefaultLoginPage());
  }

  /**
   * Gets email regards without authentication.
   */
  @Test
  void getEmailRegardsWithoutAuthentication() {
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(target.getEmailRegards())
        .isEqualTo("Your domain administrator");

    // not authenticated
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("admin", "secret"));
    softly.assertThat(target.getEmailRegards())
        .isEqualTo("Your domain administrator");

    // principal is not a java.security.Principal
    SecurityContextHolder.getContext().setAuthentication(
        new TestingAuthenticationToken("admin", "secret", "ROLE_ADMIN"));
    softly.assertThat(target.getEmailRegards())
        .isEqualTo("Your domain administrator");
    softly.assertAll();
  }

  /**
   * Gets email regards with normalized principal.
   *
   * @param softly the softly
   */
  @Test
  void getEmailRegardsWithNormalizedPrincipal(SoftAssertions softly) {
    NormalizedPrincipal principal = mock(NormalizedPrincipal.class);
    doReturn("admin").when(principal).getName();
    SecurityContextHolder.getContext().setAuthentication(
        UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));

    softly.assertThat(target.getEmailRegards())
        .isEqualTo("admin");

    doReturn("Anna").when(principal).getFirstName();
    softly.assertThat(target.getEmailRegards())
        .isEqualTo("Anna");

    doReturn("Smith").when(principal).getLastName();
    softly.assertThat(target.getEmailRegards())
        .isEqualTo("Anna Smith");

    doReturn(null).when(principal).getFirstName();
    softly.assertThat(target.getEmailRegards())
        .isEqualTo("Smith");
  }

  /**
   * Create context.
   *
   * @param softly the softly
   */
  @Test
  void createContext(SoftAssertions softly) {
    DomainUser user = DomainUser.builder()
        .samAccountName("anna")
        .preferredLanguage("en-US")
        .build();
    Context actual = target.createContext(user, "https://example.org");
    softly.assertThat(actual.getLocale()).isEqualTo(Locale.US);
    softly.assertThat(actual.getVariable("foo")).isEqualTo("bar");
    softly.assertThat(actual.getVariable("user")).isEqualTo(user);
    softly.assertThat(actual.getVariable("properties")).isSameAs(properties);
    softly.assertThat(actual.getVariable("baseUri")).isEqualTo("https://example.org");
    softly.assertThat(actual.getVariable("regards")).isEqualTo("Your domain administrator");
    softly.assertThat(actual.getVariable("defaultLoginPage"))
        .extracting("url")
        .isEqualTo("https://example.org/login");

    actual = target.createContext(user, null);
    softly.assertThat(actual.getVariable("baseUri")).isEqualTo("");
  }

}
