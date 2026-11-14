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
 * The domain group identifier.
 *
 * @author Christian Bremer
 */
@Schema(enumAsRef = true)
public enum DomainGroupIdentifier {

  /**
   * Sam account name domain group identifier.
   */
  SAM_ACCOUNT_NAME("name"),

  /**
   * Primary group id domain group identifier.
   */
  PRIMARY_GROUP_ID("group-id"),

  /**
   * Unix group id domain group identifier.
   */
  UNIX_GROUP_ID("unix-group-id");

  private final String parameterValue;

  DomainGroupIdentifier(String parameterValue) {
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
   * From parameter value domain group identifier.
   *
   * @param parameterValue the parameter value
   * @return the domain group identifier
   */
  @JsonCreator
  public static DomainGroupIdentifier fromParameterValue(String parameterValue) {

    return Arrays.stream(values())
        .filter(identifier -> identifier.getParameterValue()
            .equalsIgnoreCase(parameterValue) || identifier.name().equalsIgnoreCase(parameterValue))
        .findFirst()
        .orElse(SAM_ACCOUNT_NAME);
  }
}
