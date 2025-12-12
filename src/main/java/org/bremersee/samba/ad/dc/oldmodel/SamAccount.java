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

package org.bremersee.samba.ad.dc.oldmodel;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.Hidden;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The interface SamAccount.
 *
 * @author Christian Bremer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SamAccount extends AdEntry implements NameProvider {

  @Serial
  private static final long serialVersionUID = 1L;

  private String samAccountName;

  private Sid sid;

  private boolean criticalSystemObject;

  private Integer primaryGroupId;

  private List<String> memberships;

  public SamAccount() {
    super();
  }

  /**
   * User's group memberships.
   *
   * @return the group memberships
   */
  public List<String> getMemberships() {
    if (isNull(memberships)) {
      memberships = new ArrayList<>();
    }
    return memberships;
  }

  @Hidden
  @JsonIgnore
  @Override
  public String getName() {
    return getSamAccountName();
  }

}
