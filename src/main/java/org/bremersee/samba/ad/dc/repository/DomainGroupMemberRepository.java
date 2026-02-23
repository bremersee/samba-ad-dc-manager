/*
 * Copyright 2019-2026 the original author or authors.
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
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainGroupMember;
import org.bremersee.samba.ad.dc.model.DomainGroupMemberType;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.ldaptive.dn.Dn;
import org.springframework.lang.Nullable;

/**
 * The domain group member repository interface.
 *
 * @author Christian Bremer
 */
public interface DomainGroupMemberRepository {

  /**
   * Resolve memberships.
   *
   * @param samAccountName the sam account name
   * @param ou the ou
   * @param searchScope the search scope
   * @return the stream
   */
  Stream<DomainGroup> resolveMemberships(
      @NotEmpty String samAccountName,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  /**
   * Gets memberships.
   *
   * @param samAccountName the sam account name
   * @param ou the ou
   * @param searchScope the search scope
   * @return the memberships
   */
  Stream<DomainGroup> getMemberships(
      @NotEmpty String samAccountName,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  /**
   * Gets member selection.
   *
   * @param groupName the group name
   * @param ou the ou
   * @param searchScope the search scope
   * @param query the query
   * @param memberTypes the member types
   * @param withPrimaryMembers the with primary members
   * @return the member selection
   */
  Stream<DomainGroupMember> getMemberSelection(
      String groupName,
      Dn ou,
      TreeSearchScope searchScope,
      String query,
      Collection<DomainGroupMemberType> memberTypes,
      boolean withPrimaryMembers);

  /**
   * Modify members domain group.
   *
   * @param groupName the group name
   * @param ou the ou
   * @param searchScope the search scope
   * @param membersToAdd the members to add
   * @param membersToRemove the members to remove
   * @return the domain group
   */
  @NotNull
  DomainGroup modifyMembers(
      @NotEmpty String groupName,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope,
      @Nullable Set<String> membersToAdd,
      @Nullable Set<String> membersToRemove);

}
