package org.bremersee.samba.ad.dc.misc;

import org.bremersee.samba.ad.dc.model.PasswordInformation;

public interface PasswordGenerator {

  String generatePassword(PasswordInformation passwordInformation);

}
