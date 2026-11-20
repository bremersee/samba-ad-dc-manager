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

package org.bremersee.samba.ad.dc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * The text encryption configuration.
 *
 * @author Christian Bremer
 */
@Configuration
public class TextEncryptionConfiguration {

  /**
   * Creates text encryptor bean.
   *
   * @param properties the properties
   * @return the text encryptor
   */
  @Bean
  public TextEncryptor textEncryptor(ApplicationProperties properties) {
    return Encryptors.text(properties.getCryptoSecret(), properties.getCryptSalt());
  }

}
