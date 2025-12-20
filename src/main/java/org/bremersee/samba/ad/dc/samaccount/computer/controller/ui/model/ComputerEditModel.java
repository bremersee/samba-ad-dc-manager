package org.bremersee.samba.ad.dc.samaccount.computer.controller.ui.model;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ldaptive.dn.Dn;

@Data
@NoArgsConstructor
public class ComputerEditModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String newOu;

  private String description;

  public Dn getNewOuDn() {
    if (isEmpty(newOu)) {
      return null;
    }
    return new Dn(newOu);
  }

}
