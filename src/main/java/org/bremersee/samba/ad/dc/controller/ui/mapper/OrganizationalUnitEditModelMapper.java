package org.bremersee.samba.ad.dc.controller.ui.mapper;

import org.bremersee.samba.ad.dc.controller.ui.model.OrganizationalUnitEditModel;
import org.bremersee.samba.ad.dc.model.ImmutableOrganizationalUnit;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * The organizational unit edit model mapper.
 */
@Mapper
public abstract class OrganizationalUnitEditModelMapper {

  /**
   * The constant INSTANCE.
   */
  public static final OrganizationalUnitEditModelMapper INSTANCE = Mappers
      .getMapper(OrganizationalUnitEditModelMapper.class);

  /**
   * Instantiates a new organizational unit edit model mapper.
   */
  protected OrganizationalUnitEditModelMapper() {
    super();
  }

  /**
   * Map organizational unit edit model.
   *
   * @param source the source
   * @return the organizational unit edit model
   */
  @Mapping(target = "ou", source = "distinguishedName")
  @Mapping(target = "parentOu", source = "parentDistinguishedName")
  @Mapping(target = "newName", source = "name")
  public abstract OrganizationalUnitEditModel map(OrganizationalUnit source);

  /**
   * Merge organizational unit.
   *
   * @param source the source
   * @param existing the existing
   * @return the organizational unit
   */
  public OrganizationalUnit merge(OrganizationalUnitEditModel source,
      OrganizationalUnit existing) {
    return mergeInternal(source, OrganizationalUnit.builder().from(existing));
  }

  /**
   * Merge internal organizational unit.
   *
   * @param source the source
   * @param target the target
   * @return the organizational unit
   */
  @Mapping(target = "distinguishedName", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "systemOu", ignore = true)
  @Mapping(target = "name", source = "newName")
  abstract OrganizationalUnit mergeInternal(
      OrganizationalUnitEditModel source,
      @MappingTarget ImmutableOrganizationalUnit.Builder target);
}
