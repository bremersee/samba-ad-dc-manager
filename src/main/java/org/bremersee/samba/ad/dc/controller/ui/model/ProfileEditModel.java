package org.bremersee.samba.ad.dc.controller.ui.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import org.bremersee.samba.ad.dc.model.DomainUser;

@Data
public class ProfileEditModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String newEmail;

  public ProfileEditModel() {
    super();
  }

  public ProfileEditModel(DomainUser user) {
    this.newEmail = user.getEmail();
  }
}
