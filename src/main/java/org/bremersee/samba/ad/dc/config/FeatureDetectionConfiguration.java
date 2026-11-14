/*
 * Copyright 2026 the original author or authors.
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

package org.bremersee.samba.ad.dc.config;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.misc.FeaturesAware;
import org.bremersee.samba.ad.dc.model.Features;
import org.bremersee.samba.ad.dc.repository.DhcpLeaseListTool;
import org.bremersee.spring.security.ldaptive.authentication.LdaptiveRememberMeServices;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * The feature detection configuration.
 *
 * @author Christian Bremer
 */
@Configuration
@EnableConfigurationProperties({
    MailProperties.class
})
@Slf4j
class FeatureDetectionConfiguration {

  private final ApplicationProperties applicationProperties;

  private final MailProperties mailProperties;

  private final DhcpLeaseListTool dhcpLeaseListTool;

  private final JavaMailSender javaMailSender;

  private final LdaptiveRememberMeServices rememberMeServices;

  /**
   * Instantiates a new Feature detection configuration.
   *
   * @param applicationProperties the application properties
   * @param mailProperties the mail properties
   * @param dhcpLeaseListTool the dhcp lease list tool
   * @param javaMailSender the java mail sender
   * @param rememberMeServices the remember me services
   */
  FeatureDetectionConfiguration(
      ApplicationProperties applicationProperties,
      MailProperties mailProperties,
      DhcpLeaseListTool dhcpLeaseListTool,
      ObjectProvider<JavaMailSender> javaMailSender,
      ObjectProvider<LdaptiveRememberMeServices> rememberMeServices) {
    this.applicationProperties = applicationProperties;
    this.mailProperties = mailProperties;
    this.dhcpLeaseListTool = dhcpLeaseListTool;
    this.javaMailSender = javaMailSender.getIfAvailable();
    this.rememberMeServices = rememberMeServices.getIfAvailable();
  }

  /**
   * Creates features description.
   *
   * @return the features
   */
  @Bean(name = "sambaAdDcMangerFeatures")
  @Primary
  public Features features() {
    Features features = Features.builder()
        .dhcpEnabled(isDhcpEnabled())
        .mailEnabled(isMailEnabled())
        .rememberMeEnabled(isRememberMeEnabled())
        .build();
    log.info("Features: {}", features);
    return features;
  }

  private boolean isDhcpEnabled() {
    if (isEmpty(applicationProperties.getCli().getDhcpLeaseListBinary())) {
      return false;
    }
    try {
      dhcpLeaseListTool.findActive();
      return true;
    } catch (RuntimeException e) {
      return false;
    }
  }

  private boolean isMailEnabled() {
    String sender = applicationProperties.getEmail().getSender();
    if (isEmpty(sender)) {
      log.info("Mail sender is not configured.");
      return false;
    }
    Pattern emailPattern = applicationProperties.getEmail().getEmailRegexFlags()
        .compile(applicationProperties.getEmail().getEmailRegex());
    boolean matches = emailPattern.matcher(sender).matches();
    if (!matches) {
      log.info("Mail sender is not a valid e-mail address.");
    }
    return matches
        && !isEmpty(mailProperties.getHost())
        && !isEmpty(javaMailSender);
  }

  private boolean isRememberMeEnabled() {
    return !isEmpty(rememberMeServices);
  }

  /**
   * The features aware configuration.
   *
   * @author Christian Bremer
   */
  @Configuration
  static class FeaturesAwareConfiguration {

    /**
     * Instantiates a new features aware configuration.
     *
     * @param features the features
     * @param components the components
     */
    FeaturesAwareConfiguration(Features features, List<FeaturesAware> components) {
      log.info("Setting features on {} component(s).", components.size());
      components.forEach(component -> component.setFeatures(features));
    }
  }
}
