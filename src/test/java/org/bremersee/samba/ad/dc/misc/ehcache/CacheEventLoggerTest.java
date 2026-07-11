/*
 * Copyright 2019-2026 the original author or authors.
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

package org.bremersee.samba.ad.dc.misc.ehcache;

import static org.assertj.core.api.Assertions.assertThatNoException;

import org.ehcache.core.events.CacheEvents;
import org.ehcache.event.CacheEvent;
import org.junit.jupiter.api.Test;

/**
 * The cache event logger test.
 */
class CacheEventLoggerTest {

  /**
   * On event.
   */
  @Test
  void onEvent() {
    CacheEvent<?, ?> event = CacheEvents.creation("myKey", "newValue", null);
    CacheEventLogger target = new CacheEventLogger();
    assertThatNoException().isThrownBy(() -> target.onEvent(event));
  }

}