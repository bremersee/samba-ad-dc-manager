package org.bremersee.samba.ad.dc.misc;

import static org.springframework.util.ObjectUtils.isEmpty;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import org.ldaptive.dn.DefaultAttributeValueEscaper;
import org.ldaptive.dn.DefaultRDnNormalizer;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.RDnNormalizer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface DnTool {

  RDnNormalizer CASE_SENSITIVE_RDN_NORMALIZER = new DefaultRDnNormalizer(
      new DefaultAttributeValueEscaper(), name -> name, value -> value);

  static String toString(Dn dn) {
    return Optional.ofNullable(dn)
        .map(d -> d.format(CASE_SENSITIVE_RDN_NORMALIZER))
        .orElse("null");
  }

  static boolean isValidDn(String dn) {
    try {
      return Optional.ofNullable(dn)
          .filter(v -> !isEmpty(v))
          .map(Dn::new)
          .filter(v -> !isEmpty(v))
          .isPresent();
    } catch (RuntimeException e) {
      return false;
    }
  }

  static boolean isValidDn(Dn dn) {
    return Optional.ofNullable(dn)
        .filter(v -> !isEmpty(v))
        .isPresent();
  }

  default boolean isValidDnWithBaseDn(String dn) {
    if (isValidDn(dn)) {
      return isValidDnWithBaseDn(new Dn(dn));
    }
    return false;
  }

  default boolean isValidDnWithBaseDn(Dn dn) {
    return isValidDn(dn) && (dn.isSame(getBaseDn()) || getBaseDn().isAncestor(dn));
  }

  static boolean isSameDn(String dn1, String dn2) {
    if (!isValidDn(dn1) || !isValidDn(dn2)) {
      return false;
    }
    return isSameDn(new Dn(dn1), new Dn(dn2));
  }

  static boolean isSameDn(Dn dn1, String dn2) {
    if (!isValidDn(dn1) || !isValidDn(dn2)) {
      return false;
    }
    return isSameDn(dn1, new Dn(dn2));
  }

  static boolean isSameDn(Dn dn1, Dn dn2) {
    if (!isValidDn(dn1) || !isValidDn(dn2)) {
      return false;
    }
    return dn1.format().equalsIgnoreCase(dn2.format());
  }

  @NotNull
  Dn getBaseDn();

  @NotNull
  default Dn addBaseDn(String dn) {
    if (!isValidDn(dn)) {
      return getBaseDn();
    }
    return addBaseDn(new Dn(dn));
  }

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

  @Nullable
  default Dn removeBaseDn(String dn) {
    if (!isValidDn(dn)) {
      return null;
    }
    return removeBaseDn(new Dn(dn));
  }

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

  static Dn replaceAncestor(Dn dn, Dn ancestor, Dn newAncestor) {
    Dn newDn = removeAncestor(dn, ancestor);
    if (isValidDn(newAncestor) && !dn.isSame(newDn)) {
      newDn.add(newAncestor);
    }
    return newDn;
  }

  static DnPair getDnPair(String dn) {
    if (isValidDn(dn)) {
      return getDnPair(new Dn(dn));
    }
    return null;
  }

  static DnPair getDnPair(Dn dn) {
    if (isValidDn(dn)) {
      return new DnPair(dn.format(CASE_SENSITIVE_RDN_NORMALIZER), dn.format());
    }
    return null;
  }

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
