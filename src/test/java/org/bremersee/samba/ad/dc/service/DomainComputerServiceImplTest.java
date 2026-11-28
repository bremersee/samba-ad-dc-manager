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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.repository.DomainComputerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ldaptive.dn.Dn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * The domain computer service implementation test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class DomainComputerServiceImplTest {

  private static final Dn OU = new Dn("CN=Computers,DC=samdom,DC=example,DC=org");

  private DomainComputerRepository repository;

  private DomainComputerServiceImpl target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    repository = mock(DomainComputerRepository.class);
    target = new DomainComputerServiceImpl(SortMapper.defaultSortMapper(), repository);
  }

  private static DomainComputer computer(String name) {
    return DomainComputer.builder()
        .distinguishedName("CN=" + name + "," + OU.format())
        .samAccountName(name + "$")
        .name(name)
        .build();
  }

  /**
   * Gets computers.
   *
   * @param softly the softly
   */
  @Test
  void getComputers(SoftAssertions softly) {
    DomainComputer a = computer("a");
    DomainComputer b = computer("b");
    DomainComputer c = computer("c");
    doAnswer(invocation -> Stream.of(c, a, b))
        .when(repository)
        .findAll("q", OU, TreeSearchScope.ONELEVEL);

    Page<DomainComputer> actual = target.getComputers(
        PageRequest.of(0, 2, Sort.by("name")), "q", OU, TreeSearchScope.ONELEVEL);
    softly.assertThat(actual.getTotalElements()).isEqualTo(3L);
    softly.assertThat(actual.getContent()).containsExactly(a, b);

    actual = target.getComputers(
        PageRequest.of(1, 2, Sort.by("name")), "q", OU, TreeSearchScope.ONELEVEL);
    softly.assertThat(actual.getContent()).containsExactly(c);
  }

  /**
   * Gets computer.
   */
  @Test
  void getComputer() {
    DomainComputer expected = computer("a");
    doReturn(Optional.of(expected))
        .when(repository)
        .findOne("a", OU, TreeSearchScope.SUBTREE);
    assertThat(target.getComputer("a", OU, TreeSearchScope.SUBTREE))
        .hasValue(expected);
  }

  /**
   * Update computer.
   */
  @Test
  void updateComputer() {
    DomainComputer source = computer("a");
    DomainComputer expected = computer("b");
    doReturn(expected).when(repository).update(any(), any());
    assertThat(target.updateComputer(source, OU))
        .isEqualTo(expected);
    verify(repository).update(source, OU);
  }

  /**
   * Delete computer.
   */
  @Test
  void deleteComputer() {
    doReturn(true).when(repository).delete("a");
    assertThat(target.deleteComputer("a"))
        .isTrue();
  }

}
