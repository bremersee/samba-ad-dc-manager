/*
 * Copyright 2019-2026 the original author or authors.
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

import static org.springframework.util.ObjectUtils.isEmpty;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.PasswordGenerator;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.model.Sid;
import org.ldaptive.SearchRequest;
import org.ldaptive.dn.Dn;
import org.springframework.stereotype.Component;

/**
 * The domain repository implementation.
 *
 * @author Christian Bremer
 */
@Component("domainRepository")
@Slf4j
public class DomainRepositoryImpl extends AdRepository implements DomainRepository {

  private final HostNameSupplier hostNameSupplier;

  private final SambaToolDomain domainTool;

  private final PasswordGenerator passwordGenerator;

  private String hostName;

  /**
   * Instantiates a new domain repository.
   *
   * @param properties the properties
   * @param ldapOperations the ldap operations
   * @param hostNameSupplier the host name supplier
   * @param domainTool the domain tool
   * @param passwordGenerator the password generator
   */
  public DomainRepositoryImpl(
      ApplicationProperties properties,
      LdaptiveOperations ldapOperations,
      HostNameSupplier hostNameSupplier,
      SambaToolDomain domainTool,
      PasswordGenerator passwordGenerator) {
    super(properties, ldapOperations);
    this.hostName = properties.getDomain().getHostName();
    this.hostNameSupplier = hostNameSupplier;
    this.domainTool = domainTool;
    this.passwordGenerator = passwordGenerator;
  }

  @Override
  public String getHostName() {
    if (isEmpty(hostName)) {
      hostName = hostNameSupplier.getHostName();
    }
    return hostName;
  }

  @Override
  public String getDomainSid() {
    log.debug("getDomainSid()");
    String baseDn = getProperties().getBaseDn();
    String attrName = AdConstants.OBJECT_SID.getName();
    String[] returnAttributes = new String[]{
        attrName
    };
    return getLdapOperations()
        .findOne(SearchRequest.objectScopeSearchRequest(baseDn, returnAttributes))
        .flatMap(AdConstants.OBJECT_SID::getValue)
        .map(Sid::getValue)
        .orElseThrow(() -> ServiceException.notFound(baseDn, attrName));
  }

  @Override
  public boolean isRfc2307Enabled() {
    Dn dn = getDnTool().addBaseDn(AdConstants.YELLOW_PAGES);
    boolean result = dnExistsWithAnyObjectClass(dn.format());
    log.debug("isRfc2307Enabled() {}", result);
    return result;
  }

  @Override
  public DomainInfo getDomainInfo() {
    log.debug("getDomainInfo()");
    return domainTool.getDomainInfo(getHostName());
  }

  @Override
  public DomainInfo getDomainInfo(String ipOrHostname) {
    log.debug("getDomainInfo({})", ipOrHostname);
    return domainTool.getDomainInfo(ipOrHostname);
  }

  @Override
  public PasswordInformation getPasswordInformation() {
    log.debug("getPasswordInformation()");
    return domainTool.getPasswordInformation();
  }

  @Override
  public String createRandomPassword() {
    return passwordGenerator.generatePassword(getPasswordInformation());
  }

}
