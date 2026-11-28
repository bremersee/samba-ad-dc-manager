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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.repository.DomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The domain service implementation test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class DomainServiceImplTest {

  private DomainRepository domainRepository;

  private DomainServiceImpl target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    domainRepository = mock(DomainRepository.class);
    target = new DomainServiceImpl(domainRepository);
  }

  /**
   * Gets template engine context.
   */
  @Test
  void getTemplateEngineContext() {
    assertThat(target.getTemplateEngineContext())
        .containsEntry("domain", domainRepository)
        .hasSize(1);
  }

  /**
   * Gets domain info.
   *
   * @param softly the softly
   */
  @Test
  void getDomainInfo(SoftAssertions softly) {
    DomainInfo expected = DomainInfo.builder()
        .domain("samdom.example.org")
        .netbiosDomain("SAMDOM")
        .build();
    doReturn(expected).when(domainRepository).getDomainInfo();
    doReturn(expected).when(domainRepository).getDomainInfo("dc");
    softly.assertThat(target.getDomainInfo())
        .isEqualTo(expected);
    softly.assertThat(target.getDomainInfo("dc"))
        .isEqualTo(expected);
  }

  /**
   * Is rfc 2307 enabled.
   *
   * @param softly the softly
   */
  @Test
  void isRfc2307Enabled(SoftAssertions softly) {
    doReturn(true).when(domainRepository).isRfc2307Enabled();
    softly.assertThat(target.isRfc2307Enabled()).isTrue();
    doReturn(false).when(domainRepository).isRfc2307Enabled();
    softly.assertThat(target.isRfc2307Enabled()).isFalse();
  }

  /**
   * Gets password information.
   */
  @Test
  void getPasswordInformation() {
    PasswordInformation expected = mock(PasswordInformation.class);
    doReturn(expected).when(domainRepository).getPasswordInformation();
    assertThat(target.getPasswordInformation())
        .isSameAs(expected);
  }

  /**
   * Create random password.
   */
  @Test
  void createRandomPassword() {
    doReturn("secret").when(domainRepository).createRandomPassword();
    assertThat(target.createRandomPassword())
        .isEqualTo("secret");
  }

}
