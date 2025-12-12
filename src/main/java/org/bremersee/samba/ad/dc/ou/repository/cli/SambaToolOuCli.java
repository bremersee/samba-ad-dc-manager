package org.bremersee.samba.ad.dc.ou.repository.cli;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.List;
import org.bremersee.samba.ad.dc.common.repository.cli.SambaToolCli;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.bremersee.samba.ad.dc.ou.repository.SambaToolOu;
import org.bremersee.samba.ad.dc.ou.repository.cli.validator.OuAddValidator;
import org.bremersee.samba.ad.dc.ou.repository.cli.validator.OuDeleteValidator;
import org.bremersee.samba.ad.dc.ou.repository.cli.validator.OuMoveValidator;
import org.bremersee.samba.ad.dc.ou.repository.cli.validator.OuRenameValidator;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.NameValue;
import org.ldaptive.dn.RDn;
import org.springframework.stereotype.Component;

@Component
class SambaToolOuCli extends SambaToolCli
    implements SambaToolOu {

  SambaToolOuCli(DomainControllerProperties properties) {
    super(properties);
  }

  @Override
  protected String getSubCommand() {
    return "ou";
  }

  @Override
  public void addOrganizationalUnit(OrganizationalUnit organizationalUnit, Dn parentOu) {
    Dn dn = new Dn(new RDn(new NameValue(
        AdConstants.RDN_ATTR_NAME_OU,
        organizationalUnit.getName())));
    if (!isEmpty(parentOu) && !parentOu.isEmpty()) {
      dn.add(parentOu);
    }
    List<String> commands = getCommands();
    commands.add("add");
    commands.add(quote(dn.format(rdn -> rdn)));
    if (!isEmpty(organizationalUnit.getDescription())) {
      commands.add("--description=" + quote(organizationalUnit.getDescription()));
    }
    execute(commands, new OuAddValidator(dn));
  }

  @Override
  public Dn moveOrganizationalUnit(Dn oldDn, Dn newParentOu) {
    Dn newDn = new Dn(new RDn(new NameValue(
        AdConstants.RDN_ATTR_NAME_OU,
        oldDn.getRDn().getNameValue().getStringValue())));
    newDn.add(newParentOu);
    List<String> commands = getCommands();
    commands.add("move");
    commands.add(quote(oldDn.format()));
    commands.add(quote(newParentOu.format()));
    execute(commands, new OuMoveValidator(oldDn, newParentOu));
    return newDn;
  }

  @Override
  public Dn renameOrganizationalUnit(Dn ou, String newName) {
    Dn newDn = new Dn(new RDn(new NameValue(AdConstants.RDN_ATTR_NAME_OU, newName)));
    newDn.add(ou.getParent());
    List<String> commands = getCommands();
    commands.add("rename");
    commands.add(quote(ou.format()));
    commands.add(quote(newDn.format(rdn -> rdn)));
    execute(commands, new OuRenameValidator(ou, newName));
    return newDn;
  }

  @Override
  public void deleteOrganizationalUnit(Dn ou) {
    List<String> commands = getCommands();
    commands.add("delete");
    commands.add(quote(ou.format()));
    execute(commands, new OuDeleteValidator(ou));
  }
}
