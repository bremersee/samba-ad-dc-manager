package org.bremersee.samba.ad.dc.samaccount.computer.controller.ui.model;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bremersee.samba.ad.dc.samaccount.computer.model.DomainComputer;
import org.ldaptive.dn.Dn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Data
@NoArgsConstructor
public class DomainComputerEditRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  public static final DomainComputerEditMapper MAPPER = Mappers
      .getMapper(DomainComputerEditMapper.class);

  private String newOu;

  private String description;

  public Dn getNewOuDn() {
    if (isEmpty(newOu)) {
      return null;
    }
    return new Dn(newOu);
  }

  @Mapper
  public interface DomainComputerEditMapper {

    @Mapping(source = "dn", target = "newOu")
    DomainComputerEditRequest map(DomainComputer domainComputer);

    default String mapToNewOu(Dn distinguishedName) {
      return Optional.ofNullable(distinguishedName)
          .map(Dn::getParent)
          .map(Dn::format)
          .orElse(null);
    }

    @Mapping(target = "memberships", ignore = true)
    @Mapping(target = "networkAddresses", ignore = true)
    @Mapping(target = "servicePrincipalNames", ignore = true)
    void update(@MappingTarget DomainComputer existingDomainComputer,
        DomainComputerEditRequest domainComputerEditRequest);
  }

}
