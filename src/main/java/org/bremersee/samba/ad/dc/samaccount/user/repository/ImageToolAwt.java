package org.bremersee.samba.ad.dc.samaccount.user.repository;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import org.springframework.stereotype.Component;

/**
 * The internal (java.awt) image tool.
 */
@Component
public class ImageToolAwt implements ImageTool {

  /**
   * Instantiates a new default image tool.
   */
  public ImageToolAwt() {
    super();
  }

  @Override
  public BufferedImage scaleImage(BufferedImage img, Dimension dimension) {
    img = scaleByHalf(img, dimension);
    img = scaleExact(img, dimension);
    return img;
  }

  private static BufferedImage scaleByHalf(BufferedImage img, Dimension dimension) {
    int w = img.getWidth();
    int h = img.getHeight();
    float factor = getBinFactor(w, h, dimension);

    // make new size
    w = (int) (w * factor);
    h = (int) (h * factor);
    BufferedImage scaled = new BufferedImage(w, h,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D g = scaled.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    g.drawImage(img, 0, 0, w, h, null);
    g.dispose();
    return scaled;
  }

  private static BufferedImage scaleExact(BufferedImage img, Dimension dimension) {
    float factor = getFactor(img.getWidth(), img.getHeight(), dimension);

    // create the image
    int w = (int) (img.getWidth() * factor);
    int h = (int) (img.getHeight() * factor);
    BufferedImage scaled = new BufferedImage(w, h,
        BufferedImage.TYPE_INT_RGB);

    Graphics2D g = scaled.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(img, 0, 0, w, h, null);
    g.dispose();
    return scaled;
  }

  private static float getBinFactor(int width, int height, Dimension dimension) {
    float factor = 1;
    float target = getFactor(width, height, dimension);
    if (target <= 1) {
      while (factor / 2 > target) {
        factor /= 2;
      }
    } else {
      while (factor * 2 < target) {
        factor *= 2;
      }
    }
    return factor;
  }

  private static float getFactor(int width, int height, Dimension dimension) {
    float sx = dimension.width / (float) width;
    float sy = dimension.height / (float) height;
    return Math.min(sx, sy);
  }
}
