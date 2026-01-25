package org.bremersee.samba.ad.dc.controller.ui.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import org.bremersee.samba.ad.dc.model.DomainUser;

/**
 * The password reset model.
 */
@Data
public class PasswordResetModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The username.
   */
  private String username;

  /**
   * The new password.
   */
  private String newPassword;

  /**
   * The repetition of the new password.
   */
  private String newPasswordRepetition;

  /**
   * Instantiates a new password reset model.
   */
  public PasswordResetModel() {
    super();
  }

  /**
   * Instantiates a new password reset model.
   *
   * @param domainUser the domain user
   */
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
