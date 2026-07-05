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
import static org.mockito.Mockito.doReturn;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.EventType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * The cache event logger test.
 */
class CacheEventLoggerTest {

  /**
   * On event.
   */
  @Test
  void onEvent() {
    CacheEvent<?, ?> event = Mockito.mock(CacheEvent.class);
    doReturn(EventType.CREATED)
        .when(event)
        .getType();
    doReturn("myKey")
        .when(event)
        .getKey();
    doReturn("oldValue")
        .when(event)
        .getOldValue();
    doReturn("newValue")
        .when(event)
        .getNewValue();
    CacheEventLogger target = new CacheEventLogger();
    assertThatNoException().isThrownBy(() -> target.onEvent(event));
  }
}