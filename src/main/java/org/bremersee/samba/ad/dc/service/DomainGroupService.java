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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainGroupMember;
import org.bremersee.samba.ad.dc.model.DomainGroupMemberType;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.jspecify.annotations.Nullable;
import org.ldaptive.dn.Dn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * The domain group service.
 *
 * @author Christian Bremer
 */
public interface DomainGroupService {

  /**
   * Get groups.
   *
   * @param pageable the pageable
   * @param query the query
   * @param ou the ou
   * @param searchScope the search scope
   * @return the groups
   */
  Page<DomainGroup> getGroups(
      @NotNull Pageable pageable,
      @Nullable String query,
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
   * Resolve memberships stream.
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
   * Gets member selection.
   *
   * @param pageable the pageable
   * @param query the query
   * @param memberTypes the member types
   * @param withPrimaryMembers the with primary members
   * @param groupName the group name
   * @param ou the ou
   * @param searchScope the search scope
   * @return the member selection
   */
  Page<DomainGroupMember> getMemberSelection(
      @NotNull Pageable pageable,
      @Nullable String query,
      @Nullable Collection<DomainGroupMemberType> memberTypes,
      boolean withPrimaryMembers,
      @NotEmpty String groupName,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

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

  /**
   * Add domain group.
   *
   * @param domainGroup the domain group
   * @param ou the ou
   * @return the domain group
   */
  DomainGroup addGroup(@NotNull @Valid DomainGroup domainGroup, @Nullable Dn ou);

  /**
   * Get group by name.
   *
   * @param groupName the group name
   * @param ou the ou
   * @param searchScope the search scope
   * @return the group
   */
  Optional<DomainGroup> getGroup(
      @NotEmpty String groupName,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  /**
   * Gets group by primary group id.
   *
   * @param primaryGroupId the primary group id
   * @return the group by primary group id
   */
  Optional<DomainGroup> getGroupByPrimaryGroupId(@NotNull Integer primaryGroupId);

  /**
   * Gets group by unix group id.
   *
   * @param unixGroupId the unix group id
   * @return the group by unix group id
   */
  Optional<DomainGroup> getGroupByUnixGroupId(@NotNull Integer unixGroupId);

  /**
   * Update group domain group.
   *
   * @param groupName the group name
   * @param domainGroup the domain group
   * @param newOu the new ou
   * @return the domain group
   */
  @NotNull
  DomainGroup updateGroup(
      @NotEmpty String groupName,
      @NotNull @Valid DomainGroup domainGroup,
      @Nullable Dn newOu);

  /**
   * Delete group.
   *
   * @param groupName the group name
   * @return {@code true} if the group was removed; {@code false} if the group didn't exist
   */
  Boolean deleteGroup(@NotEmpty String groupName);

}
