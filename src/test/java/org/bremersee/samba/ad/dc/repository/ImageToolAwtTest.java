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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * The image tool awt test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class ImageToolAwtTest {

  private static final String IMAGE_LOCATION = "classpath:static/mp.jpg";

  private static final String IMAGE_1200_800_LOCATION = "classpath:1200x800.jpg";

  private static final String IMAGE_800_1200_LOCATION = "classpath:800x1200.jpg";

  private static final ResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();

  private static final ImageToolAwt target = new ImageToolAwt();

  /**
   * Scale to smaller image.
   *
   * @param softly the soft assertions
   * @throws IOException the io exception
   */
  @Test
  void scaleToSmallerImage(SoftAssertions softly) throws IOException {
    BufferedImage original = ImageIO
        .read(RESOURCE_LOADER.getResource(IMAGE_LOCATION).getInputStream());
    BufferedImage actual = target.scaleImage(original, new Dimension(20, 20));
    softly.assertThat(actual)
        .extracting(BufferedImage::getHeight, InstanceOfAssertFactories.INTEGER)
        .isLessThan(original.getHeight());
    softly.assertThat(actual)
        .extracting(BufferedImage::getWidth, InstanceOfAssertFactories.INTEGER)
        .isLessThan(original.getWidth());
  }

  /**
   * Scale to bigger image.
   *
   * @param softly the soft assertions
   * @throws IOException the io exception
   */
  @Test
  void scaleToBiggerImage(SoftAssertions softly) throws IOException {
    BufferedImage original = ImageIO
        .read(RESOURCE_LOADER.getResource(IMAGE_LOCATION).getInputStream());
    BufferedImage actual = target.scaleImage(original, new Dimension(5000, 5000));
    softly.assertThat(actual)
        .extracting(BufferedImage::getHeight, InstanceOfAssertFactories.INTEGER)
        .isGreaterThan(original.getHeight());
    softly.assertThat(actual)
        .extracting(BufferedImage::getWidth, InstanceOfAssertFactories.INTEGER)
        .isGreaterThan(original.getWidth());
  }

  /**
   * To square.
   *
   * @throws IOException the io exception
   */
  @Test
  void toSquare() throws IOException {
    byte[] bytes = RESOURCE_LOADER.getResource(IMAGE_LOCATION)
        .getContentAsByteArray();
    BufferedImage actual = target.toSquareImage(bytes);
    assertThat(actual.getHeight()).isEqualTo(actual.getWidth());
  }

  /**
   * To square image from 1200 x 800.
   *
   * @throws IOException the io exception
   */
  @Test
  void toSquareImageFrom1200x800() throws IOException {
    byte[] bytes = RESOURCE_LOADER.getResource(IMAGE_1200_800_LOCATION)
        .getContentAsByteArray();
    BufferedImage original = ImageIO.read(new ByteArrayInputStream(bytes));
    assertThat(original.getHeight()).isLessThan(original.getWidth());

    BufferedImage actual = target.toSquareImage(bytes);
    assertThat(actual.getHeight()).isEqualTo(actual.getWidth());
  }

  /**
   * To square image from 800 x 1200.
   *
   * @throws IOException the io exception
   */
  @Test
  void toSquareImageFrom800x1200() throws IOException {
    byte[] bytes = RESOURCE_LOADER.getResource(IMAGE_800_1200_LOCATION)
        .getContentAsByteArray();
    BufferedImage original = ImageIO.read(new ByteArrayInputStream(bytes));
    assertThat(original.getHeight()).isGreaterThan(original.getWidth());

    BufferedImage actual = target.toSquareImage(bytes);
    assertThat(actual.getHeight()).isEqualTo(actual.getWidth());
  }

}