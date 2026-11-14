/*
 * Copyright 2026 the original author or authors.
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
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;

/**
 * The domain user identifier.
 *
 * @author Christian Bremer
 */
@Schema(enumAsRef = true)
public enum DomainUserIdentifier {

  /**
   * Sam account name domain user identifier.
   */
  SAM_ACCOUNT_NAME("name"),

  /**
   * Principal domain user identifier.
   */
  PRINCIPAL("principal"),

  /**
   * Uid domain user identifier.
   */
  UID("uid"),

  /**
   * Uid number domain user identifier.
   */
  UID_NUMBER("uid-number");

  private final String parameterValue;

  DomainUserIdentifier(String parameterValue) {
    this.parameterValue = parameterValue;
  }

  /**
   * Gets parameter value.
   *
   * @return the parameter value
   */
  @JsonValue
  public String getParameterValue() {
    return parameterValue;
  }

  /**
   * From parameter value domain user identifier.
   *
   * @param parameterValue the parameter value
   * @return the domain user identifier
   */
  @JsonCreator
  public static DomainUserIdentifier fromParameterValue(String parameterValue) {

    return Arrays.stream(values())
        .filter(identifier -> identifier.getParameterValue()
            .equalsIgnoreCase(parameterValue) || identifier.name().equalsIgnoreCase(parameterValue))
        .findFirst()
        .orElse(SAM_ACCOUNT_NAME);
  }
}
