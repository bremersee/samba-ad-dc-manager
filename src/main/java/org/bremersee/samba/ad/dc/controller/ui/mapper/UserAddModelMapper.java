package org.bremersee.samba.ad.dc.controller.ui.mapper;

import org.bremersee.samba.ad.dc.controller.ui.model.GroupAddModel;
import org.bremersee.samba.ad.dc.controller.ui.model.UserAddModel;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserAddModelMapper {

  UserAddModelMapper INSTANCE = Mappers.getMapper(UserAddModelMapper.class);

  @Mapping(target = "distinguishedName", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "sid", ignore = true)
  @Mapping(target = "criticalSystemObject", ignore = true)
  @Mapping(target = "memberships", ignore = true)
  @Mapping(source = "enabled", target = "accountControl.enabled")
  @Mapping(
      source = "passwordExpirationEnabled",
      target = "accountControl.passwordExpirationEnabled")
  DomainUser map(UserAddModel source);

}
