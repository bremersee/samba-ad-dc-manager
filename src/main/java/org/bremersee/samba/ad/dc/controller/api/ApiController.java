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

package org.bremersee.samba.ad.dc.controller.api;

import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.controller.AbstractController;

/**
 * The abstract api controller.
 */
public abstract class ApiController extends AbstractController {

  /**
   * Instantiates a new api controller.
   */
  protected ApiController() {
    super();
  }

  /**
   * Gets as number.
   *
   * @param value the value
   * @return the as number
   */
  static int getAsNumber(String value) {
    try {
      return Integer.parseInt(value);
    } catch (RuntimeException ignored) {
      throw ServiceException
          .badRequest(String.format("Value '%s' is not a number.", value));
    }
  }

}
