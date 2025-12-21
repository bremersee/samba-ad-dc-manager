package org.bremersee.samba.ad.dc.controller.ui.mapper;

import static java.util.Objects.isNull;

import java.time.OffsetDateTime;
import org.bremersee.samba.ad.dc.controller.ui.model.DomainUserEditModel;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.DomainUserAccountControl;
import org.bremersee.samba.ad.dc.model.ImmutableDomainUser;
import org.bremersee.samba.ad.dc.model.ModifiableDomainUserAccountControl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DomainUserEditModelMapper {

  DomainUserEditModelMapper INSTANCE = Mappers.getMapper(DomainUserEditModelMapper.class);

  @Mapping(target = "newOu", source = "parentDistinguishedNameNormalized")
  @Mapping(target = "noExpiry", source = "source", qualifiedByName = "mapNoExpiry")
  @Mapping(target = "avatar", ignore = true)
  @Mapping(target = "removeAvatar", ignore = true)
  @Mapping(target = "renameNamesAutomatically", ignore = true)
  DomainUserEditModel map(DomainUser source);

  default ModifiableDomainUserAccountControl mapInternal(
      DomainUserAccountControl domainUserAccountControl) {
    return ModifiableDomainUserAccountControl.create().from(domainUserAccountControl);
  }

  @Named("mapNoExpiry")
  default boolean mapNoExpiryInternal(DomainUser source) {
    return isNull(source.getAccountExpires());
  }

  default DomainUser merge(DomainUserEditModel source, DomainUser existingDomainUser) {
    return mergeInternal(source, DomainUser.builder().from(existingDomainUser));
  }

  @Mapping(
      target = "accountExpires",
      source = "source",
      qualifiedByName = "mergeAccountExpiresInternal")
  @Mapping(target = "distinguishedName", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "sid", ignore = true)
  @Mapping(target = "criticalSystemObject", ignore = true)
  @Mapping(target = "memberships", ignore = true)
  @Mapping(target = "lastLogon", ignore = true)
  @Mapping(target = "logonCount", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "passwordLastSet", ignore = true)
  DomainUser mergeInternal(
      DomainUserEditModel source,
      @MappingTarget ImmutableDomainUser.Builder target);

  @Named("mergeAccountExpiresInternal")
  default OffsetDateTime mergeAccountExpiresInternal(DomainUserEditModel source) {
    if (source.isNoExpiry()) {
      return null;
    }
    return source.getAccountExpires();
  }

}
