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
 * The email change.
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
@JsonSerialize(as = ImmutableEmailChange.class)
@JsonDeserialize(as = ImmutableEmailChange.class)
public interface EmailChange extends Serializable {

  /**
   * Gets username.
   *
   * @return the username
   */
  @JsonProperty(value = "username", required = true)
  String getUsername();

  /**
   * Gets old email.
   *
   * @return the old email
   */
  @JsonProperty(value = "oldEmail", required = true)
  String getOldEmail();

  /**
   * Gets new email.
   *
   * @return the new email
   */
  @JsonProperty(value = "newEmail", required = true)
  String getNewEmail();

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
   * Of email change.
   *
   * @param domainUser the domain user
   * @param newEmail the new email
   * @return the email change
   */
  static EmailChange of(DomainUser domainUser, String newEmail) {
    return builder()
        .username(domainUser.getSamAccountName())
        .oldEmail(domainUser.getEmail())
        .newEmail(newEmail)
        .build();
  }

  /**
   * Builder builder.
   *
   * @return the builder
   */
  static Builder builder() {
    return new Builder();
  }

  /**
   * The type Builder.
   */
  class Builder extends ImmutableEmailChange.Builder {

  }

}
