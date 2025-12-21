package org.bremersee.samba.ad.dc.controller.ui.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DnsZoneDeleteModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String verificationName;

}
