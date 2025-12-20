package org.bremersee.samba.ad.dc.samaccount.computer.controller.ui.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bremersee.samba.ad.dc.common.DnTool;
import org.ldaptive.dn.Dn;

@Data
@NoArgsConstructor
public class ComputerEditModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String newOu;

  private String description;

  public Optional<Dn> getNewOuDn() {
    return Optional.ofNullable(newOu)
        .filter(DnTool::isValidDn)
        .map(Dn::new);
  }

}
