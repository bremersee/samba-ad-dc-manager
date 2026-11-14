/*
 * Copyright 2025-2026  the original author or authors.
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

package org.bremersee.samba.ad.dc.repository.mock;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.bremersee.samba.ad.dc.model.DhcpLease;
import org.bremersee.samba.ad.dc.repository.DhcpLeaseListTool;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * The mocked dhcp lease list tool.
 *
 * @author Christian Bremer
 */
@Primary
@Component
@Profile("mock")
class MockedDhcpLeaseListTool implements DhcpLeaseListTool {

  /**
   * Instantiates a new mocked dhcp lease list tool.
   */
  MockedDhcpLeaseListTool() {
    super();
  }

  @Override
  public List<DhcpLease> findActive() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    return List.of(
        DhcpLease.builder()
            .ip("192.168.1.101")
            .hostname("Mercury")
            .mac("xx:xx:xx:xx:xx:xx")
            .manufacturer("Apple, Inc.")
            .begin(now.minusMinutes(15L))
            .end(now.plusMinutes(15L))
            .build(),
        DhcpLease.builder()
            .ip("192.168.1.102")
            .hostname("Saturn")
            .mac("yy:yy:yy:yy:yy:yy")
            .manufacturer("HP Inc.")
            .begin(now.minusMinutes(11L))
            .end(now.plusMinutes(20L))
            .build()
    );
  }
}
