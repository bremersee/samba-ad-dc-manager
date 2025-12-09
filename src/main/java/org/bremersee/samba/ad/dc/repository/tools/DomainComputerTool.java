package org.bremersee.samba.ad.dc.repository.tools;

import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.ldaptive.dn.Dn;

public interface DomainComputerTool {

  DomainComputer moveComputer(DomainComputer domainComputer, Dn newOu);

  void deleteComputer(String name);

}
