package org.bremersee.samba.ad.dc.repository.cli;

import java.util.List;
import org.bremersee.samba.ad.dc.model.DhcpLease;

public interface DhcpLeaseListTool {

  List<DhcpLease> findActive();

}
