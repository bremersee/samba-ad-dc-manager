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

/**
 * The odel constants.
 *
 * @author Christian Bremer
 */
public class ModelConstants {

  /**
   * The constant DISCRIMINATOR.
   */
  public static final String DISCRIMINATOR = "_json_type";

  /**
   * The constant AD_ENTRY.
   */
  public static final String AD_ENTRY = "AdEntry";

  /**
   * The constant DNS_ENTRY.
   */
  public static final String DNS_ENTRY = "DnsEntry";

  /**
   * The constant DNS_ZONE.
   */
  public static final String DNS_ZONE = "DnsZone";

  /**
   * The constant COMPUTER.
   */
  public static final String COMPUTER = "DomainComputer";

  /**
   * The constant GROUP.
   */
  public static final String GROUP = "DomainGroup";

  /**
   * The constant GROUP_MEMBER.
   */
  public static final String GROUP_MEMBER = "DomainGroupMember";

  /**
   * The constant USER.
   */
  public static final String USER = "DomainUser";

  /**
   * The constant ORGANIZATIONAL_UNIT.
   */
  public static final String ORGANIZATIONAL_UNIT = "OrganizationalUnit";

  /**
   * The constant SAM_ACCOUNT.
   */
  public static final String SAM_ACCOUNT = "SamAccount";

  private ModelConstants() {
    super();
  }
}
