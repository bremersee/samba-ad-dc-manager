package org.bremersee.samba.ad.dc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.model.AesEncValue;
import org.bremersee.samba.ad.dc.model.PasswordReset;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetAesCryptoService implements PasswordResetCryptoService<AesEncValue> {

  private final DomainControllerProperties properties;

  private final ObjectMapper objectMapper;

  public PasswordResetAesCryptoService(
      DomainControllerProperties properties,
      Jackson2ObjectMapperBuilder objectMapperBuilder) {
    this.properties = properties;
    this.objectMapper = objectMapperBuilder.build();
  }

  @Override
  public AesEncValue encrypt(PasswordReset passwordReset) {
    String json = toJson(passwordReset);
    String salt = newSalt();
    String encValue = Encryptors.text(getSecret(), salt).encrypt(json);
    return new AesEncValue(encValue, salt);
  }

  @Override
  public PasswordReset decrypt(AesEncValue encryptedPasswordReset) {
    String salt = encryptedPasswordReset.salt();
    String encValue = encryptedPasswordReset.encryptedValue();
    String json = Encryptors.text(getSecret(), salt).decrypt(encValue);
    return fromJson(json);
  }

  private String getSecret() {
    return "changeit"; // TODO
  }

  private String newSalt() {
    return KeyGenerators.string().generateKey();
  }

  private String toJson(PasswordReset passwordReset) {
    try {
      return objectMapper.writeValueAsString(passwordReset);
    } catch (JsonProcessingException e) {
      throw ServiceException.internalServerError("Failed to serialize password reset.", e);
    }
  }

  private PasswordReset fromJson(String json) {
    try {
      return objectMapper.readValue(json, PasswordReset.class);
    } catch (JsonProcessingException e) {
      throw ServiceException.internalServerError("Failed to deserialize password reset.", e);
    }
  }
}
