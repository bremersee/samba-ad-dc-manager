/*
 * Copyright 2025-2026 the original author or authors.
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

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.jspecify.annotations.Nullable;
import org.ldaptive.dn.Dn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * The domain computer service.
 *
 * @author Christian Bremer
 */
public interface DomainComputerService {

  /**
   * Gets computers.
   *
   * @param pageable the pageable
   * @param query the query
   * @param ou the ou
   * @param searchScope the search scope
   * @return the computers
   */
  @NotNull
  Page<DomainComputer> getComputers(
      @NotNull Pageable pageable,
      @Nullable String query,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  /**
   * Gets computer.
   *
   * @param name the name
   * @param ou the ou
   * @param searchScope the search scope
   * @return the computer
   */
  Optional<DomainComputer> getComputer(
      @NotEmpty String name,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  /**
   * Update computer domain computer.
   *
   * @param domainComputer the domain computer
   * @param newOu the new ou
   * @return the domain computer
   */
  @NotNull
  DomainComputer updateComputer(@NotNull DomainComputer domainComputer, @Nullable Dn newOu);

  /**
   * Delete computer boolean.
   *
   * @param name the name
   * @return the boolean
   */
  boolean deleteComputer(@NotEmpty String name);

}
