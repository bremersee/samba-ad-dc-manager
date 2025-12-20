/*
 * Copyright 2025 the original author or authors.
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

package org.bremersee.samba.ad.dc.common.controller.ui.shared;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.samskivert.mustache.Mustache;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bremersee.ldaptive.converter.StringToDnConverter;
import org.bremersee.samba.ad.dc.common.controller.AbstractController;
import org.bremersee.samba.ad.dc.common.model.TreeSearchScope;
import org.ldaptive.dn.Dn;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * The interface RedirectComponent.
 *
 * @author Christian Bremer
 */
public interface RedirectComponent extends LoggerProvider {

  String PAGE_PARAMS = AbstractController.PAGE + "={{" + AbstractController.PAGE + "}}"
      + "&" + AbstractController.SIZE + "={{" + AbstractController.SIZE + "}}"
      + "&" + AbstractController.SORT + "={{" + AbstractController.SORT + "}}"
      + "&" + AbstractController.QUERY + "={{" + AbstractController.QUERY + "}}";

  String PAGE_AND_OU_PARAMS = PAGE_PARAMS
      + "&" + AbstractController.OU + "={{" + AbstractController.OU + "}}"
      + "&" + AbstractController.SCOPE + "={{" + AbstractController.SCOPE + "}}";

  String PAGE_AND_ZONE_TYPE_PARAMS = PAGE_PARAMS
      + "&" + AbstractController.ZONE_TYPE + "={{" + AbstractController.ZONE_TYPE + "}}";

  String PAGE_AND_ZONE_NAME_PARAMS = PAGE_AND_ZONE_TYPE_PARAMS
      + "&" + AbstractController.ZONE_NAME + "={{" + AbstractController.ZONE_NAME + "}}";

  String PAGE_AND_DNS_ENTRY_PARAMS = PAGE_AND_ZONE_NAME_PARAMS
      + "&name={{name}}"
      + "&type={{type}}"
      + "&value={{value}}";

  default Map<String, Object> getParamterMap(Dn ou) {
    return Map.of(
        AbstractController.PAGE, findPageParameterValue(),
        AbstractController.SIZE, findSizeParameterValue(),
        AbstractController.SORT, findParameterValue(AbstractController.SORT).orElse(""),
        AbstractController.QUERY, findParameterValue(AbstractController.QUERY).orElse(""),
        AbstractController.OU, Optional.ofNullable(ou)
            .or(this::findOuParameterValue)
            .filter(dn -> !dn.isEmpty())
            .map(Dn::format)
            .orElse(""),
        AbstractController.SCOPE, findScopeParameterValue()
            .map(TreeSearchScope::getParameterValue)
            .orElse(""),
        AbstractController.ZONE_TYPE, findZoneTypeParameterValue().orElse(""),
        AbstractController.ZONE_NAME, findParameterValue(AbstractController.ZONE_NAME)
            .orElse("")
    );
  }

  default Map<String, Object> getParamterMap() {
    return getParamterMap(null);
  }

  default Map<String, Object> putToParameterMap(Map<String, Object> map, String key, Object value) {
    Map<String, Object> result = new HashMap<>(map);
    result.put(key, value);
    return result;
  }

  default Optional<HttpServletRequest> findHttpServletRequest() {
    return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
        .filter(ServletRequestAttributes.class::isInstance)
        .map(ServletRequestAttributes.class::cast)
        .map(ServletRequestAttributes::getRequest);
  }

  default Optional<String> findParameterValue(String parameterName) {
    return findHttpServletRequest()
        .map(req -> req.getParameter(parameterName));
  }

  default int findPageParameterValue() {
    return findParameterValue(AbstractController.PAGE)
        .map(value -> {
          try {
            return Integer.parseInt(value);
          } catch (NumberFormatException e) {
            return null;
          }
        })
        .filter(value -> value >= 0)
        .orElse(AbstractController.PAGE_DEFAULT_INT);
  }

  default int findSizeParameterValue() {
    return findParameterValue(AbstractController.SIZE)
        .map(value -> {
          try {
            return Integer.parseInt(value);
          } catch (NumberFormatException e) {
            return null;
          }
        })
        .filter(value -> value >= 1)
        .orElse(AbstractController.SIZE_DEFAULT_INT);
  }

  default Optional<Dn> findOuParameterValue() {
    return findParameterValue(AbstractController.OU)
        .map(dn -> new StringToDnConverter().convert(dn));
  }

  default Optional<TreeSearchScope> findScopeParameterValue() {
    return findParameterValue(AbstractController.SCOPE)
        .map(TreeSearchScope::fromValue);
  }

  default Optional<String> findZoneTypeParameterValue() {
    return findParameterValue(AbstractController.ZONE_TYPE);
  }

  default String getRedirectUri(
      @NotEmpty String path,
      @Nullable String mustacheTemplate,
      @Nullable Map<String, Object> parameters) {

    String template = mustacheTemplate;
    StringBuilder sb = new StringBuilder();
    if (!path.toLowerCase().startsWith("redirect:")) {
      sb.append("redirect:");
    }
    sb.append(path);
    if (!isEmpty(template)) {
      if (template.startsWith("?") || template.startsWith("&")) {
        template = template.substring(1);
      }
      if (path.contains("?")) {
        sb.append("&");
      } else {
        sb.append("?");
      }
      sb.append(template);
    }
    template = sb.toString();
    String redirect = Mustache
        .compiler()
        .withEscaper(raw -> URLEncoder.encode(raw, StandardCharsets.UTF_8))
        .defaultValue("")
        .compile(template)
        .execute(Objects.requireNonNullElseGet(parameters, Map::of));
    getLogger().debug("Redirect URI: {}", redirect);
    return redirect;
  }

}
