package org.bremersee.samba.ad.dc.repository.tools;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

@ExtendWith(SoftAssertionsExtension.class)
class ImageToolAwtTest {

  private static final String IMAGE_LOCATION = "classpath:static/mp.jpg";

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
}