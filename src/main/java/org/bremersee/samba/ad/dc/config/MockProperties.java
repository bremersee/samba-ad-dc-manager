package org.bremersee.samba.ad.dc.config;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class MockProperties implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String administratorPassword = "demo";

}
