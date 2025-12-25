package org.bremersee.samba.ad.dc.controller.ui.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import org.bremersee.samba.ad.dc.model.DomainUser;

@Data
public class PasswordResetModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String username;

  private String newPassword;

  private String newPasswordRepetition;

  public PasswordResetModel() {
  }

  public PasswordResetModel(DomainUser domainUser) {
    setUsername(domainUser.getSamAccountName());
  }

  @Override
  public String toString() {
    return "PasswordResetModel{"
        + "username='" + getUsername() + '\''
        + '}';
  }

}
