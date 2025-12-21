package org.bremersee.samba.ad.dc.samaccount.computer.model;

import org.bremersee.pagebuilder.model.JsonPageDto;
import org.springframework.data.domain.Page;

public class DomainComputerPage extends JsonPageDto<DomainComputer> {

  public DomainComputerPage() {
    super();
  }

  public DomainComputerPage(Page<? extends DomainComputer> page) {
    super(page);
  }
}
