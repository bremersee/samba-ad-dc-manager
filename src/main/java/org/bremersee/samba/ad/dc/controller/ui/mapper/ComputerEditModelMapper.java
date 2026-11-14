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

import org.bremersee.samba.ad.dc.controller.ui.model.ComputerEditModel;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.model.ImmutableDomainComputer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * The computer edit model mapper.
 */
@Mapper
public abstract class ComputerEditModelMapper {

  /**
   * The constant INSTANCE.
   */
  public static final ComputerEditModelMapper INSTANCE = Mappers
      .getMapper(ComputerEditModelMapper.class);

  /**
   * Instantiates a new computer edit model mapper.
   */
  protected ComputerEditModelMapper() {
    super();
  }

  /**
   * Map computer edit model.
   *
   * @param source the source
   * @return the computer edit model
   */
  @Mapping(target = "newOu", source = "parentDistinguishedNameNormalized")
  public abstract ComputerEditModel map(DomainComputer source);

  /**
   * Merge domain computer.
   *
   * @param source the source
   * @param existing the existing
   * @return the domain computer
   */
  public DomainComputer merge(ComputerEditModel source, DomainComputer existing) {
    return mergeInternal(source, DomainComputer.builder().from(existing));
  }

  /**
   * Merge internal domain computer.
   *
   * @param source the source
   * @param target the target
   * @return the domain computer
   */
  @Mapping(target = "distinguishedName", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "samAccountName", ignore = true)
  @Mapping(target = "sid", ignore = true)
  @Mapping(target = "criticalSystemObject", ignore = true)
  @Mapping(target = "primaryGroupId", ignore = true)
  @Mapping(target = "memberships", ignore = true)
  @Mapping(target = "name", ignore = true)
  @Mapping(target = "dnsHostName", ignore = true)
  @Mapping(target = "networkAddresses", ignore = true)
  @Mapping(target = "operatingSystem", ignore = true)
  @Mapping(target = "operatingSystemVersion", ignore = true)
  @Mapping(target = "servicePrincipalNames", ignore = true)
  abstract DomainComputer mergeInternal(
      ComputerEditModel source,
      @MappingTarget ImmutableDomainComputer.Builder target);

}
