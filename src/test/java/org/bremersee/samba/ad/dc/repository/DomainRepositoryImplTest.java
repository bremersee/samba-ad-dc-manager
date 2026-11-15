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

package org.bremersee.samba.ad.dc.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import java.util.UUID;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.DefaultDnTool;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.misc.PasswordGenerator;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.model.Sid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ldaptive.LdapEntry;

/**
 * The domain repository implementation test.
 *
 * @author Christian Bremer
 */
class DomainRepositoryImplTest {

  private DnTool dnTool;

  private LdaptiveOperations ldapOperations;

  private HostNameSupplier hostNameSupplier;

  private SambaToolDomain domainTool;

  private PasswordGenerator passwordGenerator;

  private DomainRepositoryImpl target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    properties.setBaseDn("dc=samdom,dc=example,dc=org");
    dnTool = new DefaultDnTool(properties);
    ldapOperations = mock(LdaptiveOperations.class);
    hostNameSupplier = mock(HostNameSupplier.class);
    domainTool = mock(SambaToolDomain.class);
    passwordGenerator = mock(PasswordGenerator.class);
    target = new DomainRepositoryImpl(
        properties,
        ldapOperations,
        hostNameSupplier,
        domainTool,
        passwordGenerator);
  }

  /**
   * Gets host name.
   */
  @Test
  void getHostName() {
    String hostName = "dc";
    doReturn(hostName).when(hostNameSupplier).getHostName();
    String actual = target.getHostName();
    assertThat(actual).isEqualTo(hostName);
  }

  /**
   * Gets domain sid.
   */
  @Test
  void getDomainSid() {
    String domainSid = Sid.DEFAULT_SID_PREFIX + "1111111111-111111111-1111111111";
    LdapEntry entry = new LdapEntry();
    AdConstants.OBJECT_SID.setValue(entry, Sid.builder()
        .value(domainSid)
        .build());
    doReturn(Optional.of(entry))
        .when(ldapOperations)
        .findOne(any());
    String actual = target.getDomainSid();
    assertThat(actual).isEqualTo(domainSid);
  }

  /**
   * Is rfc 2307 enabled.
   */
  @Test
  void isRfc2307Enabled() {
    LdapEntry entry = new LdapEntry();
    entry.setDn(dnTool.addBaseDn(AdConstants.YELLOW_PAGES).format());
    AdConstants.DN.setValue(entry, dnTool.addBaseDn(AdConstants.YELLOW_PAGES));
    doReturn(Optional.of(entry))
        .when(ldapOperations)
        .findOne(any());
    boolean actual = target.isRfc2307Enabled();
    assertThat(actual).isTrue();
  }

  /**
   * Gets domain info.
   */
  @Test
  void getDomainInfo() {
    doReturn("dc").when(hostNameSupplier).getHostName();
    DomainInfo expected = DomainInfo.builder()
        .domain("samdom.example.org")
        .forest("samdom.example.org")
        .netbiosDomain("SAMDOM")
        .domainControllerName("dc.samdom.example.org")
        .domainControllerNetbiosName("DC")
        .serverSite("Default-First-Site-Name")
        .clientSite("Default-First-Site-Name")
        .build();
    doReturn(expected)
        .when(domainTool)
        .getDomainInfo("dc");
    DomainInfo actual = target.getDomainInfo();
    assertThat(actual).isEqualTo(expected);
  }

  /**
   * Gets domain info with host name.
   */
  @Test
  void getDomainInfoWithHostName() {
    DomainInfo expected = DomainInfo.builder()
        .domain("samdom.example.org")
        .forest("samdom.example.org")
        .netbiosDomain("SAMDOM")
        .domainControllerName("dc.samdom.example.org")
        .domainControllerNetbiosName("DC")
        .serverSite("Default-First-Site-Name")
        .clientSite("Default-First-Site-Name")
        .build();
    doReturn(expected)
        .when(domainTool)
        .getDomainInfo("dc");
    DomainInfo actual = target.getDomainInfo("dc");
    assertThat(actual).isEqualTo(expected);
  }

  /**
   * Gets password information.
   */
  @Test
  void getPasswordInformation() {
    PasswordInformation expected = PasswordInformation.builder().build();
    doReturn(expected)
        .when(domainTool)
        .getPasswordInformation();
    PasswordInformation actual = target.getPasswordInformation();
    assertThat(actual).isEqualTo(expected);
  }

  /**
   * Create random password.
   */
  @Test
  void createRandomPassword() {
    PasswordInformation passwordInformation = PasswordInformation.builder().build();
    doReturn(passwordInformation)
        .when(domainTool)
        .getPasswordInformation();
    String expected = UUID.randomUUID().toString();
    doReturn(expected)
        .when(passwordGenerator)
        .generatePassword(passwordInformation);
    String actual = target.createRandomPassword();
    assertThat(actual).isEqualTo(expected);
  }
}