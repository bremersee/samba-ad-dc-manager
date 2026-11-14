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

package org.bremersee.samba.ad.dc.model;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Getter;

/**
 * The domain group member type.
 *
 * @author Christian Bremer
 */
@Schema(description = "The type of o group member.", enumAsRef = true)
@Getter
public enum DomainGroupMemberType {

  /**
   * The Unknown.
   */
  UNKNOWN(
      "text-danger",
      "fas fa-question-circle",
      "Unknown",
      "domain-group-member-type.unknown"),

  /**
   * The User.
   */
  USER(
      "text-primary",
      "fas fa-user",
      "User",
      "domain-group-member-type.user"),

  /**
   * The Group.
   */
  GROUP(
      "text-success",
      "fas fa-user-group",
      "Group",
      "domain-group-member-type.group"),

  /**
   * The Computer.
   */
  COMPUTER(
      "text-warning",
      "fas fa-computer",
      "Computer",
      "domain-group-member-type.computer");

  private final String cssClass;

  private final String cssClassIcon;

  private final String displayName;

  private final String i18nCode;

  DomainGroupMemberType(String cssClass, String cssClassIcon, String displayName, String i18nCode) {
    this.cssClass = cssClass;
    this.cssClassIcon = cssClassIcon;
    this.displayName = displayName;
    this.i18nCode = i18nCode;
  }

  @JsonValue
  @Override
  public String toString() {
    return name().toLowerCase();
  }

  /**
   * From string domain group member type.
   *
   * @param type the type
   * @return the domain group member type
   */
  @JsonCreator
  public static DomainGroupMemberType fromString(String type) {
    if (isNull(type)) {
      return null;
    }
    try {
      return DomainGroupMemberType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * From object classes domain group member type.
   *
   * @param objectClasses the object classes
   * @return the domain group member type
   */
  public static DomainGroupMemberType fromObjectClasses(Collection<String> objectClasses) {
    return Stream.ofNullable(objectClasses)
        .flatMap(Collection::stream)
        .sorted()
        .map(DomainGroupMemberType::fromString)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(UNKNOWN);
  }

}
