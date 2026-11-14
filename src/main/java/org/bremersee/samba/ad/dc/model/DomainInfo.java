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

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Schema(description = "The password information of an active directory.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableDomainInfo.class)
@JsonDeserialize(as = ImmutableDomainInfo.class)
public interface DomainInfo extends Serializable {

  @Nullable
  String getForest();

  @Nullable
  String getDomain();

  @Nullable
  String getNetbiosDomain();

  @Nullable
  String getDomainControllerName();

  @Nullable
  String getDomainControllerNetbiosName();

  @Nullable
  String getServerSite();

  @Nullable
  String getClientSite();

  /**
   * Gets the immutable builder.
   *
   * @return the builder
   */
  static Builder builder() {
    return new Builder();
  }

  /**
   * The immutable builder.
   */
  class Builder extends ImmutableDomainInfo.Builder {

  }

}
