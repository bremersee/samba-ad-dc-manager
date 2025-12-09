/*
 * Copyright 2024 the original author or authors.
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

import static java.util.Objects.requireNonNull;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The interface SamAccount.
 *
 * @author Christian Bremer
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SamAccount extends AdEntry implements NameProvider {

  String samAccountName;

  Sid sid;

  boolean criticalSystemObject;

  Integer primaryGroupId;

  List<String> memberships;

  public SamAccount() {
    super();
  }

  public SamAccount(
      String distinguishedName,
      OffsetDateTime created,
      OffsetDateTime modified,
      String samAccountName,
      Sid sid,
      boolean criticalSystemObject,
      Integer primaryGroupId,
      List<String> memberships) {
    super(distinguishedName, created, modified);
    this.samAccountName = requireNonNull(samAccountName, "samAccountName is required.");
    this.criticalSystemObject = criticalSystemObject;
    this.sid = sid;
    this.primaryGroupId = primaryGroupId;
    this.memberships = memberships;
  }

  /**
   * User's group memberships.
   *
   * @return the group memberships
   */
  public List<String> getMemberships() {
    if (Objects.isNull(memberships)) {
      memberships = new ArrayList<>();
    }
    return memberships;
  }

  @Override
  public String getName() {
    return getSamAccountName();
  }

}
