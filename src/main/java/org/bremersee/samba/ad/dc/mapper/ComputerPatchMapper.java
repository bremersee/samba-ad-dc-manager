package org.bremersee.samba.ad.dc.mapper;

import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.model.ImmutableDomainComputer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ComputerPatchMapper {

  public static final ComputerPatchMapper INSTANCE = Mappers.getMapper(ComputerPatchMapper.class);

  protected ComputerPatchMapper() {
    super();
  }

  public DomainComputer patch(DomainComputer patch, DomainComputer existing) {
    DomainComputer source = patch
        .withDistinguishedName(existing.getDistinguishedName())
        .withSid(existing.getSid())
        .withCriticalSystemObject(existing.isCriticalSystemObject())
        .withMemberships(existing.getMemberships())
        .withName(existing.getName());
    return patchInternal(source, DomainComputer.builder().from(existing));
  }

  abstract DomainComputer patchInternal(
      DomainComputer source,
      @MappingTarget ImmutableDomainComputer.Builder target);

}
