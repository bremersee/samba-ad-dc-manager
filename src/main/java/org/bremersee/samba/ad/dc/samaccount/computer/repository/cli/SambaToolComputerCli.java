package org.bremersee.samba.ad.dc.samaccount.computer.repository.cli;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.common.repository.cli.SambaToolCli;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.samaccount.computer.model.DomainComputer;
import org.bremersee.samba.ad.dc.samaccount.computer.repository.SambaToolComputer;
import org.bremersee.samba.ad.dc.samaccount.computer.repository.cli.validator.ComputerDeleteValidator;
import org.bremersee.samba.ad.dc.samaccount.computer.repository.cli.validator.ComputerMoveValidator;
import org.ldaptive.dn.Dn;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class SambaToolComputerCli extends SambaToolCli implements SambaToolComputer {

  SambaToolComputerCli(DomainControllerProperties properties) {
    super(properties);
  }

  @Override
  protected String getSubCommand() {
    return "computer";
  }


  @Override
  public DomainComputer moveComputer(DomainComputer domainComputer, Dn newOu) {
    String ou = Optional.ofNullable(getDnTool().removeBaseDn(newOu))
        .map(Dn::format)
        .orElse(null);
    if (ou == null) {
      return domainComputer;
    }
    List<String> commands = getCommands();
    commands.add("move");
    commands.add(quote(domainComputer.getSamAccountNameWithoutTrailingDollarSign()));
    commands.add(quote(ou));
    execute(commands, new ComputerMoveValidator(domainComputer, newOu));
    Dn newDn = new Dn(domainComputer.getDn().getRDn());
    newDn.add(newOu);
    // TODO make method void
    return DomainComputer.builder()
        .from(domainComputer)
        .distinguishedName(getDnTool().addBaseDn(newDn).format(rdn -> rdn))
        .build();
  }

  @Override
  public void deleteComputer(DomainComputer domainComputer) {
    String samAccountName = domainComputer.getSamAccountNameWithoutTrailingDollarSign();
    List<String> commands = getCommands();
    commands.add("delete");
    commands.add(quote(samAccountName));
    execute(commands, new ComputerDeleteValidator(samAccountName));
  }
}
