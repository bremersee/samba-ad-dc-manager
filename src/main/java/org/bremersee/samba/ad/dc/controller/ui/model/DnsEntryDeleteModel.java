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

package org.bremersee.samba.ad.dc.controller.ui.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * The dns entry delete model.
 *
 * @author Christian Bremer
 */
@Data
public class DnsEntryDeleteModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The verification name.
   */
  private String verificationName;

  /**
   * Specifies whether to delete the reverse entry or not.
   */
  private boolean deleteReverseEntry;

  /**
   * Instantiates a new dns entry delete model.
   */
  public DnsEntryDeleteModel() {
    super();
  }
}
