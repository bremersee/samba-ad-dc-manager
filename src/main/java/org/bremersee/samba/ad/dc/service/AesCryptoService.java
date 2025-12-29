package org.bremersee.samba.ad.dc.service;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.model.AesEncValue;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.keygen.KeyGenerators;

public abstract class AesCryptoService<S> implements CryptoService<S, AesEncValue> {

  private final String secret;

  private final ObjectMapper objectMapper;

  protected AesCryptoService(String secret) {
    this(secret, null);
  }

  protected AesCryptoService(String secret, ObjectMapper objectMapper) {
    this.secret = secret;
    this.objectMapper = isEmpty(objectMapper) ? createObjectMapper() : objectMapper;
  }

  private static ObjectMapper createObjectMapper() {
    return Jackson2ObjectMapperBuilder.json().build();
  }

  @Override
  public AesEncValue encrypt(S source) {
    String json = toJson(source);
    String salt = newSalt();
    String encValue = Encryptors.text(secret, salt).encrypt(json);
    return new AesEncValue(encValue, salt);
  }

  @Override
  public S decrypt(AesEncValue encryptedValue) {
    String salt = encryptedValue.salt();
    String encValue = encryptedValue.encryptedValue();
    String json = Encryptors.text(secret, salt).decrypt(encValue);
    return fromJson(json);
  }

  private String newSalt() {
    return KeyGenerators.string().generateKey();
  }

  private String toJson(S source) {
    try {
      return objectMapper.writeValueAsString(source);
    } catch (JsonProcessingException e) {
      throw ServiceException
          .internalServerError(String.format("Failed to serialize source %s.", source), e);
    }
  }

  private S fromJson(String json) {
    try {
      return objectMapper.readValue(json, getSourceClass());
    } catch (JsonProcessingException e) {
      throw ServiceException.internalServerError("Failed to deserialize json.", e);
    }
  }

  protected abstract Class<S> getSourceClass();

}
