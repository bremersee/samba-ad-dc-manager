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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.AvatarDefault;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.dn.Dn;
import org.springframework.core.io.DefaultResourceLoader;

/**
 * The avatar repository implementation test.
 *
 * @author Christian Bremer
 */
class AvatarRepositoryImplTest {

  private LdaptiveOperations ldapOperations;

  private AvatarProvider avatarProvider;

  private ImageTool imageTool;

  private AvatarRepositoryImpl target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    properties.setBaseDn("DC=samdom,DC=example,DC=org");
    properties.getUser().setDefaultOu("CN=People");
    ldapOperations = mock(LdaptiveOperations.class);
    avatarProvider = mock(AvatarProvider.class);
    imageTool = mock(ImageTool.class);
    target = new AvatarRepositoryImpl(
        properties,
        ldapOperations,
        List.of(avatarProvider),
        imageTool);
  }

  /**
   * Gets default ou.
   */
  @Test
  void getDefaultOu() {
    Dn expected = new Dn("CN=People");
    Dn actual = target.getDefaultOu();
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Gets object class value.
   */
  @Test
  void getObjectClassValue() {
    String expected = AdConstants.OBJECT_CLASS_USER;
    String actual = target.getObjectClassValue();
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Gets binary attributes.
   */
  @Test
  void getBinaryAttributes() {
    String[] actual = target.getBinaryAttributes();
    assertThat(actual)
        .containsExactly(AdConstants.USER_JPEG_PHOTO.getName());
  }

  /**
   * Gets return attributes.
   */
  @Test
  void getReturnAttributes() {
    String[] actual = target.getReturnAttributes();
    assertThat(actual)
        .containsExactly(
            AdConstants.USER_JPEG_PHOTO.getName(),
            AdConstants.MAIL.getName());
  }

  /**
   * Gets unique name attribute name.
   */
  @Test
  void getUniqueNameAttributeName() {
    String expected = AdConstants.SAM_ACCOUNT_NAME.getName();
    String actual = target.getUniqueNameAttributeName();
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Exists avatar in active directory and expect true.
   */
  @Test
  void existsAvatarInActiveDirectoryAndExpectTrue() {
    byte[] image = "Image".getBytes(StandardCharsets.UTF_8);
    LdapEntry ldapEntry = new LdapEntry();
    AdConstants.USER_JPEG_PHOTO.setValue(ldapEntry, image);
    doReturn(Optional.of(ldapEntry))
        .when(ldapOperations)
        .findOne(any(SearchRequest.class));
    boolean actual = target.existsAvatarInActiveDirectory("junit", null, null);
    assertThat(actual)
        .isTrue();
  }

  /**
   * Exists avatar in active directory and expect false.
   */
  @Test
  void existsAvatarInActiveDirectoryAndExpectFalse() {
    doReturn(Optional.empty())
        .when(ldapOperations)
        .findOne(any(SearchRequest.class));
    boolean actual = target
        .existsAvatarInActiveDirectory("junit", new Dn("CN=People"), TreeSearchScope.ONELEVEL);
    assertThat(actual)
        .isFalse();
  }

  /**
   * Find avatar in active directory.
   *
   * @throws IOException the io exception
   */
  @Test
  void findAvatarInActiveDirectory() throws IOException {
    byte[] image = new DefaultResourceLoader()
        .getResource("classpath:avatar.jpeg")
        .getContentAsByteArray();
    LdapEntry ldapEntry = new LdapEntry();
    AdConstants.USER_JPEG_PHOTO.setValue(ldapEntry, image);
    doReturn(Optional.of(ldapEntry))
        .when(ldapOperations)
        .findOne(any(SearchRequest.class));

    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
    doReturn(bufferedImage)
        .when(imageTool)
        .toSquareImage(any(byte[].class));
    doReturn(bufferedImage)
        .when(imageTool)
        .scaleImage(any(BufferedImage.class), any(Dimension.class));

    Optional<byte[]> actual = target
        .findAvatar(
            "junit",
            new Dn("CN=Users"),
            TreeSearchScope.SUBTREE,
            AvatarDefault.NOT_FOUND,
            100);

    assertThat(actual)
        .isPresent();
  }

  /**
   * Find avatar by provider.
   *
   * @throws IOException the io exception
   */
  @Test
  void findAvatarByProvider() throws IOException {
    doReturn(Optional.empty())
        .when(ldapOperations)
        .findOne(any(SearchRequest.class));

    byte[] image = new DefaultResourceLoader()
        .getResource("classpath:avatar.jpeg")
        .getContentAsByteArray();
    doReturn(Optional.of(image))
        .when(avatarProvider)
        .findAvatar("junit", AvatarDefault.NOT_FOUND, 100);

    byte[] actual = target
        .findAvatar(
            "junit",
            new Dn("CN=Users"),
            TreeSearchScope.SUBTREE,
            AvatarDefault.NOT_FOUND,
            100)
        .orElse(new byte[0]);

    assertThat(actual)
        .isEqualTo(image);
  }

  /**
   * Find avatar by provider with mail.
   *
   * @throws IOException the io exception
   */
  @Test
  void findAvatarByProviderWithMail() throws IOException {
    LdapEntry ldapEntry = new LdapEntry();
    AdConstants.MAIL.setValue(ldapEntry, "junit@example.org");
    doReturn(Optional.of(ldapEntry))
        .when(ldapOperations)
        .findOne(any(SearchRequest.class));

    byte[] image = new DefaultResourceLoader()
        .getResource("classpath:avatar.jpeg")
        .getContentAsByteArray();
    doReturn(Optional.of(image))
        .when(avatarProvider)
        .findAvatar("junit", AvatarDefault.NOT_FOUND, 100);

    byte[] actual = target
        .findAvatar(
            "junit",
            new Dn("CN=Users"),
            TreeSearchScope.SUBTREE,
            AvatarDefault.NOT_FOUND,
            100)
        .orElse(new byte[0]);

    assertThat(actual)
        .isEqualTo(image);
  }

  /**
   * Save avatar.
   *
   * @throws IOException the io exception
   */
  @Test
  void saveAvatar() throws IOException {
    String userName = "junit";
    LdapEntry ldapEntry = new LdapEntry();
    ldapEntry.setDn("CN=junit,CN=Users,DC=samdom,DC=example,DC=org");
    doReturn(Optional.of(ldapEntry))
        .when(ldapOperations)
        .findOne(any(SearchRequest.class));

    byte[] image = new DefaultResourceLoader()
        .getResource("classpath:avatar.jpeg")
        .getContentAsByteArray();
    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
    doReturn(bufferedImage)
        .when(imageTool)
        .scaleImage(any(BufferedImage.class), any(Dimension.class));

    ByteArrayInputStream inputStream = new ByteArrayInputStream(image);
    assertThatNoException()
        .isThrownBy(() -> target.saveAvatar(userName, inputStream));
  }

  /**
   * Remove avatar.
   */
  @Test
  void removeAvatar() {
    String userName = "junit";
    LdapEntry ldapEntry = new LdapEntry();
    ldapEntry.setDn("CN=junit,CN=Users,DC=samdom,DC=example,DC=org");
    doReturn(Optional.of(ldapEntry))
        .when(ldapOperations)
        .findOne(any(SearchRequest.class));
    assertThatNoException()
        .isThrownBy(() -> target.removeAvatar(userName));
  }
}