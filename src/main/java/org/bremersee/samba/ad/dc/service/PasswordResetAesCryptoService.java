package org.bremersee.samba.ad.dc.service;

import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.PasswordReset;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetAesCryptoService extends AesCryptoService<PasswordReset> {

  public PasswordResetAesCryptoService(ApplicationProperties properties) {
    super(properties.getCryptoSecret());
  }

  @Override
  protected Class<PasswordReset> getSourceClass() {
    return PasswordReset.class;
  }
}
