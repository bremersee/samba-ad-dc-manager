package org.bremersee.samba.ad.dc.domain.repository;

import org.bremersee.samba.ad.dc.domain.model.DomainInfo;
import org.bremersee.samba.ad.dc.domain.model.PasswordInformation;

public interface SambaToolDomain {

  DomainInfo getDomainInfo(String ipOrHostname);

  PasswordInformation getPasswordInformation();

}
