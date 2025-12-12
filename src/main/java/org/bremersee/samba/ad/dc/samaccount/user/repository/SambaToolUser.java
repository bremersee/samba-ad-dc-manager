package org.bremersee.samba.ad.dc.samaccount.user.repository;

import org.bremersee.samba.ad.dc.model.DomainUser;
import org.ldaptive.dn.Dn;

public interface SambaToolUser {

  void addUser(DomainUser domainUser, Dn ou, Boolean useUsernameAsCn, Boolean isRfc2307Enabled);

  // TODO void
  DomainUser renameAndMoveUser(DomainUser oldDomainUser, DomainUser newDomainUser, Dn newDn);

  void deleteUser(String samAccountName);

}
