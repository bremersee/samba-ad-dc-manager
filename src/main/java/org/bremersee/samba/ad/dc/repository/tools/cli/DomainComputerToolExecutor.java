package org.bremersee.samba.ad.dc.repository.tools.cli;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.repository.tools.DomainComputerTool;
import org.bremersee.samba.ad.dc.repository.tools.cli.validator.DomainComputerDeleteValidator;
import org.bremersee.samba.ad.dc.repository.tools.cli.validator.DomainComputerMoveValidator;
import org.ldaptive.dn.Dn;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class DomainComputerToolExecutor extends SambaToolExecutor implements DomainComputerTool {

  DomainComputerToolExecutor(DomainControllerProperties properties) {
    super(properties);
  }

  @Override
  String getSubCommand() {
    return "computer";
  }


  @Override
  public DomainComputer moveComputer(DomainComputer domainComputer, Dn newOu) {
    String ou = getProperties().removeBaseDn(newOu).format();
    List<String> commands = getCommands();
    commands.add("move");
    commands.add(quote(domainComputer.getSamAccountNameWithoutTrailingDollarSign()));
    commands.add(quote(ou));
    execute(commands, new DomainComputerMoveValidator(domainComputer, newOu));
    Dn newDn = new Dn(domainComputer.getDn().getRDn());
    newDn.add(newOu);
    domainComputer.setDn(getProperties().getBaseDn(newDn));
    return domainComputer;
  }

  @Override
  public void deleteComputer(DomainComputer domainComputer) {
    String samAccountName = domainComputer.getSamAccountNameWithoutTrailingDollarSign();
    List<String> commands = getCommands();
    commands.add("delete");
    commands.add(quote(samAccountName));
    execute(commands, new DomainComputerDeleteValidator(samAccountName));
  }
}
