package org.bremersee.samba.ad.dc.samaccount.user.controller.ui.mapper;

import static java.util.Objects.isNull;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.bremersee.samba.ad.dc.samaccount.user.controller.ui.model.DomainUserEditModel;
import org.bremersee.samba.ad.dc.samaccount.user.model.DomainUser;
import org.bremersee.samba.ad.dc.samaccount.user.model.DomainUserAccountControl;
import org.bremersee.samba.ad.dc.samaccount.user.model.ImmutableDomainUser;
import org.bremersee.samba.ad.dc.samaccount.user.model.ModifiableDomainUserAccountControl;
import org.ldaptive.dn.Dn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DomainUserEditModelMapper {

  DomainUserEditModelMapper INSTANCE = Mappers.getMapper(DomainUserEditModelMapper.class);

  @Mapping(target = "newOu", source = "dn")
  @Mapping(target = "noExpiry", source = "source", qualifiedByName = "mapNoExpiryInternal")
  DomainUserEditModel map(DomainUser source);

  default ModifiableDomainUserAccountControl mapInternal(
      DomainUserAccountControl domainUserAccountControl) {
    return ModifiableDomainUserAccountControl.create().from(domainUserAccountControl);
  }

  default String mapInternal(Dn distinguishedName) {
    return Optional.ofNullable(distinguishedName)
        .map(Dn::getParent)
        .map(Dn::format)
        .orElse(null);
  }

  @Named("mapNoExpiryInternal")
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
