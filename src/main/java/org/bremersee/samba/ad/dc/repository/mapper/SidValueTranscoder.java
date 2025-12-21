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

package org.bremersee.samba.ad.dc.repository.mapper;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.samaccount.common.model.Sid;
import org.ldaptive.ad.SecurityIdentifier;
import org.ldaptive.transcode.AbstractBinaryValueTranscoder;

/**
 * The SID value transcoder.
 *
 * @author Christian Bremer
 */
@Slf4j
public class SidValueTranscoder extends AbstractBinaryValueTranscoder<Sid> {
  // TODO use ldaptive stuff
  // belongs to samaccount, but AdConstants? Replace with
  SecurityIdentifier securityIdentifier;
  @Override
  public Sid decodeBinaryValue(byte[] value) {
    return Optional.ofNullable(value)
        .map(SecurityIdentifier::toString)
        .map(objectSid -> Sid.builder()
            .value(objectSid)
            .build())
        .orElse(null);
  }

  @Override
  public byte[] encodeBinaryValue(Sid value) {
    return Optional.ofNullable(value)
        .map(Sid::getValue)
        .map(SecurityIdentifier::toBytes)
        .orElse(null);
  }

  @Override
  public Class<Sid> getType() {
    return Sid.class;
  }

}
