package org.bremersee.samba.ad.dc.controller.ui.model;

import java.io.Serial;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PasswordChangeModel extends PasswordResetModel {

  @Serial
  private static final long serialVersionUID = 1L;

  private String oldPassword;

  @Override
  public String toString() {
    return "PasswordChangeModel{"
        + "username='" + getUsername() + '\''
        + '}';
  }

}
