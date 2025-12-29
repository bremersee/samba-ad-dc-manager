package org.bremersee.samba.ad.dc.service;

import jakarta.validation.constraints.NotNull;

public interface CryptoService<S, T> {

  @NotNull
  T encrypt(@NotNull S passwordReset);

  @NotNull
  S decrypt(@NotNull T encryptedPasswordReset);

}
