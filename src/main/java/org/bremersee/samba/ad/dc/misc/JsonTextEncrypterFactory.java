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

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The json text encrypter factory.
 *
 * @author Christian Bremer
 */
@Component
public class JsonTextEncrypterFactory {

  private final TextEncryptor textEncryptor;

  private final JsonMapper jsonMapper;

  /**
   * Instantiates a new Json text encrypter factory.
   *
   * @param textEncryptor the text encryptor
   * @param jsonMapperBuilder the json mapper builder
   */
  public JsonTextEncrypterFactory(
      TextEncryptor textEncryptor,
      JsonMapper.Builder jsonMapperBuilder) {
    this.textEncryptor = textEncryptor;
    this.jsonMapper = jsonMapperBuilder.build();
  }

  /**
   * New instance json text encryptor.
   *
   * @param <M> the type parameter
   * @param clazz the clazz
   * @return the json text encryptor
   */
  public <M> JsonTextEncryptor<M> newInstance(Class<M> clazz) {
    return new JsonTextEncryptor<>(textEncryptor, clazz, jsonMapper);
  }

}
