package org.bremersee.samba.ad.dc.controller.ui.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import org.bremersee.samba.ad.dc.model.DomainUser;

/**
 * The profile edit model.
 */
@Data
public class ProfileEditModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The new email.
   */
  private String newEmail;

  /**
   * Instantiates a new profile edit model.
   *
   * @param user the user
   */
  public ProfileEditModel(DomainUser user) {
    this.newEmail = user.getEmail();
  }
}
