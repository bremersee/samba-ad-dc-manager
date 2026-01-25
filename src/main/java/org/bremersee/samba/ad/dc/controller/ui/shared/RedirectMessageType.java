/*
 * Copyright 2016 the original author or authors.
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

import lombok.Getter;

/**
 * The redirect message type.
 *
 * @author Christian Bremer
 */
public enum RedirectMessageType {

  /**
   * The Primary.
   */
  PRIMARY("alert alert-primary"),

  /**
   * The Secondary.
   */
  SECONDARY("alert alert-secondary"),

  /**
   * The Success.
   */
  SUCCESS("alert alert-success"),

  /**
   * The Danger.
   */
  DANGER("alert alert-danger"),

  /**
   * The Warning.
   */
  WARNING("alert alert-warning"),

  /**
   * The Info.
   */
  INFO("alert alert-info"),

  /**
   * The Light.
   */
  LIGHT("alert alert-light"),

  /**
   * The Dark.
   */
  DARK("alert alert-dark");

  /**
   * The css class.
   */
  @Getter
  private final String cssClass;

  RedirectMessageType(final String cssClass) {
    this.cssClass = cssClass;
  }
}
