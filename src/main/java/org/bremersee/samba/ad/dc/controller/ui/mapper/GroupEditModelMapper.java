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

package org.bremersee.samba.ad.dc.controller.ui.mapper;

import org.bremersee.samba.ad.dc.controller.ui.model.GroupEditModel;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.ImmutableDomainGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * The group edit model mapper.
 */
@Mapper
public abstract class GroupEditModelMapper {

  /**
   * The constant INSTANCE.
   */
  public static final GroupEditModelMapper INSTANCE = Mappers
      .getMapper(GroupEditModelMapper.class);

  /**
   * Instantiates a new group edit model mapper.
   */
  protected GroupEditModelMapper() {
    super();
  }

  /**
   * Map group edit model.
   *
   * @param source the source
   * @return the group edit model
   */
  @Mapping(target = "newOu", source = "parentDistinguishedNameNormalized")
  public abstract GroupEditModel map(DomainGroup source);

  /**
   * Merge domain group.
   *
   * @param source the source
   * @param existing the existing
   * @return the domain group
   */
  public DomainGroup merge(GroupEditModel source, DomainGroup existing) {
    return mergeInternal(source, DomainGroup.builder().from(existing));
  }

  /**
   * Merge internal domain group.
   *
   * @param source the source
   * @param target the target
   * @return the domain group
   */
  @Mapping(target = "distinguishedName", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "sid", ignore = true)
  @Mapping(target = "criticalSystemObject", ignore = true)
  @Mapping(target = "memberships", ignore = true)
  @Mapping(target = "groupType", ignore = true)
  @Mapping(target = "members", ignore = true)
  abstract DomainGroup mergeInternal(
      GroupEditModel source,
      @MappingTarget ImmutableDomainGroup.Builder target);

}
