package org.bremersee.samba.ad.dc.controller.ui.mapper;

import org.bremersee.samba.ad.dc.controller.ui.model.GroupAddModel;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GroupAddModelMapper {

  GroupAddModelMapper INSTANCE = Mappers.getMapper(GroupAddModelMapper.class);

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
