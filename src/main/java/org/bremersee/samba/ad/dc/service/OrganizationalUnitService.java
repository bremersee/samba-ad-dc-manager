/*
 * Copyright 2025-2026  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.samba.ad.dc.service;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.stream.Stream;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.jspecify.annotations.Nullable;
import org.ldaptive.dn.Dn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * The organizational unit service.
 *
 * @author Christian Bremer
 */
public interface OrganizationalUnitService {

  /**
   * Gets organizational units.
   *
   * @param pageable the pageable
   * @param query the query
   * @return the organizational units
   */
  Page<OrganizationalUnit> getOrganizationalUnits(Pageable pageable, String query);

  /**
   * Gets organizational units.
   *
   * @return the organizational units
   */
  Stream<OrganizationalUnit> getOrganizationalUnits();

  /**
   * Gets organizational units with system ous.
   *
   * @return the organizational units with system ous
   */
  Stream<OrganizationalUnit> getOrganizationalUnitsWithSystemOus();

  /**
   * Gets organizational units with system ous and base.
   *
   * @return the organizational units with system ous and base
   */
  Stream<OrganizationalUnit> getOrganizationalUnitsWithSystemOusAndBase();

  /**
   * Gets organizational unit.
   *
   * @param ou the ou
   * @return the organizational unit
   */
  Optional<OrganizationalUnit> getOrganizationalUnit(@Nullable Dn ou);

  /**
   * Gets base.
   *
   * @return the base
   */
  OrganizationalUnit getBase();

  /**
   * Organisation unit exists boolean.
   *
   * @param ou the ou
   * @return the boolean
   */
  boolean organisationUnitExists(@Nullable Dn ou);

  /**
   * Has children boolean.
   *
   * @param ou the ou
   * @return the boolean
   */
  boolean hasChildren(@Nullable Dn ou);

  /**
   * Gets children.
   *
   * @param ou the ou
   * @param pageable the pageable
   * @return the children
   */
  Page<AdEntry> getChildren(@Nullable Dn ou, @NotNull Pageable pageable);

  /**
   * Add organizational unit.
   *
   * @param organizationalUnit the organizational unit
   * @param parentOu the parent ou
   * @return the organizational unit
   */
  @NotNull
  OrganizationalUnit add(@NotNull OrganizationalUnit organizationalUnit, @Nullable Dn parentOu);

  /**
   * Update organizational unit.
   *
   * @param organizationalUnit the organizational unit
   * @param newParentOu the new parent ou
   * @return the organizational unit
   */
  @NotNull
  OrganizationalUnit update(@NotNull OrganizationalUnit organizationalUnit,
      @Nullable Dn newParentOu);

  /**
   * Delete boolean.
   *
   * @param ou the ou
   * @return the boolean
   */
  boolean delete(@NotNull Dn ou);

}
