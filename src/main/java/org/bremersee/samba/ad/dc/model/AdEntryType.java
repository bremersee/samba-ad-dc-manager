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

package org.bremersee.samba.ad.dc.model;

import lombok.Getter;
import org.springframework.core.Ordered;

/**
 * The ad entry type.
 */
public enum AdEntryType {

  /**
   * Ad entry type.
   */
  AD_ENTRY(Ordered.LOWEST_PRECEDENCE),

  /**
   * Dns entry ad entry type.
   */
  DNS_ENTRY(300),

  /**
   * Dns zone ad entry type.
   */
  DNS_ZONE(200),

  /**
   * Sam account ad entry type.
   */
  SAM_ACCOUNT(30),

  /**
   * Computer ad entry type.
   */
  COMPUTER(50),

  /**
   * Group ad entry type.
   */
  GROUP(20),

  /**
   * Group member ad entry type.
   */
  GROUP_MEMBER(100),

  /**
   * User ad entry type.
   */
  USER(40),

  /**
   * Organizational unit ad entry type.
   */
  ORGANIZATIONAL_UNIT(10);

  @Getter
  private final int sortOrder;

  AdEntryType(int sortOrder) {
    this.sortOrder = sortOrder;
  }

}
