package org.bremersee.samba.ad.dc.service;

import java.time.OffsetDateTime;
import org.bremersee.samba.ad.dc.model.DomainUser;

public interface CryptoService {

  String encryptSamAccountName(DomainUser user);

  String encryptPasswordLastSet(DomainUser user);

  String encryptCurrentDateTime(DomainUser user);

  String decryptSamAccountName(String encryptedSamAccountName);

  OffsetDateTime decryptPasswordLastSet(String encryptedValue, DomainUser user);

  OffsetDateTime decryptDateTime(String encryptedValue, DomainUser user);

}
