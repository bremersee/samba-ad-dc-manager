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

import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.repository.SambaToolDomain;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * The mocked samba tool domain.
 *
 * @author Christian Bremer
 */
@Primary
@Component
@Profile("mock")
class MockedSambaToolDomain implements SambaToolDomain {

  private final SambaStore store;

  /**
   * Instantiates a new mocked samba tool domain.
   *
   * @param store the store
   */
  MockedSambaToolDomain(SambaStore store) {
    this.store = store;
  }

  @Override
  public DomainInfo getDomainInfo(String ipOrHostname) {
    return store.getDomainInfo();
  }

  @Override
  public PasswordInformation getPasswordInformation() {
    return store.getPasswordInformation();
  }
}
