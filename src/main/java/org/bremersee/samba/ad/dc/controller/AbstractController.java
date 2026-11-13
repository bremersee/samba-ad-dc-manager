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

package org.bremersee.samba.ad.dc.controller;

import static org.springframework.util.ObjectUtils.isEmpty;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Optional;
import lombok.Getter;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.samba.ad.dc.misc.SortMapperAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * The type Abstract controller.
 */
public abstract class AbstractController implements SortMapperAware {

  /**
   * The constant PAGE.
   */
  public static final String PAGE = "page";

  /**
   * The constant PAGE_DEFAULT_INT.
   */
  public static final int PAGE_DEFAULT_INT = 0;

  /**
   * The constant PAGE_DEFAULT.
   */
  public static final String PAGE_DEFAULT = "" + PAGE_DEFAULT_INT;

  /**
   * The constant SIZE.
   */
  public static final String SIZE = "size";

  /**
   * The constant SIZE_DEFAULT_INT.
   */
  public static final int SIZE_DEFAULT_INT = 20;

  /**
   * The constant SIZE_DEFAULT.
   */
  public static final String SIZE_DEFAULT = "" + SIZE_DEFAULT_INT;

  /**
   * The constant SORT.
   */
  public static final String SORT = "sort";

  /**
   * The constant DN_SORT.
   */
  public static final String DN_SORT = "distinguishedName";

  /**
   * The constant COMPUTER_SORT.
   */
  public static final String COMPUTER_SORT = "name";

  /**
   * The constant DHCP_LEASE_SORT.
   */
  public static final String DHCP_LEASE_SORT = "ip";

  /**
   * The constant GROUP_SORT.
   */
  public static final String GROUP_SORT = "samAccountName";

  /**
   * The constant OU_SORT.
   */
  public static final String OU_SORT = "nameTree";

  /**
   * The constant USER_SORT_LAST_NAME.
   */
  public static final String USER_SORT_LAST_NAME = "lastName";

  /**
   * The constant USER_SORT_FIRST_NAME.
   */
  public static final String USER_SORT_FIRST_NAME = "firstName";

  /**
   * The constant USER_SORT_USERNAME.
   */
  public static final String USER_SORT_USERNAME = "samAccountName";

  /**
   * The constant USER_SORT.
   */
  public static final String USER_SORT = USER_SORT_LAST_NAME
      + ";" + USER_SORT_FIRST_NAME
      + ";" + USER_SORT_USERNAME;

  /**
   * The constant QUERY.
   */
  public static final String QUERY = "q";

  /**
   * The constant QUERY_DEFAULT.
   */
  public static final String QUERY_DEFAULT = "";

  /**
   * The constant OU.
   */
  public static final String OU = "ou";

  /**
   * The constant SCOPE.
   */
  public static final String SCOPE = "scope";

  /**
   * The constant ZONE_NAME.
   */
  public static final String ZONE_NAME = "zone-name";

  /**
   * The constant ZONE_TYPE.
   */
  public static final String ZONE_TYPE = "zone-type";

  /**
   * The constant ZONE_TYPE_DEFAULT.
   */
  public static final String ZONE_TYPE_DEFAULT = "primary";

  /**
   * The constant DNS_ENTRY_NAME.
   */
  public static final String DNS_ENTRY_NAME = "name";

  /**
   * The constant DNS_ENTRY_TYPE.
   */
  public static final String DNS_ENTRY_TYPE = "type";

  /**
   * The constant DNS_ENTRY_VALUE.
   */
  public static final String DNS_ENTRY_VALUE = "value";

  /**
   * The constant DNS_ENTRY_SORT.
   */
  public static final String DNS_ENTRY_SORT = DNS_ENTRY_NAME
      + ";" + DNS_ENTRY_TYPE
      + ";" + DNS_ENTRY_VALUE;

  @Getter
  private SortMapper sortMapper;

  /**
   * Instantiates a new abstract controller.
   */
  protected AbstractController() {
    this.sortMapper = SortMapper.defaultSortMapper();
  }

  @Override
  public void setSortMapper(SortMapper sortMapper) {
    if (!isEmpty(sortMapper)) {
      this.sortMapper = sortMapper;
    }
  }

  /**
   * Gets base uri.
   *
   * @param configuredBaseUri the configured base uri
   * @return the base uri
   */
  protected static String getBaseUri(String configuredBaseUri) {
    if (!isEmpty(configuredBaseUri)
        && (configuredBaseUri.toLowerCase().startsWith("http://")
        || configuredBaseUri.toLowerCase().startsWith("https://"))) {
      return configuredBaseUri;
    }
    return getBaseUriFromRequestContext()
        .orElseThrow(() -> new IllegalStateException(
            "Getting base uri from request context failed."));
  }

  private static Optional<String> getBaseUriFromRequestContext() {
    return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
        .filter(ServletRequestAttributes.class::isInstance)
        .map(ServletRequestAttributes.class::cast)
        .map(ServletRequestAttributes::getRequest)
        .map(AbstractController::getBaseUri);
  }

  private static String getBaseUri(HttpServletRequest request) {
    StringBuilder baseUriBuilder = new StringBuilder(getScheme(request));
    baseUriBuilder.append("://");
    baseUriBuilder.append(getServerName(request));
    int port = getPort(request);
    if (!(port == 80 || port == 443)) {
      baseUriBuilder.append(':').append(port);
    }
    baseUriBuilder.append(request.getContextPath());
    return baseUriBuilder.toString();
  }

  private static String getScheme(HttpServletRequest request) {
    String value = null;
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      if ("X-Forwarded-Proto".equalsIgnoreCase(headerName)) {
        value = request.getHeader(headerName);
        break;
      }
    }
    if (isEmpty(value)) {
      value = request.getScheme();
    }
    if ("http".equalsIgnoreCase(value) || "https".equalsIgnoreCase(value)) {
      return value;
    }
    return "http";
  }

  private static String getServerName(HttpServletRequest request) {
    return request.getServerName();
  }

  private static int getPort(HttpServletRequest request) {
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      if ("X-Forwarded-Port".equalsIgnoreCase(headerName)) {
        try {
          return Integer.parseInt(request.getHeader(headerName));
        } catch (RuntimeException ignored) {
          // ignored
        }
      }
    }
    return request.getServerPort();
  }

}
