package org.bremersee.samba.ad.dc.repository;

import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.ldaptive.dn.Dn;

public interface SambaToolGroup {

  void addGroup(DomainGroup domainGroup, Dn ou, Boolean isRfc2307Enabled);

  void renameAndMoveGroup(DomainGroup oldDomainGroup, DomainGroup newDomainGroup, Dn newDn);

  void deleteGroup(String samAccountName);

}
