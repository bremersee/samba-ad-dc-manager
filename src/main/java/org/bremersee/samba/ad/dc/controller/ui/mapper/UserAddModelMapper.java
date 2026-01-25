package org.bremersee.samba.ad.dc.controller.ui.mapper;

import org.bremersee.samba.ad.dc.controller.ui.model.UserAddModel;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The user add model mapper.
 */
@Mapper
public interface UserAddModelMapper {

  /**
   * The constant INSTANCE.
   */
  UserAddModelMapper INSTANCE = Mappers.getMapper(UserAddModelMapper.class);

  /**
   * Map domain user.
   *
   * @param source the source
   * @return the domain user
   */
  @Mapping(target = "distinguishedName", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "sid", ignore = true)
  @Mapping(target = "criticalSystemObject", ignore = true)
  @Mapping(target = "memberships", ignore = true)
  @Mapping(target = "primaryGroupId", ignore = true)
  @Mapping(target = "lastLogon", ignore = true)
  @Mapping(target = "logonCount", ignore = true)
  @Mapping(target = "passwordLastSet", ignore = true)
  @Mapping(target = "userPrincipalName", ignore = true)
  @Mapping(target = "accountControl.enabled", source = "enabled")
  @Mapping(
      target = "accountControl.passwordExpirationEnabled",
      source = "passwordExpirationEnabled")
  DomainUser map(UserAddModel source);

}
