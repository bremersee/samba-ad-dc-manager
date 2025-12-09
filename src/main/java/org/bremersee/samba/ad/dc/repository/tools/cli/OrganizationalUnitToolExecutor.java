package org.bremersee.samba.ad.dc.repository.tools.cli;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.List;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.bremersee.samba.ad.dc.repository.tools.OrganizationalUnitTool;
import org.bremersee.samba.ad.dc.repository.tools.cli.validator.OrganizationalUnitAddValidator;
import org.bremersee.samba.ad.dc.repository.tools.cli.validator.OrganizationalUnitDeleteValidator;
import org.bremersee.samba.ad.dc.repository.tools.cli.validator.OrganizationalUnitMoveValidator;
import org.bremersee.samba.ad.dc.repository.tools.cli.validator.OrganizationalUnitRenameValidator;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.NameValue;
import org.ldaptive.dn.RDn;
import org.springframework.stereotype.Component;

@Component
class OrganizationalUnitToolExecutor extends SambaToolExecutor
    implements OrganizationalUnitTool {

  OrganizationalUnitToolExecutor(DomainControllerProperties properties) {
    super(properties);
  }

  @Override
  String getSubCommand() {
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
    execute(commands, new OrganizationalUnitAddValidator(dn));
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
    execute(commands, new OrganizationalUnitMoveValidator(oldDn, newParentOu));
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
    execute(commands, new OrganizationalUnitRenameValidator(ou, newName));
    return newDn;
  }

  @Override
  public void deleteOrganizationalUnit(Dn ou) {
    List<String> commands = getCommands();
    commands.add("delete");
    commands.add(quote(ou.format()));
    execute(commands, new OrganizationalUnitDeleteValidator(ou));
  }
}
