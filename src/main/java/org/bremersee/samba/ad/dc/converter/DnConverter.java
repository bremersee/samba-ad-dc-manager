package org.bremersee.samba.ad.dc.converter;

import static org.springframework.util.ObjectUtils.isEmpty;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import org.ldaptive.dn.DefaultAttributeValueEscaper;
import org.ldaptive.dn.DefaultRDnNormalizer;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.RDnNormalizer;
import org.springframework.lang.NonNull;

public interface DnConverter {

  RDnNormalizer CASE_SENSITIVE_RDN_NORMALIZER = new DefaultRDnNormalizer(
      new DefaultAttributeValueEscaper(), name -> name, value -> value);

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

  boolean isValidDnWithBaseDn(String dn);

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

  default boolean isDnWithBaseDn(String dn) {
    if (!isValidDn(dn)) {
      return false;
    }
    return isDnWithBaseDn(new Dn(dn));
  }

  default boolean isDnWithBaseDn(Dn dn) {
    if (!isValidDn(dn)) {
      return false;
    }
    return getBaseDn().isAncestor(dn);
  }

  default Dn addBaseDn(Dn dn) {
    // TODO
    return dn;
  }

  Dn removeBaseDn(Dn dn);

  @NotNull
  Dn getBaseDn();

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
