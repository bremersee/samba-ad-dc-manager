package org.bremersee.samba.ad.dc.samaccount.computer.controller.ui.mapper;

import java.util.Optional;
import org.bremersee.samba.ad.dc.samaccount.computer.controller.ui.model.ComputerEditModel;
import org.bremersee.samba.ad.dc.samaccount.computer.model.DomainComputer;
import org.bremersee.samba.ad.dc.samaccount.computer.model.ImmutableDomainComputer;
import org.bremersee.samba.ad.dc.samaccount.user.controller.ui.mapper.DomainUserEditModelMapper;
import org.bremersee.samba.ad.dc.samaccount.user.controller.ui.model.DomainUserEditModel;
import org.bremersee.samba.ad.dc.samaccount.user.model.DomainUser;
import org.bremersee.samba.ad.dc.samaccount.user.model.ImmutableDomainUser;
import org.ldaptive.dn.Dn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ComputerEditModelMapper {

  ComputerEditModelMapper INSTANCE = Mappers.getMapper(ComputerEditModelMapper.class);

  @Mapping(target = "newOu", source = "dn", qualifiedByName = "mapDistinguishedNameToNewOu")
  ComputerEditModel map(DomainComputer domainComputer);

  @Named("mapDistinguishedNameToNewOu")
  default String mapDistinguishedNameToNewOuInternal(Dn dn) {
    return Optional.ofNullable(dn)
        .map(Dn::getParent)
        .map(Dn::format)
        .orElse(null);
  }

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
