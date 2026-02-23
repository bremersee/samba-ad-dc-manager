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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * The image tool.
 *
 * @author Christian Bremer
 */
public interface ImageTool {

  /**
   * To square image.
   *
   * @param img the img
   * @return the buffered image
   * @throws IOException the io exception
   */
  default BufferedImage toSquareImage(byte[] img) throws IOException {
    return toSquareImage(ImageIO.read(new ByteArrayInputStream(img)));
  }

  /**
   * To square image.
   *
   * @param img the img
   * @return the buffered image
   */
  default BufferedImage toSquareImage(BufferedImage img) {
    int width = img.getWidth();
    int height = img.getHeight();
    int size = Math.min(width, height);
    BufferedImage squareImg;
    if (width != height) {
      if (width > height) {
        int a = (width - height) / 2;
        squareImg = img.getSubimage(a, 0, size, size);
      } else {
        int a = (height - width) / 2;
        squareImg = img.getSubimage(0, a, size, size);
      }
    } else {
      squareImg = img;
    }
    return squareImg;
  }

  /**
   * Scale image.
   *
   * @param img the img
   * @param dimension the dimension
   * @return the buffered image
   */
  BufferedImage scaleImage(BufferedImage img, Dimension dimension);

}
