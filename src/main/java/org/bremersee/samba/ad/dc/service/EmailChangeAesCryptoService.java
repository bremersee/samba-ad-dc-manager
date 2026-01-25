package org.bremersee.samba.ad.dc.service;

import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.EmailChange;
import org.springframework.stereotype.Service;

@Service
public class EmailChangeAesCryptoService extends AesCryptoService<EmailChange> {

  public EmailChangeAesCryptoService(ApplicationProperties properties) {
    super(properties.getCryptoSecret());
  }

  @Override
  protected Class<EmailChange> getSourceClass() {
    return EmailChange.class;
  }
}
