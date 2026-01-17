package org.bremersee.samba.ad.dc.controller.ui.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import lombok.Data;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.ldaptive.dn.Dn;

/**
 * The computer edit model.
 */
@Data
public class ComputerEditModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The new distinguished name of organization unit.
   */
  private String newOu;

  /**
   * The description of the computer.
   */
  private String description;

  /**
   * Instantiates a new computer edit model.
   */
  public ComputerEditModel() {
    super();
  }

  /**
   * Gets new distinguished name of organization unit.
   *
   * @return the new distinguished name of organization unit
   */
  public Optional<Dn> getNewOuDn() {
    return Optional.ofNullable(newOu)
        .filter(DnTool::isValidDn)
        .map(Dn::new);
  }

}
