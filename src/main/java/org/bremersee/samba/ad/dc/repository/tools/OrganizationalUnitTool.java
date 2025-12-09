package org.bremersee.samba.ad.dc.repository.tools;

import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.ldaptive.dn.Dn;

public interface OrganizationalUnitTool {

  void addOrganizationalUnit(OrganizationalUnit organizationalUnit, Dn parentOu);

  Dn moveOrganizationalUnit(Dn oldDn, Dn newParentOu);

  Dn renameOrganizationalUnit(Dn ou, String newName);

  void deleteOrganizationalUnit(Dn ou);

}
