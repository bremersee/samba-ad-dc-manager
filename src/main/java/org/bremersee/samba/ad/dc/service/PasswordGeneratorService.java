package org.bremersee.samba.ad.dc.service;

import org.bremersee.samba.ad.dc.model.PasswordInformation;

public interface PasswordGeneratorService {

  String generatePassword(PasswordInformation passwordInformation);

}
