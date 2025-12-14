package org.bremersee.samba.ad.dc.samaccount.computer.repository;

import org.bremersee.samba.ad.dc.samaccount.computer.model.DomainComputer;
import org.ldaptive.dn.Dn;

public interface SambaToolComputer {

  void moveComputer(DomainComputer domainComputer, Dn newOu);

  void deleteComputer(DomainComputer domainComputer);

}
