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

package org.bremersee.samba.ad.dc.config;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * The domain properties.
 *
 * @author Christian Bremer
 */
@Data
public class DomainProperties implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String hostName;

  private int maximumPasswordLength = 75;

  private String simplePasswordRegexTemplate = "^(?=.{%d,%d}$).*";

  private String complexPasswordRegexTemplate = "(?=^.{%d,%d}$)"
      + "((?=.*\\d)(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[^A-Za-z0-9])(?=.*[a-z])"
      + "|(?=.*[^A-Za-z0-9])(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[A-Z])(?=.*[^A-Za-z0-9]))^.*";

  private List<String> forbiddenNewSamAccountNames = new ArrayList<>();

  private String newSamAccountNameRegex = "^(?![._-])[a-zA-Z0-9._-]{3,75}$";

  private String defaultNisDomain;

  /**
   * Instantiates new domain properties.
   */
  public DomainProperties() {
    super();
  }
}
