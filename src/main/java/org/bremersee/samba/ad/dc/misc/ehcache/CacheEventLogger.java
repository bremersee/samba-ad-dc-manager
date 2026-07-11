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

import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

/**
 * The cache event logger.
 *
 * @author Christian Bremer
 */
@Slf4j
public class CacheEventLogger implements CacheEventListener<Object, Object> {

  @Override
  public void onEvent(CacheEvent<?, ?> cacheEvent) {

    log.debug(
        "A new cache event has occurred: type='{}', key='{}' ({}), oldValue='{}', newValue='{}'",
        cacheEvent.getType(),
        cacheEvent.getKey(),
        cacheEvent.getKey().getClass(),
        cacheEvent.getOldValue(),
        cacheEvent.getNewValue());
  }
}
