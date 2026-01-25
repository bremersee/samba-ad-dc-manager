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

package org.bremersee.samba.ad.dc.controller.ui.shared;

import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * The current page provider.
 *
 * @author Christian Bremer
 */
public interface CurrentPageNameProvider {

  /**
   * Gets current page name.
   *
   * @return the current page name
   */
  String getCurrentPageName();

  /**
   * Add current page name to model.
   *
   * @return the current page name
   */
  @ModelAttribute("currentPage")
  default String addCurrentPageName() {
    return getCurrentPageName();
  }

}
