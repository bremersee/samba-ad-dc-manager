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

package org.bremersee.samba.ad.dc.repository;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.stream.Stream;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.ldaptive.dn.Dn;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

/**
 * The domain computer repository.
 *
 * @author Christian Bremer
 */
@Validated
public interface DomainComputerRepository {

  /**
   * Find all.
   *
   * @param query the query
   * @param ou the ou
   * @param searchScope the search scope
   * @return the stream
   */
  Stream<DomainComputer> findAll(
      @Nullable String query,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  /**
   * Find one.
   *
   * @param name the name
   * @param ou the ou
   * @param searchScope the search scope
   * @return the optional
   */
  Optional<DomainComputer> findOne(
      @NotEmpty String name,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  /**
   * Update domain computer.
   *
   * @param domainComputer the domain computer
   * @param newOu the new ou
   * @return the domain computer
   */
  @NotNull
  DomainComputer update(@NotNull DomainComputer domainComputer, @Nullable Dn newOu);

  /**
   * Delete.
   *
   * @param name the name
   * @return the boolean
   */
  boolean delete(@NotEmpty String name);

}
