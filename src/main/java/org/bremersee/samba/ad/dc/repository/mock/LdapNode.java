package org.bremersee.samba.ad.dc.repository.mock;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.dn.Dn;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.EqualityFilter;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.NotFilter;
import org.ldaptive.filter.OrFilter;
import org.ldaptive.filter.PresenceFilter;
import org.ldaptive.filter.SubstringFilter;

@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
class LdapNode extends LdapEntry {

  @Setter
  private LdapNode parent;

  private final List<LdapNode> children = new ArrayList<>();

  LdapNode(String baseDn) {
    setDn(baseDn);
    AdConstants.DN.setValue(this, new Dn(getDn()));
    OffsetDateTime now = OffsetDateTime.now();
    AdConstants.WHEN_CREATED.setValue(this, now);
    AdConstants.WHEN_CHANGED.setValue(this, now);
  }

  LdapNode(LdapEntry entry, LdapNode parent) {
    setDn(entry.getDn());
    addAttributes(entry.getAttributes());
    AdConstants.DN.setValue(this, new Dn(getDn()));
    OffsetDateTime now = OffsetDateTime.now();
    AdConstants.WHEN_CREATED.setValue(this, now);
    AdConstants.WHEN_CHANGED.setValue(this, now);
    if (!isEmpty(parent)) {
      parent.addChild(this);
    }
  }

  void addChild(LdapNode child) {
    if (!isEmpty(child) && DnTool.isValidDn(child.getDn()) && !containsChild(child)) {
      child.parent = this;
      children.add(child);
    }
  }

  void removeChild(LdapNode child) {
    if (!isEmpty(child) && getChildren()
        .removeIf(c -> DnTool.isSameDn(c.getDn(), child.getDn()))) {
      child.parent = null;
    }
  }

  boolean containsChild(LdapNode child) {
    if (isEmpty(child) || !DnTool.isValidDn(child.getDn())) {
      return false;
    }
    return children.stream()
        .anyMatch(ch -> DnTool.isSameDn(ch.getDn(), child.getDn()));
  }

  boolean matches(Filter filter) {
    if (isEmpty(filter)) {
      return true;
    }
    if (filter instanceof AndFilter af) {
      return af.getComponents().stream().allMatch(this::matches);
    }
    if (filter instanceof OrFilter or) {
      return or.getComponents().stream().anyMatch(this::matches);
    }
    if (filter instanceof NotFilter nf) {
      return !matches(nf.getComponent());
    }
    if (filter instanceof PresenceFilter pf) {
      String attrName = pf.getAttributeDesc();
      return !isEmpty(getAttribute(attrName));
    }
    if (filter instanceof EqualityFilter ef) {
      return matches(ef);
    }
    if (filter instanceof SubstringFilter sf) {
      return matches(sf);
    }
    // and so on, what we need
    return false;
  }

  private boolean matches(EqualityFilter ef) {
    String attrName = ef.getAttributeDesc();
    return Stream.ofNullable(getAttribute(attrName))
        .map(LdapAttribute::getStringValues)
        .flatMap(Collection::stream)
        .anyMatch(value -> value
            .equalsIgnoreCase(LdapUtils.utf8Encode(ef.getAssertionValue())));
  }

  private boolean matches(SubstringFilter sf) {
    Predicate<String> startsWith;
    if (isEmpty(sf.getSubInitial())) {
      startsWith = value -> true;
    } else {
      startsWith = value -> value.toLowerCase()
          .startsWith(LdapUtils.utf8Encode(sf.getSubInitial()).toLowerCase());
    }
    Predicate<String> endsWith;
    if (isEmpty(sf.getSubFinal())) {
      endsWith = value -> true;
    } else {
      endsWith = value -> value.toLowerCase()
          .endsWith(LdapUtils.utf8Encode(sf.getSubInitial()).toLowerCase());
    }
    List<String> subAnyList = Arrays.stream(sf.getSubAny())
        .filter(b -> !isEmpty(b))
        .map(b -> LdapUtils.utf8Encode(b).toLowerCase())
        .toList();
    Predicate<String> contains;
    if (isEmpty(subAnyList)) {
      contains = value -> true;
    } else {
      contains = value -> subAnyList.stream()
          .allMatch(v -> value.toLowerCase().contains(v));
    }
    Predicate<String> all = startsWith.and(endsWith).and(contains);
    String attrName = sf.getAttributeDesc();
    return Stream.ofNullable(getAttribute(attrName))
        .map(LdapAttribute::getStringValues)
        .flatMap(Collection::stream)
        .anyMatch(all);
  }

}
