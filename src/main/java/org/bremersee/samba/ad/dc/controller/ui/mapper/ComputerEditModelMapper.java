package org.bremersee.samba.ad.dc.controller.ui.mapper;

import org.bremersee.samba.ad.dc.controller.ui.model.ComputerEditModel;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.model.ImmutableDomainComputer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ComputerEditModelMapper {

  ComputerEditModelMapper INSTANCE = Mappers.getMapper(ComputerEditModelMapper.class);

  @Mapping(target = "newOu", source = "parentDistinguishedNameNormalized")
  ComputerEditModel map(DomainComputer source);

  default DomainComputer merge(ComputerEditModel source, DomainComputer existing) {
    return mergeInternal(source, DomainComputer.builder().from(existing));
  }

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
  DomainComputer mergeInternal(
      ComputerEditModel source,
      @MappingTarget ImmutableDomainComputer.Builder target);

}
