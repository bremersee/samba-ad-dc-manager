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

package org.bremersee.samba.ad.dc.domain.repository;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.common.repository.AdRepository;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.domain.model.DomainInfo;
import org.bremersee.samba.ad.dc.domain.model.PasswordInformation;
import org.bremersee.samba.ad.dc.common.repository.AdConstants;
import org.ldaptive.LdapAttribute;
import org.ldaptive.SearchRequest;
import org.ldaptive.ad.SecurityIdentifier;
import org.ldaptive.dn.Dn;
import org.passay.CharacterRule;
import org.passay.PasswordGenerator;
import org.springframework.stereotype.Component;

/**
 * The domain repository implementation.
 *
 * @author Christian Bremer
 */
@Component("domainRepository")
@Slf4j
public class DomainRepositoryImpl extends AdRepository implements DomainRepository {

  private final Random random;

  private final HostNameSupplier hostNameSupplier;

  private final SambaToolDomain domainTool;

  private String hostName;

  /**
   * Instantiates a new domain repository.
   *
   * @param properties the properties
   */
  public DomainRepositoryImpl(
      DomainControllerProperties properties,
      LdaptiveTemplate ldapTemplate,
      HostNameSupplier hostNameSupplier,
      SambaToolDomain domainTool) {
    super(properties, ldapTemplate);
    this.hostName = properties.getHostName();
    this.hostNameSupplier = hostNameSupplier;

    this.random = new SecureRandom();
    this.domainTool = domainTool;
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
    String baseDn = getProperties().getBaseDn().format();
    String attrName = AdConstants.OBJECT_SID.getName();
    String[] returnAttributes = new String[]{
        attrName
    };
    return getLdapTemplate()
        .findOne(SearchRequest.objectScopeSearchRequest(baseDn, returnAttributes))
        .map(ldapEntry -> ldapEntry.getAttribute(attrName))
        .map(LdapAttribute::getBinaryValue)
        .map(SecurityIdentifier::toString)
        .orElseThrow(() -> ServiceException.notFound(baseDn, attrName));
  }

  @Override
  public boolean isRfc2307Enabled() {
    Dn dn = new Dn("CN=ypservers,CN=ypServ30,CN=RpcServices,CN=System");
    dn.add(getProperties().getBaseDn());
    boolean result = dnExistsWithAnyObjectClass(dn.format());
    log.debug("Are nis extensions (rfc2307) installed? {}", result);
    return result;
  }

  @Override
  public DomainInfo getDomainInfo(String ipOrHostname) {
    return domainTool.getDomainInfo(ipOrHostname);
  }

  @Override
  public PasswordInformation getPasswordInformation() {
    log.debug("getPasswordInformation()");
    return domainTool.getPasswordInformation();
  }

  @Override
  public String createRandomPassword() {
    PasswordInformation passwordInformation = getPasswordInformation();
    int minLength = requireNonNullElse(passwordInformation.getMinimumPasswordLength(), 12);
    int maxLength = requireNonNullElse(passwordInformation.getMaximumPasswordLength(), 75);
    int maxPlus = Math.min(maxLength - minLength, 9);
    int length = Optional.of(minLength + (maxPlus > 0 ? random.nextInt(maxPlus) : 0))
        .filter(len -> len >= 4)
        .orElse(4);
    int lower = Math.max((int) Math.floor(length * 0.3), 1);
    int upper = Math.max((int) Math.floor(length * 0.3), 1);
    int digit = Math.max((int) Math.floor(length * 0.2), 1);
    int special = Math.max((int) Math.floor(length * 0.1), 1);
    List<CharacterRule> rules = getCharacterRules(lower, upper, digit, special);
    return new PasswordGenerator(random).generatePassword(length, rules);
  }

}
