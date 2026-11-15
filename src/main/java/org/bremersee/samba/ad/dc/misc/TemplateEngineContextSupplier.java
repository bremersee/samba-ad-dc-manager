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

import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * The template engine context supplier.
 *
 * @author Christian Bremer
 */
public interface TemplateEngineContextSupplier {

  /**
   * Gets template engine context.
   *
   * @return the template engine context
   */
  @NotNull
  Map<String, Object> getTemplateEngineContext();

}
