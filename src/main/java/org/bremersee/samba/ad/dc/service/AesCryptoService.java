package org.bremersee.samba.ad.dc.service;

import static org.springframework.util.ObjectUtils.isEmpty;

import org.bremersee.samba.ad.dc.model.AesEncValue;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.keygen.KeyGenerators;
import tools.jackson.databind.json.JsonMapper;

public abstract class AesCryptoService<S> implements CryptoService<S, AesEncValue> {

  private final String secret;

  private final JsonMapper jsonMapper;

  protected AesCryptoService(String secret) {
    this(secret, null);
  }

  protected AesCryptoService(String secret, JsonMapper.Builder jsonMapperBuilder) {
    this.secret = secret;
    this.jsonMapper = isEmpty(jsonMapperBuilder) ? createJsonMapper() : jsonMapperBuilder.build();
  }

  private static JsonMapper createJsonMapper() {
    return JsonMapper.builder().build();
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
    return jsonMapper.writeValueAsString(source);
  }

  private S fromJson(String json) {
    return jsonMapper.readValue(json, getSourceClass());
  }

  protected abstract Class<S> getSourceClass();

}
