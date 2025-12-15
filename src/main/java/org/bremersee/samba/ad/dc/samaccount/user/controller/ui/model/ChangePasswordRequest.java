package org.bremersee.samba.ad.dc.samaccount.user.controller.ui.model;

import lombok.Data;

@Data
public class ChangePasswordRequest {

  private String username;

  private String oldPassword;

  private String newPassword;

  private String newPasswordRepetition;

}
