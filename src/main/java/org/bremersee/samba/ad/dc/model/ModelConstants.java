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

public class ModelConstants {

  public static final String DISCRIMINATOR = "_json_type";

  public static final String AD_ENTRY = "AdEntry";

  public static final String DNS_ENTRY = "DnsEntry"; //.class.getSimpleName(); // " dns-entry";

  public static final String DNS_ZONE = "DnsZone"; //.class.getSimpleName(); // "dns-zone";

  public static final String COMPUTER = "DomainComputer"; //.class.getSimpleName(); // "computer";

  public static final String GROUP = "DomainGroup"; //.class.getSimpleName(); // "group";

  public static final String GROUP_MEMBER = "DomainGroupMember"; //.class.getSimpleName(); // "group-member";

  public static final String USER = "DomainUser"; //.class.getSimpleName(); // "user";

  public static final String ORGANIZATIONAL_UNIT = "OrganizationalUnit"; //.class.getSimpleName(); // "organizational-unit";

  public static final String SAM_ACCOUNT = "SamAccount"; //.class.getSimpleName(); // "sam-account";

  private ModelConstants() {
    super();
  }
}
