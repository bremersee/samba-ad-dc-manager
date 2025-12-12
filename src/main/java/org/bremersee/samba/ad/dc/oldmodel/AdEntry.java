/*
 * Copyright 2019-2020 the original author or authors.
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
import static java.util.Objects.nonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.ldaptive.dn.DefaultAttributeValueEscaper;
import org.ldaptive.dn.DefaultRDnNormalizer;
import org.ldaptive.dn.Dn;

/**
 * The active directory base entry.
 *
 * @author Christian Bremer
 */
@Schema(description = "Active directory base entry.")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString(exclude = {"dn"})
@EqualsAndHashCode(exclude = {"dn"})
public class AdEntry implements Serializable, DistinguishedNameProvider {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The distinguished name in the active directory.
   */
  @Hidden
  @JsonIgnore
  transient Dn dn;

  /**
   * The distinguished name in the active directory.
   */
  @Schema(description = "The distinguished name.")
  String distinguishedName;

  /**
   * The creation date.
   */
  @Schema(description = "The creation date.")
  OffsetDateTime created;

  /**
   * The last modification date.
   */
  @Schema(description = "The last modification date.")
  OffsetDateTime modified;

  /**
   * Instantiates a new active directory base entry.
   */
  public AdEntry() {
    super();
  }

  /**
   * Instantiates a new common attributes.
   *
   * @param distinguishedName the distinguished name
   * @param created the created
   * @param modified the modified
   */
  public AdEntry(
      String distinguishedName,
      OffsetDateTime created,
      OffsetDateTime modified) {

    setDistinguishedName(distinguishedName);
    setCreated(created);
    setModified(modified);
  }

  /**
   * Gets dn.
   *
   * @return the dn
   */
  @Hidden
  @JsonIgnore
  public Dn getDn() {
    if (isNull(dn) && nonNull(distinguishedName)) {
      dn = new Dn(distinguishedName);
    }
    return dn;
  }

  /**
   * Sets dn.
   *
   * @param dn the dn
   */
  @Hidden
  @JsonIgnore
  public void setDn(Dn dn) {
    if (isNull(dn) || dn.isEmpty()) {
      this.dn = null;
      this.distinguishedName = null;
    } else {
      this.dn = dn;
      this.distinguishedName = dn.format();
    }
  }

  @Override
  public String getDistinguishedName() {
    return isNull(getDn()) ? null : getDn().format();
  }

  /**
   * Sets distinguished name.
   *
   * @param distinguishedName the distinguished name
   */
  public void setDistinguishedName(String distinguishedName) {
    if (isNull(distinguishedName) || distinguishedName.isEmpty()) {
      this.dn = null;
      this.distinguishedName = null;
    } else {
      setDn(new Dn(distinguishedName));
    }
  }

  /**
   * Gets distinguished name unformatted.
   *
   * @return the distinguished name unformatted
   */
  @Hidden
  @JsonIgnore
  public String getDistinguishedNameUnformatted() {
    return isNull(getDn()) ? null : getDn()
        .format(new DefaultRDnNormalizer(
            new DefaultAttributeValueEscaper(),
            name -> name,
            value -> value));
  }

  /**
   * Gets parent distinguished name.
   *
   * @return the parent distinguished name
   */
  @Hidden
  @JsonIgnore
  public String getParentDistinguishedName() {
    return Optional.ofNullable(getDn())
        .map(Dn::getParent)
        .map(Dn::format)
        .orElse(null);
  }

  /**
   * Gets name tree.
   *
   * @return the name tree
   */
  @Hidden
  @JsonIgnore
  public String getNameTree() { // ou is reverse
    return Stream.ofNullable(getDn())
        .map(Dn::getRDns)
        .flatMap(Collection::stream)
        .filter(rdn -> !rdn.getNameValue().hasName("dc"))
        .map(rdn -> rdn.getNameValue().getStringValue())
        .collect(Collectors.joining(" → "));
  }

}

