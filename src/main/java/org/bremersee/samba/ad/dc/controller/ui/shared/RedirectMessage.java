/*
 * Copyright 2019-2026 the original author or authors.
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

import static java.util.Objects.requireNonNullElse;

import java.io.Serial;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * The redirect message.
 *
 * @author Christian Bremer
 */
@Getter
@ToString
@EqualsAndHashCode
public class RedirectMessage implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The constant ATTRIBUTE_NAME.
   */
  public static final String ATTRIBUTE_NAME = "rmsg";

  /**
   * The message type.
   */
  private final RedirectMessageType msgType;

  /**
   * The message.
   */
  private final String msg;

  /**
   * Instantiates a new redirect message.
   */
  public RedirectMessage() {
    this(null);
  }

  /**
   * Instantiates a new redirect message.
   *
   * @param msg the msg
   */
  public RedirectMessage(String msg) {
    this(msg, null);
  }

  /**
   * Instantiates a new redirect message.
   *
   * @param msg the msg
   * @param msgType the msg type
   */
  public RedirectMessage(String msg, RedirectMessageType msgType) {
    this.msgType = requireNonNullElse(msgType, RedirectMessageType.INFO);
    this.msg = requireNonNullElse(msg, "");
  }
}
