package org.bremersee.samba.ad.dc.repository;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

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
