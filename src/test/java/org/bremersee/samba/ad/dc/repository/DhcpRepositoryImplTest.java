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

package org.bremersee.samba.ad.dc.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.time.OffsetDateTime;
import java.util.List;
import org.bremersee.samba.ad.dc.model.DhcpLease;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The dhcp repository impl test.
 *
 * @author Christian Bremer
 */
@ExtendWith({MockitoExtension.class})
class DhcpRepositoryImplTest {

  @Mock
  private DhcpLeaseListTool dhcpTool;

  @InjectMocks
  private DhcpRepositoryImpl repository;

  /**
   * Find active.
   */
  @Test
  void findActive() {
    List<DhcpLease> expected = List.of(
        DhcpLease.builder()
            .hostname("ukelei")
            .ip("192.168.1.201")
            .mac("b8:xx:xx:xx:xx:xx")
            .begin(OffsetDateTime.parse("2019-08-18T11:20:33Z"))
            .end(OffsetDateTime.parse("2019-08-18T11:23:33Z"))
            .manufacturer("Apple, Inc.")
            .build(),
        DhcpLease.builder()
            .hostname("forelle")
            .ip("192.168.1.202")
            .mac("b7:xx:xx:xx:xx:xx")
            .begin(OffsetDateTime.parse("2019-08-18T11:12:33Z"))
            .end(OffsetDateTime.parse("2019-08-18T11:15:33Z"))
            .build()
    );
    doReturn(expected)
        .when(dhcpTool)
        .findActive();
    List<DhcpLease> actual = repository.findActive();
    assertThat(actual)
        .containsExactlyInAnyOrderElementsOf(expected);
  }

}