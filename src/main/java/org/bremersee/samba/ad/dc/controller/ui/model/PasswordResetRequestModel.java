package org.bremersee.samba.ad.dc.controller.ui.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class PasswordResetRequestModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String username;

}
