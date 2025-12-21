package org.bremersee.samba.ad.dc.repository;

import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.PasswordInformation;

public interface SambaToolDomain {

  DomainInfo getDomainInfo(String ipOrHostname);

  PasswordInformation getPasswordInformation();

}
