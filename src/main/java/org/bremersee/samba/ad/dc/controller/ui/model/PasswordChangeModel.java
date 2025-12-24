package org.bremersee.samba.ad.dc.controller.ui.model;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeModel extends PasswordResetModel {

  @Serial
  private static final long serialVersionUID = 1L;

  private String username;

  private String oldPassword;

}
