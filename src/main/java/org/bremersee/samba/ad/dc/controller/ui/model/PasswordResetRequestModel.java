package org.bremersee.samba.ad.dc.controller.ui.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * The password reset request model.
 */
@Data
public class PasswordResetRequestModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The username.
   */
  private String username;

  /**
   * Instantiates a new password reset request model.
   */
  public PasswordResetRequestModel() {
    super();
  }
}
