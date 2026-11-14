/*
 * Copyright 2025-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.samba.ad.dc.service;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.nio.charset.StandardCharsets;
import org.bremersee.samba.ad.dc.model.AesEncValue;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.AesCbcBytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import tools.jackson.databind.json.JsonMapper;

/**
 * The type Aes crypto service.
 *
 * @param <S> the type parameter
 */
public abstract class AesCryptoService<S> implements CryptoService<S, AesEncValue> {

  private final String secret;

  private final JsonMapper jsonMapper;

  /**
   * Instantiates a new Aes crypto service.
   *
   * @param secret the secret
   */
  protected AesCryptoService(String secret) {
    this(secret, null);
  }

  /**
   * Instantiates a new aes crypto service.
   *
   * @param secret the secret
   * @param jsonMapperBuilder the json mapper builder
   */
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
    byte[] encryptedBytes = AesCbcBytesEncryptor
        .withPassword(secret, salt)
        .build()
        .encrypt(json.getBytes(StandardCharsets.UTF_8));
    String encValue = new String(Hex.encode(encryptedBytes));
    return new AesEncValue(encValue, salt);
  }

  @Override
  public S decrypt(AesEncValue encryptedValue) {
    String salt = encryptedValue.salt();
    byte[] encryptedBytes = encryptedValue.encryptedValue().getBytes(StandardCharsets.UTF_8);
    byte[] decryptedBytes = AesCbcBytesEncryptor
        .withPassword(secret, salt)
        .build()
        .decrypt(encryptedBytes);
    String hex = new String(decryptedBytes, StandardCharsets.UTF_8);
    byte[] json = Hex.decode(hex);
    return fromJson(json);
  }

  private String newSalt() {
    return KeyGenerators.string().generateKey();
  }

  private String toJson(S source) {
    return jsonMapper.writeValueAsString(source);
  }

  private S fromJson(byte[] json) {
    return jsonMapper.readValue(json, getSourceClass());
  }

  /**
   * Gets source class.
   *
   * @return the source class
   */
  protected abstract Class<S> getSourceClass();

}
