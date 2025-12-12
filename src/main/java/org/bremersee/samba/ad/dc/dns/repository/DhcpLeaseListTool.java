package org.bremersee.samba.ad.dc.dns.repository;

import java.util.List;
import org.bremersee.samba.ad.dc.dns.model.DhcpLease;

public interface DhcpLeaseListTool {

  List<DhcpLease> findActive();

}
