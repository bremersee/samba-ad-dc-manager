package org.bremersee.samba.ad.dc.repository.mock;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.misc.DnTool.DnPair;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.RDn;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.EqualityFilter;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.NotFilter;
import org.ldaptive.filter.OrFilter;
import org.ldaptive.filter.PresenceFilter;
import org.ldaptive.filter.SubstringFilter;
import org.springframework.util.Assert;

@Slf4j
@Getter(AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
class LdapNode extends LdapEntry {

  private LdapNode parent;

  private final List<LdapNode> children = new ArrayList<>();

  private LdapNode() {
    OffsetDateTime now = OffsetDateTime.now();
    AdConstants.WHEN_CREATED.setValue(this, now);
    AdConstants.WHEN_CHANGED.setValue(this, now);
  }

  LdapNode(String baseDn) {
    this();
    Assert.isTrue(DnTool.isValidDn(baseDn), "Base DN must be valid.");
    setDn(baseDn);
    AdConstants.DN.setValue(this, new Dn(getDn()));
  }

  LdapNode(LdapEntry entry, LdapNode parent) {
    this(isEmpty(entry) ? null : entry.getDn());
    Assert.notNull(parent, "Parent ldap entry is required.");
    addAttributes(entry.getAttributes());
    parent.addChild(this);
  }

  boolean isRoot() {
    return isEmpty(parent);
  }

  LdapNode getRoot() {
    LdapNode tmp = this;
    while (!tmp.isRoot()) {
      tmp = tmp.getParent();
    }
    return tmp;
  }

  boolean hasObjectClass(String objectClass) {
    return Stream.ofNullable(getAttribute(AdConstants.OBJECT_CLASS.getName()))
        .map(LdapAttribute::getStringValues)
        .flatMap(Collection::stream)
        .anyMatch(oc -> oc.equalsIgnoreCase(objectClass));
  }

  @Override
  public LdapAttribute getAttribute(String name) {
    LdapAttribute attr = super.getAttribute(name);
    boolean isMemberOf = AdConstants.MEMBER_OF_GROUP.getName().equalsIgnoreCase(name)
        && (hasObjectClass(AdConstants.OBJECT_CLASS_USER)
        || hasObjectClass(AdConstants.OBJECT_CLASS_GROUP));
    if (isMemberOf) {
      synchronized (SambaStore.LOCK) {
        return getMemberships(attr);
      }
    }
    if (!isEmpty(attr) || !DnTool.isValidDn(getDn())) {
      return attr;
    }
    return getRdnAttribute(name);
  }

  private LdapAttribute getRdnAttribute(String name) {
    RDn rdn = new Dn(getDn()).getRDn();
    if (rdn.getNameValue().getName().equalsIgnoreCase(name)) {
      return LdapAttribute.builder()
          .name(rdn.getNameValue().getName())
          .values(rdn.getNameValue().getStringValue())
          .build();
    }
    return null;
  }

  private LdapAttribute getMemberships(LdapAttribute attr) {
    Set<DnPair> memberOfSet = Stream.ofNullable(attr)
        .map(LdapAttribute::getStringValues)
        .flatMap(Collection::stream)
        .map(DnTool::getDnPair)
        .collect(Collectors.toCollection(HashSet::new));
    collectMemberships(getRoot().getChildren(), memberOfSet);
    if (memberOfSet.isEmpty()) {
      return null;
    }
    return LdapAttribute.builder()
        .name(AdConstants.MEMBER_OF_GROUP.getName())
        .stringValues(memberOfSet.stream().map(DnPair::dn).toList())
        .build();
  }

  private void collectMemberships(Collection<LdapNode> children, Set<DnPair> memberOfSet) {
    for (LdapNode child : children) {
      if (child.hasObjectClass(AdConstants.OBJECT_CLASS_GROUP)) {
        LdapAttribute members = child.getAttribute(AdConstants.GROUP_MEMBER.getName());
        boolean containsThis = Stream.ofNullable(members)
            .map(LdapAttribute::getStringValues)
            .flatMap(Collection::stream)
            .anyMatch(dn -> DnTool.isSameDn(getDn(), dn));
        if (containsThis) {
          memberOfSet.add(DnTool.getDnPair(child.getDn()));
        }
      }
      collectMemberships(child.getChildren(), memberOfSet);
    }
  }

  void addChild(LdapNode child) {
    if (!isEmpty(child) && DnTool.isValidDn(child.getDn()) && !containsChild(child)) {
      child.parent = this;
      children.add(child);
    }
  }

  void removeChild(LdapNode child) {
    if (!isEmpty(child) && children
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
    return Optional.ofNullable(getAttribute(attrName))
        .map(attr -> matches(ef, attr))
        .orElse(false);
  }

  private boolean matches(EqualityFilter ef, LdapAttribute attr) {
    return attr.isBinary()
        ? matchesBytes(ef.getAssertionValue(), attr.getBinaryValues())
        : matchesString(LdapUtils.utf8Encode(ef.getAssertionValue()), attr.getStringValues());
  }

  private boolean matchesBytes(byte[] assertionValue, Collection<byte[]> attrValues) {
    return Stream.ofNullable(attrValues)
        .flatMap(Collection::stream)
        .anyMatch(value -> Arrays.equals(value, assertionValue));
  }

  private boolean matchesString(String assertionValue, Collection<String> attrValues) {
    return Stream.ofNullable(attrValues)
        .flatMap(Collection::stream)
        .anyMatch(value -> value.equalsIgnoreCase(assertionValue));
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
