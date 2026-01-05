package org.bremersee.samba.ad.dc.model;

import java.io.Serial;
import org.bremersee.pagebuilder.model.JsonPageDto;
import org.springframework.data.domain.Page;

public class DomainComputerPage extends JsonPageDto<DomainComputer> {

  @Serial
  private static final long serialVersionUID = 1;

  public DomainComputerPage(Page<? extends DomainComputer> page) {
    super(page);
  }

}
