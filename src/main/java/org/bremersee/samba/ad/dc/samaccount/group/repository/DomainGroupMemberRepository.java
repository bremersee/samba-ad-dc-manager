/*
 * Copyright 2019-2020 the original author or authors.
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

package org.bremersee.samba.ad.dc.samaccount.group.repository;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.stream.Stream;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainGroupMember;
import org.bremersee.samba.ad.dc.model.DomainGroupMemberType;
import org.bremersee.samba.ad.dc.common.model.TreeSearchScope;
import org.ldaptive.dn.Dn;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

/**
 * The domain group member repository interface.
 *
 * @author Christian Bremer
 */
@Validated
public interface DomainGroupMemberRepository {

  Stream<DomainGroup> resolveMemberships(
      @NotEmpty String samAccountName,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  Stream<DomainGroup> getMemberships(
      @NotEmpty String samAccountName,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  Stream<DomainGroupMember> getPossibleMembers(
      String groupName,
      Dn ou,
      TreeSearchScope searchScope,
      String query,
      Set<DomainGroupMemberType> memberTypes);

  Stream<DomainGroupMember> findPossibleMembers(
      @NotEmpty String groupName,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  Stream<DomainGroupMember> queryPossibleMembers(
      @NotEmpty String groupName,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope,
      @Nullable String query);

  Stream<DomainGroupMember> getMembers(
      @NotEmpty String groupName,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  @NotNull
  DomainGroup modifyMembers(
      @NotEmpty String groupName,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope,
      @Nullable Set<String> membersToAdd,
      @Nullable Set<String> membersToRemove);

}
