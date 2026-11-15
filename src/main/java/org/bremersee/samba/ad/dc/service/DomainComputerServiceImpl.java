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

import java.util.Optional;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.pagebuilder.PageBuilder;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.repository.DomainComputerRepository;
import org.ldaptive.dn.Dn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * The domain computer service implementation.
 *
 * @author Christian Bremer
 */
@Service
@Validated
public class DomainComputerServiceImpl implements DomainComputerService {

  private final SortMapper sortMapper;

  private final DomainComputerRepository domainComputerRepository;

  /**
   * Instantiates a new domain computer service.
   *
   * @param sortMapper the sort mapper
   * @param domainComputerRepository the domain computer repository
   */
  public DomainComputerServiceImpl(
      SortMapper sortMapper,
      DomainComputerRepository domainComputerRepository) {
    this.sortMapper = sortMapper;
    this.domainComputerRepository = domainComputerRepository;
  }

  @Override
  public Page<DomainComputer> getComputers(
      Pageable pageable,
      String query,
      Dn ou,
      TreeSearchScope searchScope) {

    return new PageBuilder<DomainComputer, DomainComputer>()
        .sourceEntries(domainComputerRepository.findAll(query, ou, searchScope))
        .pageable(sortMapper.applyDefaults(pageable, null, true, null))
        .build();
  }

  @Override
  public Optional<DomainComputer> getComputer(String name, Dn ou, TreeSearchScope searchScope) {
    return domainComputerRepository.findOne(name, ou, searchScope);
  }

  @Override
  public DomainComputer updateComputer(DomainComputer domainComputer, Dn newOu) {
    return domainComputerRepository.update(domainComputer, newOu);
  }

  @Override
  public boolean deleteComputer(String name) {
    return domainComputerRepository.delete(name);
  }
}
