/*
 * Copyright 2024 the original author or authors.
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

package org.bremersee.samba.ad.dc.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bremersee.samba.ad.dc.model.AvatarDefault;
import org.bremersee.samba.ad.dc.repository.tools.ImageToolAwt;
import org.junit.jupiter.api.Test;

/**
 * The fallback avatar repository test.
 */
class FallbackAvatarProviderTest {

  /**
   * The Target.
   */
  static final FallbackAvatarProvider target = new FallbackAvatarProvider(new ImageToolAwt());

  /**
   * Gets fallback avatar.
   */
  @Test
  void getFallbackAvatar() {
    assertNotNull(target.getFallbackAvatar(100));
  }

  /**
   * Find avatar.
   */
  @Test
  void findAvatar() {
    assertTrue(target.findAvatar("foo", AvatarDefault.MP, 120).isPresent());
    assertTrue(target.findAvatar("foo", AvatarDefault.NOT_FOUND, 120).isEmpty());
  }

  /**
   * Exists avatar.
   */
  @Test
  void existsAvatar() {
    assertFalse(target.existsAvatar("foo"));
  }
}