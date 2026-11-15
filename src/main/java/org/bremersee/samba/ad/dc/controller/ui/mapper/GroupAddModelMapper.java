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

import org.bremersee.samba.ad.dc.controller.ui.model.GroupAddModel;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The group add model mapper.
 *
 * @author Christian Bremer
 */
@Mapper
public interface GroupAddModelMapper {

  /**
   * The constant INSTANCE.
   */
  GroupAddModelMapper INSTANCE = Mappers.getMapper(GroupAddModelMapper.class);

  /**
   * Map domain group.
   *
   * @param source the source
   * @return the domain group
   */
  @Mapping(target = "distinguishedName", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "sid", ignore = true)
  @Mapping(target = "criticalSystemObject", ignore = true)
  @Mapping(target = "memberships", ignore = true)
  @Mapping(target = "groupType", source = "selectedGroupType")
  @Mapping(target = "members", ignore = true)
  DomainGroup map(GroupAddModel source);

}
