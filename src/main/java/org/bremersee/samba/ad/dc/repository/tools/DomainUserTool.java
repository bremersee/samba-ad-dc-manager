package org.bremersee.samba.ad.dc.repository.tools;

import org.bremersee.samba.ad.dc.model.DomainUser;
import org.ldaptive.dn.Dn;

public interface DomainUserTool {

  void addUser(DomainUser domainUser, Dn ou, Boolean useUsernameAsCn, Boolean isRfc2307Enabled);

  DomainUser renameAndMoveUser(DomainUser oldDomainUser, DomainUser newDomainUser, Dn newDn);

  void deleteUser(String samAccountName);

}
