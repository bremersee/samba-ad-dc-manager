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

  private final JavaMailSender javaMailSender;

  private final DhcpLeaseListTool dhcpLeaseListTool;

  /**
   * Instantiates a new Feature detection configuration.
   *
   * @param applicationProperties the application properties
   * @param mailProperties the mail properties
   * @param javaMailSender the java mail sender
   * @param dhcpLeaseListTool the dhcp lease list tool
   */
  FeatureDetectionConfiguration(
      ApplicationProperties applicationProperties,
      MailProperties mailProperties,
      ObjectProvider<JavaMailSender> javaMailSender,
      DhcpLeaseListTool dhcpLeaseListTool) {
    this.applicationProperties = applicationProperties;
    this.mailProperties = mailProperties;
    this.javaMailSender = javaMailSender.getIfAvailable();
    this.dhcpLeaseListTool = dhcpLeaseListTool;
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
        .build();
    log.info("Features: {}", features);
    return features;
  }

  private boolean isMailEnabled() {
    String sender = applicationProperties.getEmail().getSender();
    if (isEmpty(sender)) {
      return false;
    }
    Pattern emailPattern = applicationProperties.getEmail().getEmailRegexFlags()
        .compile(applicationProperties.getEmail().getEmailRegex());
    return !isEmpty(mailProperties.getHost())
        && !isEmpty(javaMailSender)
        && emailPattern.matcher(sender).matches();
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
