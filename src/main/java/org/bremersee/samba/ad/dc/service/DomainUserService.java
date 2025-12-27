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

package org.bremersee.samba.ad.dc.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.InputStream;
import java.util.Optional;
import org.bremersee.samba.ad.dc.model.AvatarDefault;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.ldaptive.dn.Dn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

/**
 * The domain user service interface.
 *
 * @author Christian Bremer
 */
@Validated
public interface DomainUserService {

  /**
   * Get domain users.
   *
   * @param pageable the pageable
   * @param query the query
   * @param ou the ou
   * @param searchScope the search scope
   * @return the users
   */
  Page<DomainUser> getUsers(
      @NotNull Pageable pageable,
      @Nullable String query,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  /**
   * Add domain user.
   *
   * @param domainUser the domain user
   * @param clearPassword the clear password
   * @param ou the ou
   * @param useUsernameAsCn the use username as cn
   * @return the domain user
   */
  DomainUser addUser(
      @NotNull @Valid DomainUser domainUser,
      @Nullable String clearPassword,
      @Nullable Dn ou,
      @Nullable Boolean useUsernameAsCn);

  /**
   * Get domain user.
   *
   * @param userName the username
   * @param ou the ou
   * @param searchScope the search scope
   * @return the domain user
   */
  Optional<DomainUser> getUser(@NotEmpty String userName, @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  /**
   * Gets user avatar.
   *
   * @param userName the username
   * @param ou the ou
   * @param searchScope the search scope
   * @param avatarDefault the avatar default
   * @param size the size
   * @return the user avatar
   */
  Optional<byte[]> getUserAvatar(
      @NotEmpty String userName,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope,
      @Nullable AvatarDefault avatarDefault,
      @Nullable Integer size);

  /**
   * Update user domain user.
   *
   * @param userName the username
   * @param domainUser the domain user
   * @param newOu the new ou
   * @return the domain user
   */
  @NotNull
  DomainUser updateUser(
      @NotEmpty String userName,
      @NotNull DomainUser domainUser,
      @Nullable Dn newOu);

  /**
   * Update user password.
   *
   * @param userName the username
   * @param newPassword the new password
   */
  void updateUserPassword(@NotEmpty String userName, @NotEmpty String newPassword);

  /**
   * Update user password.
   *
   * @param userName the username
   * @param oldPassword the old password
   * @param newPassword the new password
   */
  void updateUserPassword(
      @NotEmpty String userName,
      @NotNull String oldPassword,
      @NotNull String newPassword);

  /**
   * Update user avatar.
   *
   * @param userName the username
   * @param avatar the avatar
   */
  void updateUserAvatar(@NotEmpty String userName, @NotNull InputStream avatar);

  /**
   * Remove user avatar.
   *
   * @param userName the username
   */
  void removeUserAvatar(@NotEmpty String userName);

  /**
   * Delete user.
   *
   * @param userName the username
   * @return {@code true} if the user was removed; {@code false} if the user didn't exist
   */
  Boolean deleteUser(@NotEmpty String userName);

  /**
   * Exists avatar in active directory boolean.
   *
   * @param user the user
   * @param ou the ou
   * @param searchScope the search scope
   * @return the boolean
   */
  boolean existsAvatarInActiveDirectory(
      @NotEmpty String user,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

}
