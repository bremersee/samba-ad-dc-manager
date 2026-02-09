package org.bremersee.samba.ad.dc.controller.api.mapper;

import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.ImmutableDomainUser;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class UserPatchMapper {

  public static final UserPatchMapper INSTANCE = Mappers.getMapper(UserPatchMapper.class);

  protected UserPatchMapper() {
    super();
  }

  public DomainUser patch(DomainUser patch, DomainUser existing) {
    DomainUser source = patch
        .withDistinguishedName(existing.getDistinguishedName())
        .withSid(existing.getSid())
        .withCriticalSystemObject(existing.isCriticalSystemObject())
        .withMemberships(existing.getMemberships());
    return patchInternal(source, DomainUser.builder().from(existing));
  }

  abstract DomainUser patchInternal(
      DomainUser source,
      @MappingTarget ImmutableDomainUser.Builder target);

}
