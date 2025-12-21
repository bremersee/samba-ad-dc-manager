package org.bremersee.samba.ad.dc.repository;

import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.ldaptive.dn.Dn;

public interface SambaToolComputer {

  void moveComputer(DomainComputer domainComputer, Dn newOu);

  void deleteComputer(DomainComputer domainComputer);

}
