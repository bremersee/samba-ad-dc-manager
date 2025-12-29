package org.bremersee.samba.ad.dc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.AesEncValue;
import org.bremersee.samba.ad.dc.model.EmailChange;
import org.bremersee.samba.ad.dc.model.PasswordReset;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.keygen.KeyGenerators;
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
