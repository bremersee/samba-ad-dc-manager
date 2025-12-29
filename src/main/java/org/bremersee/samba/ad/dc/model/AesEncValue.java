package org.bremersee.samba.ad.dc.model;

import java.io.Serial;
import java.io.Serializable;

public record AesEncValue(String encryptedValue, String salt) implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

}
