package org.bremersee.samba.ad.dc.controller;

import static org.springframework.util.ObjectUtils.isEmpty;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public abstract class AbstractController {

  public static final String PAGE = "page";

  public static final int PAGE_DEFAULT_INT = 0;

  public static final String PAGE_DEFAULT = "" + PAGE_DEFAULT_INT;

  public static final String SIZE = "size";

  public static final int SIZE_DEFAULT_INT = 20;

  public static final String SIZE_DEFAULT = "" + SIZE_DEFAULT_INT;

  public static final String SORT = "sort";

  public static final String COMPUTER_SORT = "name";

  public static final String DHCP_LEASE_SORT = "ip";

  public static final String DNS_ENTRY_SORT = "name;type;value";

  public static final String GROUP_SORT = "samAccountName";

  public static final String OU_SORT = "nameTree";

  public static final String USER_SORT = "lastName;firstName;samAccountName";

  public static final String QUERY = "q";

  public static final String QUERY_DEFAULT = "";

  public static final String OU = "ou";

  public static final String SCOPE = "scope";

  public static final String ZONE_NAME = "zone-name";

  public static final String ZONE_TYPE = "zone-type";

  public static final String ZONE_TYPE_DEFAULT = "primary";

  public static final String DNS_ENTRY_NAME = "name";

  public static final String DNS_ENTRY_TYPE = "type";

  public static final String DNS_ENTRY_VALUE = "value";

  protected AbstractController() {
    super();
  }

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
