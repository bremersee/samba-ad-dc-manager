package org.bremersee.samba.ad.dc.samaccount.group.controller.ui.mapper;

import org.bremersee.samba.ad.dc.samaccount.group.controller.ui.model.GroupEditModel;
import org.bremersee.samba.ad.dc.samaccount.group.model.DomainGroup;
import org.bremersee.samba.ad.dc.samaccount.group.model.ImmutableDomainGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GroupEditModelMapper {

  GroupEditModelMapper INSTANCE = Mappers.getMapper(GroupEditModelMapper.class);

  @Mapping(target = "newOu", source = "parentDistinguishedNameNormalized")
  GroupEditModel map(DomainGroup source);

  default DomainGroup merge(GroupEditModel source, DomainGroup existing) {
    return mergeInternal(source, DomainGroup.builder().from(existing));
  }

  @Mapping(target = "distinguishedName", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "sid", ignore = true)
  @Mapping(target = "criticalSystemObject", ignore = true)
  @Mapping(target = "memberships", ignore = true)
  @Mapping(target = "groupType", ignore = true)
  @Mapping(target = "members", ignore = true)
  DomainGroup mergeInternal(
      GroupEditModel source,
      @MappingTarget ImmutableDomainGroup.Builder target);

}
