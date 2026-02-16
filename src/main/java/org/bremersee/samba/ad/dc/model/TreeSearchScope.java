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

package org.bremersee.samba.ad.dc.model;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * The enum TreeSearchScope.
 *
 * @author Christian Bremer
 */
@Getter
public enum TreeSearchScope implements Translatable {

  ONELEVEL("one-level", "search-scope.one-level.label", "One Level"),

  SUBTREE("subtree", "search-scope.subtree.label", "Subtree");

  private final String parameterValue;

  private final String i18nCode;

  private final String defaultDisplayName;

  TreeSearchScope(String parameterValue, String i18nCode, String defaultDisplayName) {
    this.parameterValue = parameterValue;
    this.i18nCode = i18nCode;
    this.defaultDisplayName = defaultDisplayName;
  }

  @JsonValue
  public String getParameterValue() {
    return parameterValue;
  }

  @JsonCreator
  public static TreeSearchScope fromValue(String value) {
    if (isNull(value) || value.isEmpty()) {
      return null;
    }
    for (TreeSearchScope scope : values()) {
      if (scope.parameterValue.equalsIgnoreCase(value) || scope.name().equalsIgnoreCase(value)) {
        return scope;
      }
    }
    return null;
  }

  @Override
  public String getDefaultTranslation(Object... args) {
    return getDefaultDisplayName();
  }

}
