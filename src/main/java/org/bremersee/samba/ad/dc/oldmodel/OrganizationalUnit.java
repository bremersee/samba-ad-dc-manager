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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.RDn;
import org.springframework.lang.NonNull;

/**
 * The type OrganisationUnit.
 *
 * @author Christian Bremer
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class OrganizationalUnit extends AdEntry
    implements Comparable<OrganizationalUnit> {

  private String description;

  private String name;

  private boolean systemOu;

  public OrganizationalUnit() {
    super();
  }

  @Hidden
  @JsonIgnore
  @Override
  public String getNameTree() {
    String nameTree = Stream.ofNullable(getDn())
        .map(Dn::getRDns)
        .flatMap(rdnList -> {
          List<RDn> rdns = new ArrayList<>(getDn().getRDns());
          Collections.reverse(rdns);
          return rdns.stream();
        })
        .filter(rdn -> !rdn.getNameValue().hasName("dc"))
        .map(rdn -> rdn.getNameValue().getStringValue())
        .collect(Collectors.joining(" → "));
    if (nameTree.isEmpty()) {
      return getName();
    }
    return nameTree;
  }

  @Override
  public int compareTo(@NonNull OrganizationalUnit o) {
    String s0 = Objects.requireNonNullElse(getDistinguishedName(), "");
    String s1 = Objects.requireNonNullElse(o.getDistinguishedName(), "");
    return s0.compareToIgnoreCase(s1);
  }
}
