package org.bremersee.samba.ad.dc.config;

import java.io.Serial;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class AesCryptoProperties implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String secret = "change-it";

  private String saltHex = "53f4d31f3ff7cba8";

  @Override
  public String toString() {
    return "AesCryptoProperties{"
        + "secret='****'"
        + ", saltHex='****'"
        + '}';
  }
}
