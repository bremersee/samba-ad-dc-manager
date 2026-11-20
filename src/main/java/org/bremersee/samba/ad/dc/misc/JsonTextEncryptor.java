/*
 * Copyright 2026 the original author or authors.
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

package org.bremersee.samba.ad.dc.misc;

import static org.springframework.util.ObjectUtils.isEmpty;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.Assert;
import tools.jackson.databind.json.JsonMapper;

/**
 * The json text encryptor.
 *
 * @param <M> the type parameter
 */
public class JsonTextEncryptor<M> {

  private final Class<M> modelClass;

  private final TextEncryptor textEncryptor;

  private final JsonMapper jsonMapper;

  /**
   * Instantiates a new Json text encryptor.
   *
   * @param textEncryptor the text encryptor
   * @param modelClass the model class
   */
  public JsonTextEncryptor(TextEncryptor textEncryptor, Class<M> modelClass) {
    this(textEncryptor, modelClass, (JsonMapper.Builder) null);
  }

  /**
   * Instantiates a new Json text encryptor.
   *
   * @param textEncryptor the text encryptor
   * @param modelClass the model class
   * @param jsonMapperBuilder the json mapper builder
   */
  public JsonTextEncryptor(
      TextEncryptor textEncryptor,
      Class<M> modelClass,
      JsonMapper.Builder jsonMapperBuilder) {
    this(textEncryptor, modelClass, isEmpty(jsonMapperBuilder) ? null : jsonMapperBuilder.build());
  }

  /**
   * Instantiates a new Json text encryptor.
   *
   * @param textEncryptor the text encryptor
   * @param modelClass the model class
   * @param jsonMapper the json mapper
   */
  public JsonTextEncryptor(
      TextEncryptor textEncryptor,
      Class<M> modelClass,
      JsonMapper jsonMapper) {
    Assert.notNull(textEncryptor, "Text encryptor must not be null!");
    Assert.notNull(modelClass, "Model class must not be null!");
    this.textEncryptor = textEncryptor;
    this.modelClass = modelClass;
    this.jsonMapper = isEmpty(jsonMapper)
        ? JsonMapper.builder().build()
        : jsonMapper;
  }

  /**
   * Encrypt.
   *
   * @param source the source
   * @return the string
   */
  public String encrypt(M source) {
    return textEncryptor.encrypt(toJson(source));
  }

  /**
   * Decrypt.
   *
   * @param encoded the encoded
   * @return the model
   */
  public M decrypt(String encoded) {
    return fromJson(textEncryptor.decrypt(encoded));
  }

  private String toJson(M source) {
    return jsonMapper.writeValueAsString(source);
  }

  private M fromJson(String json) {
    return jsonMapper.readValue(json, modelClass);
  }

}
