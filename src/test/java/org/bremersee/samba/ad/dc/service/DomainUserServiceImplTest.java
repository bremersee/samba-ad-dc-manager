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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.samba.ad.dc.model.AvatarDefault;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.repository.AvatarRepository;
import org.bremersee.samba.ad.dc.repository.DomainUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ldaptive.dn.Dn;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

/**
 * The domain user service implementation test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class DomainUserServiceImplTest {

  private static final Dn OU = new Dn("CN=Users,DC=samdom,DC=example,DC=org");

  private DomainUserRepository userRepository;

  private AvatarRepository avatarRepository;

  private AuthenticationManager authenticationManager;

  private DomainUserServiceImpl target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    userRepository = mock(DomainUserRepository.class);
    avatarRepository = mock(AvatarRepository.class);
    authenticationManager = mock(AuthenticationManager.class);
    target = new DomainUserServiceImpl(
        SortMapper.defaultSortMapper(), userRepository, avatarRepository);
    target.setAuthenticationManager(authenticationManager);
  }

  private static DomainUser user(String name) {
    return DomainUser.builder()
        .distinguishedName("CN=" + name + "," + OU.format())
        .samAccountName(name)
        .build();
  }

  /**
   * Gets users.
   *
   * @param softly the softly
   */
  @Test
  void getUsers(SoftAssertions softly) {
    DomainUser a = user("anna");
    DomainUser b = user("bert");
    DomainUser c = user("carl");
    doAnswer(invocation -> Stream.of(c, a, b))
        .when(userRepository)
        .findAll("q", OU, TreeSearchScope.ONELEVEL);
    Page<DomainUser> actual = target.getUsers(
        PageRequest.of(0, 2, Sort.by("samAccountName")), "q", OU, TreeSearchScope.ONELEVEL);
    softly.assertThat(actual.getTotalElements()).isEqualTo(3L);
    softly.assertThat(actual.getTotalPages()).isEqualTo(2);
    softly.assertThat(actual.getContent()).containsExactly(a, b);
  }

  /**
   * Add user.
   */
  @Test
  void addUser() {
    DomainUser source = user("anna");
    doReturn(source).when(userRepository).add(source, "secret", OU, true);
    assertThat(target.addUser(source, "secret", OU, true))
        .isEqualTo(source);
    verify(userRepository).add(source, "secret", OU, true);
  }

  /**
   * Gets user.
   *
   * @param softly the softly
   */
  @Test
  void getUser(SoftAssertions softly) {
    DomainUser expected = user("anna");
    doReturn(Optional.of(expected))
        .when(userRepository)
        .findOne("anna", OU, TreeSearchScope.SUBTREE);
    doReturn(Optional.of(expected))
        .when(userRepository)
        .findOneByPrincipalName("anna@samdom.example.org");
    doReturn(Optional.of(expected))
        .when(userRepository)
        .findOneByUid("anna");
    doReturn(Optional.of(expected))
        .when(userRepository)
        .findOneByUidNumber(10000);

    softly.assertThat(target.getUser("anna", OU, TreeSearchScope.SUBTREE))
        .hasValue(expected);
    softly.assertThat(target.getUserByPrincipalName("anna@samdom.example.org"))
        .hasValue(expected);
    softly.assertThat(target.getUserByUid("anna"))
        .hasValue(expected);
    softly.assertThat(target.getUserByUidNumber(10000))
        .hasValue(expected);
    softly.assertThat(target.getUserByUidNumber(10001))
        .isEmpty();
  }

  /**
   * Gets user avatar.
   */
  @Test
  void getUserAvatar() {
    byte[] expected = {1, 2, 3};
    doReturn(Optional.of(expected))
        .when(avatarRepository)
        .findAvatar("anna", OU, TreeSearchScope.SUBTREE, AvatarDefault.MP, 80);
    assertThat(target.getUserAvatar(
        "anna", OU, TreeSearchScope.SUBTREE, AvatarDefault.MP, 80))
        .hasValue(expected);
  }

  /**
   * Update user.
   */
  @Test
  void updateUser() {
    DomainUser source = user("anna");
    DomainUser expected = user("anne");
    doReturn(expected).when(userRepository).update("anna", source, OU);
    assertThat(target.updateUser("anna", source, OU))
        .isEqualTo(expected);
  }

  /**
   * Update user password.
   */
  @Test
  void updateUserPassword() {
    target.updateUserPassword("anna", "newSecret");
    verify(userRepository).savePassword("anna", "newSecret");
    verify(authenticationManager, never()).authenticate(any());
  }

  /**
   * Update user password with old password.
   *
   * @param softly the softly
   */
  @Test
  void updateUserPasswordWithOldPassword(SoftAssertions softly) {
    target.updateUserPassword("anna", "oldSecret", "newSecret");
    ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
    verify(authenticationManager).authenticate(captor.capture());
    softly.assertThat(captor.getValue().getPrincipal()).isEqualTo("anna");
    softly.assertThat(captor.getValue().getCredentials()).isEqualTo("oldSecret");
    verify(userRepository).savePassword("anna", "newSecret");
  }

  /**
   * Update user password with wrong old password.
   */
  @Test
  void updateUserPasswordWithWrongOldPassword() {
    doThrow(new BadCredentialsException("Bad credentials"))
        .when(authenticationManager)
        .authenticate(any());
    assertThatExceptionOfType(BadCredentialsException.class)
        .isThrownBy(() -> target.updateUserPassword("anna", "wrong", "newSecret"));
    verify(userRepository, never()).savePassword(anyString(), anyString());
  }

  /**
   * Update and remove user avatar.
   */
  @Test
  void updateAndRemoveUserAvatar() {
    InputStream avatar = new ByteArrayInputStream(new byte[]{1, 2, 3});
    target.updateUserAvatar("anna", avatar);
    verify(avatarRepository).saveAvatar("anna", avatar);
    target.removeUserAvatar("anna");
    verify(avatarRepository).removeAvatar("anna");
  }

  /**
   * Delete user.
   *
   * @param softly the softly
   */
  @Test
  void deleteUser(SoftAssertions softly) {
    doReturn(true).when(userRepository).delete("anna");
    softly.assertThat(target.deleteUser("anna")).isTrue();
    softly.assertThat(target.deleteUser("bert")).isFalse();
  }

  /**
   * Exists avatar in active directory.
   *
   * @param softly the softly
   */
  @Test
  void existsAvatarInActiveDirectory(SoftAssertions softly) {
    doReturn(true)
        .when(avatarRepository)
        .existsAvatarInActiveDirectory("anna", OU, TreeSearchScope.SUBTREE);
    softly.assertThat(target.existsAvatarInActiveDirectory("anna", OU, TreeSearchScope.SUBTREE))
        .isTrue();
    softly.assertThat(target.existsAvatarInActiveDirectory("bert", OU, TreeSearchScope.SUBTREE))
        .isFalse();
  }

}
