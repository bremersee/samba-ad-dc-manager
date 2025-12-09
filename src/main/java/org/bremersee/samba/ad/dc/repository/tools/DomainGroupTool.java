package org.bremersee.samba.ad.dc.repository.tools;

import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.ldaptive.dn.Dn;

public interface DomainGroupTool {

  void addGroup(DomainGroup domainGroup, Dn ou, Boolean isRfc2307Enabled);

  DomainGroup renameAndMoveGroup(DomainGroup oldDomainGroup, DomainGroup newDomainGroup, Dn newDn);

  void deleteGroup(String samAccountName);

}
