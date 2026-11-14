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

/**
 * The password reset model.
 *
 * @author Christian Bremer
 */
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

  /**
   * Gets username.
   *
   * @return the username
   */
  @JsonProperty(value = "username", required = true)
  String getUsername();

  /**
   * Gets request date time.
   *
   * @return the request date time
   */
  @JsonProperty(value = "requested", required = true)
  @Value.Default
  default OffsetDateTime getRequestDateTime() {
    return OffsetDateTime.now(ZoneOffset.UTC);
  }

  /**
   * Gets pwd last set date time.
   *
   * @return the pwd last set date time
   */
  @JsonProperty(value = "pwdLastSet", required = true)
  OffsetDateTime getPwdLastSetDateTime();

  /**
   * Is invitation boolean.
   *
   * @return the boolean
   */
  @JsonProperty(value = "invitation", defaultValue = "false")
  @Value.Default
  default boolean isInvitation() {
    return false;
  }

  /**
   * Of password reset.
   *
   * @param domainUser the domain user
   * @param isInvitation the is invitation
   * @return the password reset
   */
  static PasswordReset of(DomainUser domainUser, boolean isInvitation) {
    return builder()
        .username(domainUser.getSamAccountName())
        .pwdLastSetDateTime(domainUser.getPasswordLastSet())
        .invitation(isInvitation)
        .build();
  }

  /**
   * Gets new builder.
   *
   * @return the builder
   */
  static Builder builder() {
    return new Builder();
  }

  /**
   * The builder.
   *
   * @author Christian Bremer
   */
  class Builder extends ImmutablePasswordReset.Builder {

  }

}
