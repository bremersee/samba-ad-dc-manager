package org.bremersee.samba.ad.dc.controller.api.mapper;

import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.ImmutableDomainGroup;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class GroupPatchMapper {

  public static final GroupPatchMapper INSTANCE = Mappers.getMapper(GroupPatchMapper.class);

  protected GroupPatchMapper() {
    super();
  }

  public DomainGroup patch(DomainGroup patch, DomainGroup existing) {
    DomainGroup source = patch
        .withDistinguishedName(existing.getDistinguishedName())
        .withSid(existing.getSid())
        .withCriticalSystemObject(existing.isCriticalSystemObject())
        .withMemberships(existing.getMemberships())
        .withGroupType(existing.getGroupType())
        .withMembers(existing.getMembers());
    return patchInternal(source, DomainGroup.builder().from(existing));
  }

  abstract DomainGroup patchInternal(
      DomainGroup source,
      @MappingTarget ImmutableDomainGroup.Builder target);

}
