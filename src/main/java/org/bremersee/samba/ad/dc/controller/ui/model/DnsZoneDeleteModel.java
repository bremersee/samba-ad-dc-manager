package org.bremersee.samba.ad.dc.controller.ui.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * The dns zone delete model.
 */
@Data
public class DnsZoneDeleteModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The verification name.
   */
  private String verificationName;

  /**
   * Instantiates a new dns zone delete model.
   */
  public DnsZoneDeleteModel() {
    super();
  }
}
