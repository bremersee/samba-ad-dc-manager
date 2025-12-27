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

package org.bremersee.samba.ad.dc.repository;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.ldaptive.LdapAttribute;
import org.ldaptive.SearchRequest;
import org.ldaptive.ad.SecurityIdentifier;
import org.ldaptive.dn.Dn;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * The domain repository implementation.
 *
 * @author Christian Bremer
 */
@Component("domainRepository")
@Slf4j
public class DomainRepositoryImpl extends AdRepository implements DomainRepository, KeyGenerator {

  private final HostNameSupplier hostNameSupplier;

  private final SambaToolDomain domainTool;

  private String hostName;

  /**
   * Instantiates a new domain repository.
   *
   * @param properties the properties
   */
  public DomainRepositoryImpl(
      ApplicationProperties properties,
      LdaptiveTemplate ldapTemplate,
      HostNameSupplier hostNameSupplier,
      SambaToolDomain domainTool) {
    super(properties, ldapTemplate);
    this.hostName = properties.getDomain().getHostName();
    this.hostNameSupplier = hostNameSupplier;
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
    log.debug("getDomainSid()");
    String baseDn = getProperties().getBaseDn();
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
    dn.add(getDnTool().getBaseDn());
    boolean result = dnExistsWithAnyObjectClass(dn.format());
    log.debug("isRfc2307Enabled() {}", result);
    return result;
  }

  @Cacheable(value = "domainInfoCache", keyGenerator = "domainRepository")
  @Override
  public DomainInfo getDomainInfo() {
    log.debug("getDomainInfo()");
    return domainTool.getDomainInfo(getHostName());
  }

  @Cacheable(value = "domainInfoCache", key = "#p0")
  @Override
  public DomainInfo getDomainInfo(String ipOrHostname) {
    log.debug("getDomainInfo({})", ipOrHostname);
    return domainTool.getDomainInfo(ipOrHostname);
  }

  @Cacheable(value = "passwordInformationCache")
  @Override
  public PasswordInformation getPasswordInformation() {
    log.debug("getPasswordInformation()");
    return domainTool.getPasswordInformation();
  }

  @NonNull
  @Override
  public Object generate(
      @NonNull Object target,
      @NonNull Method method,
      @NonNull Object... params) {

    // the cache key for getDomainInfo()
    return getHostName();
  }
}
