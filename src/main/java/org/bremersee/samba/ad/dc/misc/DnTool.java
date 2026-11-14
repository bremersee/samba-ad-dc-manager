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

package org.bremersee.samba.ad.dc.misc;

import static org.springframework.util.ObjectUtils.isEmpty;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.ldaptive.dn.DefaultAttributeValueEscaper;
import org.ldaptive.dn.DefaultRDnNormalizer;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.RDnNormalizer;

/**
 * The dn tool.
 *
 * @author Christian Bremer
 */
public interface DnTool {

  /**
   * The constant CASE_SENSITIVE_RDN_NORMALIZER.
   */
  RDnNormalizer CASE_SENSITIVE_RDN_NORMALIZER = new DefaultRDnNormalizer(
      new DefaultAttributeValueEscaper(), name -> name, value -> value);

  /**
   * To string.
   *
   * @param dn the dn
   * @return the string
   */
  static String toString(Dn dn) {
    return Optional.ofNullable(dn)
        .map(d -> d.format(CASE_SENSITIVE_RDN_NORMALIZER))
        .orElse("null");
  }

  /**
   * Is valid dn boolean.
   *
   * @param dn the dn
   * @return the boolean
   */
  static boolean isValidDn(String dn) {
    try {
      return Optional.ofNullable(dn)
          .filter(v -> !isEmpty(v))
          .map(Dn::new)
          .filter(v -> !isEmpty(v))
          .isPresent();
    } catch (RuntimeException ignored) {
      return false;
    }
  }

  /**
   * Is valid dn boolean.
   *
   * @param dn the dn
   * @return the boolean
   */
  static boolean isValidDn(Dn dn) {
    return Optional.ofNullable(dn)
        .filter(v -> !isEmpty(v))
        .isPresent();
  }

  /**
   * Is valid dn with base dn boolean.
   *
   * @param dn the dn
   * @return the boolean
   */
  default boolean isValidDnWithBaseDn(String dn) {
    if (isValidDn(dn)) {
      return isValidDnWithBaseDn(new Dn(dn));
    }
    return false;
  }

  /**
   * Is valid dn with base dn boolean.
   *
   * @param dn the dn
   * @return the boolean
   */
  default boolean isValidDnWithBaseDn(Dn dn) {
    return isValidDn(dn) && (dn.isSame(getBaseDn()) || getBaseDn().isAncestor(dn));
  }

  /**
   * Is same dn boolean.
   *
   * @param dn1 the dn 1
   * @param dn2 the dn 2
   * @return the boolean
   */
  static boolean isSameDn(String dn1, String dn2) {
    if (!isValidDn(dn1) || !isValidDn(dn2)) {
      return false;
    }
    return isSameDn(new Dn(dn1), new Dn(dn2));
  }

  /**
   * Is same dn boolean.
   *
   * @param dn1 the dn 1
   * @param dn2 the dn 2
   * @return the boolean
   */
  static boolean isSameDn(Dn dn1, String dn2) {
    if (!isValidDn(dn1) || !isValidDn(dn2)) {
      return false;
    }
    return isSameDn(dn1, new Dn(dn2));
  }

  /**
   * Is same dn boolean.
   *
   * @param dn1 the dn 1
   * @param dn2 the dn 2
   * @return the boolean
   */
  static boolean isSameDn(Dn dn1, Dn dn2) {
    if (!isValidDn(dn1) || !isValidDn(dn2)) {
      return false;
    }
    return dn1.format().equalsIgnoreCase(dn2.format());
  }

  /**
   * Gets base dn.
   *
   * @return the base dn
   */
  @NotNull
  Dn getBaseDn();

  /**
   * Add base dn.
   *
   * @param dn the dn
   * @return the dn
   */
  @NotNull
  default Dn addBaseDn(String dn) {
    if (!isValidDn(dn)) {
      return getBaseDn();
    }
    return addBaseDn(new Dn(dn));
  }

  /**
   * Add base dn.
   *
   * @param dn the dn
   * @return the dn
   */
  @NotNull
  default Dn addBaseDn(Dn dn) {
    if (!isValidDn(dn)) {
      return getBaseDn();
    }
    if (isValidDnWithBaseDn(dn)) {
      return dn;
    }
    Dn newDn = new Dn(dn.getRDns());
    newDn.add(getBaseDn());
    return newDn;
  }

  /**
   * Remove base dn.
   *
   * @param dn the dn
   * @return the dn
   */
  @Nullable
  default Dn removeBaseDn(Dn dn) {
    Dn adBaseDn = getBaseDn();
    if (!isValidDn(dn) || dn.isSame(adBaseDn)) {
      return null;
    }
    if (adBaseDn.isAncestor(dn)) {
      return dn.subDn(0, dn.size() - adBaseDn.size());
    }
    return dn;
  }

  /**
   * Remove ancestor dn.
   *
   * @param dn the dn
   * @param ancestor the ancestor
   * @return the dn
   */
  static Dn removeAncestor(Dn dn, Dn ancestor) {
    if (!isValidDn(dn) || !isValidDn(ancestor)) {
      return dn;
    }
    if (ancestor.isSame(dn)) {
      return new Dn("");
    }
    if (ancestor.isAncestor(dn)) {
      return dn.subDn(0, dn.size() - ancestor.size());
    }
    return dn;
  }

  /**
   * Replace ancestor dn.
   *
   * @param dn the dn
   * @param ancestor the ancestor
   * @param newAncestor the new ancestor
   * @return the dn
   */
  static Dn replaceAncestor(Dn dn, Dn ancestor, Dn newAncestor) {
    Dn newDn = removeAncestor(dn, ancestor);
    if (isValidDn(newAncestor) && !dn.isSame(newDn)) {
      newDn.add(newAncestor);
    }
    return newDn;
  }

  /**
   * Gets dn pair.
   *
   * @param dn the dn
   * @return the dn pair
   */
  static DnPair getDnPair(String dn) {
    if (isValidDn(dn)) {
      return getDnPair(new Dn(dn));
    }
    return null;
  }

  /**
   * Gets dn pair.
   *
   * @param dn the dn
   * @return the dn pair
   */
  static DnPair getDnPair(Dn dn) {
    if (isValidDn(dn)) {
      return new DnPair(dn.format(CASE_SENSITIVE_RDN_NORMALIZER), dn.format());
    }
    return null;
  }

  /**
   * The dn pair.
   *
   * @param dn the dn
   * @param normalizedDn the normalized dn
   *
   * @author Christian Bremer
   */
  record DnPair(String dn, String normalizedDn) {

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      DnPair dnPair = (DnPair) o;
      return Objects.equals(normalizedDn, dnPair.normalizedDn);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(normalizedDn);
    }

    @NonNull
    @Override
    public String toString() {
      return "DnPair{"
          + "dn='" + dn + '\''
          + ", normalizedDn='" + normalizedDn + '\''
          + '}';
    }
  }

}
