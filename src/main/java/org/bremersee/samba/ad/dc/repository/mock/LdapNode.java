package org.bremersee.samba.ad.dc.repository.mock;

import java.util.List;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.ldaptive.serializable.SerLdapEntry;
import org.bremersee.samba.ad.dc.model.Sid;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.EqualityFilter;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.NotFilter;
import org.ldaptive.filter.OrFilter;
import org.ldaptive.filter.SubstringFilter;
import org.springframework.util.ObjectUtils;

public class LdapNode extends LdapEntry {

  LdaptiveOperations ldapOperations;

  private LdapEntry parent;

  //private LdapEntry current; // dann brauche ich die Modifications gar nicht machen, oder? nur die Password änderungen gehen nicht

  private List<LdapEntry> children;

  public LdapNode(String baseDn) {
    setDn(baseDn);
    AdConstants.OBJECT_SID.setValue(this, Sid.builder()
            .value("example-sid-1234")
        .build());
  }

  boolean matches(Filter filter) {
    if (ObjectUtils.isEmpty(filter)) {
      return true;
    }
    if (filter instanceof AndFilter af) {
      return af.getComponents().stream().allMatch(this::matches);
    }
    if (filter instanceof OrFilter or) {
      return or.getComponents().stream().allMatch(this::matches);
    }
    if (filter instanceof NotFilter nf) {
      return !matches(nf.getComponent());
    }
    /*
    if (filter instanceof EqualityFilter ef) {
      return findAttribute(ef.getAttributeDesc())
          .map(attr -> attr.hasValue(ef.getAssertionValue()))
          .orElse(false);
    }

     */
    if (filter instanceof SubstringFilter sf) {

    }
    // and so on, what we need
    return false;
  }

}
