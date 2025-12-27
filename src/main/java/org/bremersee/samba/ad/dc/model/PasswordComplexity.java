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

package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * The password complexity.
 *
 * @author Christian Bremer
 */
public enum PasswordComplexity {

  /**
   * On password complexity.
   */
  ON(
      "on",
      "Please enter a new password. It must be between 8 and 75 characters in length "
          + "and must include at least one upper case letter, one lower case letter, and one "
          + "numeric digit.",
      "password-complexity.on"),

  /**
   * Off password complexity.
   */
  OFF(
      "off",
      "Please enter a new password. It must be between 8 and 75 characters in length.",
      "password-complexity.off"),

  /**
   * Default password complexity.
   */
  DEFAULT(
      "default",
      "Please enter a new password. It must be between 8 and 75 characters in length "
          + "and must include at least one upper case letter, one lower case letter, and one "
          + "numeric digit.",
      "password-complexity.on");


  private final String value;

  @Getter
  private final String defaultDescription;

  @Getter
  private final String i18nCode;

  PasswordComplexity(String value, String defaultDescription, String i18nCode) {
    this.value = value;
    this.defaultDescription = defaultDescription;
    this.i18nCode = i18nCode;
  }

  @JsonValue
  @Override
  public String toString() {
    return value;
  }

  /**
   * From value password complexity.
   *
   * @param value the value
   * @return the password complexity
   */
  @JsonCreator
  public static PasswordComplexity fromValue(String value) {
    for (PasswordComplexity pc : PasswordComplexity.values()) {
      if (pc.value.equalsIgnoreCase(value)) {
        return pc;
      }
    }
    return ON;
  }
}
