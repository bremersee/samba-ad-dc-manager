package org.bremersee.samba.ad.dc.service;

import jakarta.validation.constraints.NotNull;
import org.bremersee.samba.ad.dc.model.PasswordReset;

public interface PasswordResetCryptoService<T> {

  @NotNull
  T encrypt(@NotNull PasswordReset passwordReset);

  @NotNull
  PasswordReset decrypt(@NotNull T encryptedPasswordReset);

}
