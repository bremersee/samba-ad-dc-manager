package org.bremersee.samba.ad.dc.repository;

import org.bremersee.samba.ad.dc.model.DomainUser;
import org.ldaptive.dn.Dn;

public interface SambaToolUser {

  void addUser(DomainUser domainUser, Dn ou, Boolean useUsernameAsCn, Boolean isRfc2307Enabled);

  void renameAndMoveUser(DomainUser oldDomainUser, DomainUser newDomainUser, Dn newDn);

  void deleteUser(String samAccountName);

}
