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

package org.bremersee.samba.ad.dc.misc;

import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.ldaptive.dn.Dn;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * The default dn tool.
 *
 * @author Christian Bremer
 */
@Component
@Validated
public class DefaultDnTool implements DnTool {

  private final ApplicationProperties properties;

  /**
   * Instantiates a new default dn tool.
   *
   * @param properties the properties
   */
  public DefaultDnTool(ApplicationProperties properties) {
    this.properties = properties;
  }

  @Override
  public Dn getBaseDn() {
    return new Dn(properties.getBaseDn());
  }

}
