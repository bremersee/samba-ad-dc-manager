package org.bremersee.samba.ad.dc.ou.repository;

import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.ldaptive.dn.Dn;

 public interface SambaToolOu {

  void addOrganizationalUnit(OrganizationalUnit ou, Dn parentOu);

  Dn moveOrganizationalUnit(Dn ouDn, Dn newParentOu);

  Dn renameOrganizationalUnit(Dn ouDn, String newName);

  void deleteOrganizationalUnit(Dn ouDn);

}
