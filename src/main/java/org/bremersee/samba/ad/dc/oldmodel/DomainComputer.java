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
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type DomainComputer.
 *
 * @author Christian Bremer
 */
@Schema(description = "Domain computer.")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DomainComputer extends SamAccount {

  @Serial
  private static final long serialVersionUID = 1L;

  private String name;

  private String dnsHostName;

  private List<String> networkAddresses;

  private String operatingSystem;

  private String operatingSystemVersion;

  private String description;

  private List<String> servicePrincipalNames;

  public DomainComputer() {
    super();
  }

  @Override
  public String getName() {
    return name;
  }

  public List<String> getNetworkAddresses() {
    if (isNull(networkAddresses)) {
      networkAddresses = new ArrayList<>();
    }
    return networkAddresses;
  }

  public List<String> getServicePrincipalNames() {
    if (isNull(servicePrincipalNames)) {
      servicePrincipalNames = new ArrayList<>();
    }
    return servicePrincipalNames;
  }

  @Hidden
  @JsonIgnore
  public String getSamAccountNameWithoutTrailingDollarSign() {
    String tmpName = getSamAccountName();
    if (isNull(tmpName) || tmpName.isEmpty()) {
      return null;
    }
    return tmpName.substring(0, tmpName.length() - 1);
  }
}
