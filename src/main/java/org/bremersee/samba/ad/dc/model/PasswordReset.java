/*
 * Copyright 2025-2026 the original author or authors.
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

package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutablePasswordReset.class)
@JsonDeserialize(as = ImmutablePasswordReset.class)
public interface PasswordReset extends Serializable {

  @JsonProperty(value = "username", required = true)
  String getUsername();

  @JsonProperty(value = "requested", required = true)
  @Value.Default
  default OffsetDateTime getRequestDateTime() {
    return OffsetDateTime.now(ZoneOffset.UTC);
  }

  @JsonProperty(value = "pwdLastSet", required = true)
  OffsetDateTime getPwdLastSetDateTime();

  @JsonProperty(value = "invitation", defaultValue = "false")
  @Value.Default
  default boolean isInvitation() {
    return false;
  }

  static PasswordReset of(DomainUser domainUser, boolean isInvitation) {
    return builder()
        .username(domainUser.getSamAccountName())
        .pwdLastSetDateTime(domainUser.getPasswordLastSet())
        .invitation(isInvitation)
        .build();
  }

  static Builder builder() {
    return new Builder();
  }

  class Builder extends ImmutablePasswordReset.Builder {

  }

}
