/*
 * Copyright 2024 the original author or authors.
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

import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

/**
 * The domain group type.
 *
 * <p>See: <a
 * href="https://learn.microsoft.com/en-us/windows-server/identity/ad-ds/manage/understand-security-groups">
 * Active Directory security groups</a>
 *
 * @author Christian Bremer
 */
@Schema(description = "The domain group type.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableDomainGroupType.class)
@JsonDeserialize(as = ImmutableDomainGroupType.class)
public interface DomainGroupType {

  int GLOBAL_SECURITY = -2147483646;

  int GLOBAL_DISTRIBUTION = 2;

  int DOMAIN_LOCAL_SECURITY = -2147483644;

  int DOMAIN_LOCAL_DISTRIBUTION = 4;

  int UNIVERSAL_SECURITY = -2147483640;

  int UNIVERSAL_DISTRIBUTION = 8;

  @Schema(description = "The integer value of the group type.", defaultValue = "-2147483646")
  @JsonProperty(value = "value", defaultValue = "-2147483646")
  @Value.Default
  default int getValue() {
    return GLOBAL_SECURITY;
  }

  @Schema(description = "The scope of the group.", accessMode = Schema.AccessMode.READ_ONLY)
  @JsonProperty(value = "scope", access = Access.READ_ONLY)
  @Value.Lazy
  default Scope getScope() {
    return switch (getValue()) {
      case GLOBAL_SECURITY, GLOBAL_DISTRIBUTION -> Scope.GLOBAL;
      case DOMAIN_LOCAL_SECURITY, DOMAIN_LOCAL_DISTRIBUTION -> Scope.DOMAIN_LOCAL;
      case UNIVERSAL_SECURITY, UNIVERSAL_DISTRIBUTION -> Scope.UNIVERSAL;
      default -> Scope.UNKNOWN;
    };
  }

  @Schema(description = "The purpose of the group.", accessMode = Schema.AccessMode.READ_ONLY)
  @JsonProperty(value = "purpose", access = Access.READ_ONLY)
  @Value.Lazy
  default Purpose getPurpose() {
    return switch (getValue()) {
      case GLOBAL_SECURITY,
           DOMAIN_LOCAL_SECURITY,
           UNIVERSAL_SECURITY -> Purpose.SECURITY;
      case GLOBAL_DISTRIBUTION,
           DOMAIN_LOCAL_DISTRIBUTION,
           UNIVERSAL_DISTRIBUTION -> Purpose.DISTRIBUTION;
      default -> Purpose.UNKNOWN;
    };
  }

  static DomainGroupType from(Scope scope, Purpose purpose) {
    if (scope == Scope.DOMAIN_LOCAL && purpose == Purpose.DISTRIBUTION) {
      return from(DOMAIN_LOCAL_DISTRIBUTION);
    }
    if (scope == Scope.DOMAIN_LOCAL && purpose == Purpose.SECURITY) {
      return from(DOMAIN_LOCAL_SECURITY);
    }
    if (scope == Scope.UNIVERSAL && purpose == Purpose.DISTRIBUTION) {
      return from(UNIVERSAL_DISTRIBUTION);
    }
    if (scope == Scope.UNIVERSAL && purpose == Purpose.SECURITY) {
      return from(UNIVERSAL_SECURITY);
    }
    if (scope == Scope.GLOBAL && purpose == Purpose.DISTRIBUTION) {
      return from(GLOBAL_DISTRIBUTION);
    }
    return defaultGroupType();
  }

  static DomainGroupType from(int value) {
    return builder().value(value).build();
  }

  static DomainGroupType defaultGroupType() {
    return from(GLOBAL_SECURITY);
  }

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
  class Builder extends ImmutableDomainGroupType.Builder {

  }

  enum Scope {
    UNKNOWN("Unknown", "domain-group-type.scope.unknown"),
    UNIVERSAL("Universal", "domain-group-type.scope.universal"),
    GLOBAL("Global", "domain-group-type.scope.global"),
    DOMAIN_LOCAL("Domain", "domain-group-type.scope.domain-local");

    @Getter
    private final String value;

    @Getter
    private final String i18nCode;

    Scope(String value, String i18nCode) {
      this.value = value;
      this.i18nCode = i18nCode;
    }

    @JsonValue
    @Override
    public String toString() {
      return value;
    }

    @JsonCreator
    public static Scope fromString(String scope) {
      if (isNull(scope)) {
        return null;
      }
      for (Scope type : Scope.values()) {
        if (scope.equalsIgnoreCase(type.getValue()) || scope.equalsIgnoreCase(type.name())) {
          return type;
        }
      }
      return null;
    }
  }

  enum Purpose {
    UNKNOWN("Unknown", "domain-group-type.purpose.unknown"),
    SECURITY("Security", "domain-group-type.purpose.security"),
    DISTRIBUTION("Distribution", "domain-group-type.purpose.distribution");

    @Getter
    private final String value;

    @Getter
    private final String i18nCode;

    Purpose(String purpose, String i18nCode) {
      this.value = purpose;
      this.i18nCode = i18nCode;
    }

    @JsonValue
    @Override
    public String toString() {
      return value;
    }

    @JsonCreator
    public static Purpose fromString(String value) {
      if (isNull(value)) {
        return null;
      }
      for (Purpose purpose : Purpose.values()) {
        if (value.equalsIgnoreCase(purpose.getValue()) || value.equalsIgnoreCase(purpose.name())) {
          return purpose;
        }
      }
      return null;
    }
  }

}
