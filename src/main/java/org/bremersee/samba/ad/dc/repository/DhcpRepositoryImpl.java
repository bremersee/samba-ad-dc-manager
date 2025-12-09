/*
 * Copyright 2019-2020 the original author or authors.
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

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.model.DhcpLease;
import org.bremersee.samba.ad.dc.repository.tools.DhcpTool;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * The dhcp repository implementation.
 *
 * @author Christian Bremer
 */
@Component("dhcpRepository")
@Slf4j
public class DhcpRepositoryImpl implements DhcpRepository {

  private final DhcpTool dhcpTool;

  public DhcpRepositoryImpl(DhcpTool dhcpTool) {
    this.dhcpTool = dhcpTool;
  }

  @Cacheable(cacheNames = "dhcpLeasesCache")
  @Override
  public List<DhcpLease> findActive() {
    return dhcpTool.findActive();
  }

}
