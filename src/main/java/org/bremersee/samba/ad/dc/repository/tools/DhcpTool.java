package org.bremersee.samba.ad.dc.repository.tools;

import java.util.List;
import org.bremersee.samba.ad.dc.model.DhcpLease;

public interface DhcpTool {

  List<DhcpLease> findActive();

}
