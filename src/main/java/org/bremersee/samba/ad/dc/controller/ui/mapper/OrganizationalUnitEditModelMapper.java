package org.bremersee.samba.ad.dc.controller.ui.mapper;

import org.bremersee.samba.ad.dc.controller.ui.model.OrganizationalUnitEditModel;
import org.bremersee.samba.ad.dc.model.ImmutableOrganizationalUnit;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrganizationalUnitEditModelMapper {

  @Mapping(target = "ou", source = "distinguishedName")
  @Mapping(target = "parentOu", source = "parentDistinguishedName")
  OrganizationalUnitEditModel map(OrganizationalUnit source);

  OrganizationalUnitEditModelMapper INSTANCE = Mappers
      .getMapper(OrganizationalUnitEditModelMapper.class);

  default OrganizationalUnit merge(OrganizationalUnitEditModel source,
      OrganizationalUnit existing) {
    return mergeInternal(source, OrganizationalUnit.builder().from(existing));
  }

  @Mapping(target = "distinguishedName", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "systemOu", ignore = true)
  OrganizationalUnit mergeInternal(
      OrganizationalUnitEditModel source,
      @MappingTarget ImmutableOrganizationalUnit.Builder target);
}
