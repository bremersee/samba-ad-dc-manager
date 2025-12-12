package org.bremersee.samba.ad.dc.samaccount.computer.repository;

import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.ldaptive.dn.Dn;

public interface SambaToolComputer {

  DomainComputer moveComputer(DomainComputer domainComputer, Dn newOu);

  void deleteComputer(DomainComputer domainComputer);

}
