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

package org.bremersee.samba.ad.dc.repository.cli;

import static java.util.Objects.nonNull;

import java.util.List;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.repository.SambaToolDomain;
import org.bremersee.samba.ad.dc.repository.cli.parser.DomainInfoParser;
import org.bremersee.samba.ad.dc.repository.cli.parser.PasswordInformationParser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * The samba tool domain cli.
 *
 * @author Christian Bremer
 */
@Component
public class SambaToolDomainCli extends SambaToolCli implements SambaToolDomain {

  private DomainInfoParser domainInfoParser;

  private PasswordInformationParser passwordInformationParser;

  /**
   * Instantiates a new samba tool domain cli.
   *
   * @param properties the properties
   */
  public SambaToolDomainCli(ApplicationProperties properties) {
    super(properties);
    domainInfoParser = DomainInfoParser.defaultParser();
    passwordInformationParser = PasswordInformationParser.defaultParser();
  }

  /**
   * Sets domain info parser.
   *
   * @param domainInfoParser the domain info parser
   */
  public void setDomainInfoParser(DomainInfoParser domainInfoParser) {
    if (nonNull(domainInfoParser)) {
      this.domainInfoParser = domainInfoParser;
    }
  }

  /**
   * Sets password information parser.
   *
   * @param passwordInformationParser the password information parser
   */
  public void setPasswordInformationParser(PasswordInformationParser passwordInformationParser) {
    if (nonNull(passwordInformationParser)) {
      this.passwordInformationParser = passwordInformationParser;
    }
  }

  @Override
  protected String getSubCommand() {
    return "domain";
  }

  @Cacheable(value = "domainInfoCache", key = "#p0")
  @Override
  public DomainInfo getDomainInfo(String ipOrHostname) {
    List<String> commands = getCommands();
    commands.add("info");
    commands.add(quote(ipOrHostname));
    return executeAndGet(commands, domainInfoParser)
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            DomainInfo.class.getSimpleName(),
            ipOrHostname,
            ErrorCode.EC_DOMAIN_INFO_NOT_FOUND));
  }

  @Cacheable(value = "passwordInformationCache")
  @Override
  public PasswordInformation getPasswordInformation() {
    List<String> commands = getCommands();
    commands.add("passwordsettings");
    commands.add("show");
    PasswordInformation raw = executeAndGet(
        commands,
        passwordInformationParser);
    int minLength = raw.getMinimumPasswordLength();
    int maxLength = Math.max(getProperties().getDomain().getMaximumPasswordLength(), minLength);
    return PasswordInformation.builder().from(raw)
        .maximumPasswordLength(maxLength)
        .simplePasswordRegexTemplate(getProperties().getDomain().getSimplePasswordRegexTemplate())
        .complexPasswordRegexTemplate(getProperties().getDomain().getComplexPasswordRegexTemplate())
        .build();
  }

}
