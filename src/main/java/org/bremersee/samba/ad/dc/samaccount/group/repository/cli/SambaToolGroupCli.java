package org.bremersee.samba.ad.dc.samaccount.group.repository.cli;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.common.repository.cli.CommandExecutor;
import org.bremersee.samba.ad.dc.common.repository.cli.SambaToolCli;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.samaccount.common.model.NisDomainMember;
import org.bremersee.samba.ad.dc.samaccount.group.model.DomainGroup;
import org.bremersee.samba.ad.dc.samaccount.group.model.DomainGroupType;
import org.bremersee.samba.ad.dc.samaccount.group.model.DomainGroupTypeContainer;
import org.bremersee.samba.ad.dc.samaccount.group.repository.SambaToolGroup;
import org.bremersee.samba.ad.dc.samaccount.group.repository.cli.validator.GroupAddValidator;
import org.bremersee.samba.ad.dc.samaccount.group.repository.cli.validator.GroupDeleteValidator;
import org.bremersee.samba.ad.dc.samaccount.group.repository.cli.validator.GroupMoveValidator;
import org.bremersee.samba.ad.dc.samaccount.group.repository.cli.validator.GroupRenameValidator;
import org.ldaptive.dn.Dn;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class SambaToolGroupCli extends SambaToolCli implements SambaToolGroup {

  SambaToolGroupCli(DomainControllerProperties properties) {
    super(properties);
  }

  @Override
  protected String getSubCommand() {
    return "group";
  }

  @Override
  public void addGroup(DomainGroup domainGroup, Dn ou, Boolean isRfc2307Enabled) {
    List<String> commands = getCommands();
    commands.add("add");
    commands.add(quote(domainGroup.getSamAccountName()));
    Optional.ofNullable(domainGroup.getGroupType())
        .map(DomainGroupTypeContainer::getGroupType)
        .map(DomainGroupType::getScope)
        .ifPresent(scope -> commands.add("--group-scope=" + scope));
    Optional.ofNullable(domainGroup.getGroupType())
        .map(DomainGroupTypeContainer::getGroupType)
        .map(DomainGroupType::getPurpose)
        .ifPresent(purpose -> commands.add("--group-type=" + purpose));
    Optional.ofNullable(getDnTool().removeBaseDn(ou))
        .map(Dn::format)
        .map(CommandExecutor::quote)
        .ifPresent(dn -> commands.add("--groupou=" + dn));
    if (Boolean.TRUE.equals(isRfc2307Enabled) && hasAllNisAttributes(domainGroup)) {
      commands.add("--nis-domain=" + quote(domainGroup.getNisDomain()));
      commands.add("--gid-number=" + domainGroup.getGidNumber());
    }
    execute(commands, new GroupAddValidator(domainGroup));
  }

  @Override
  public DomainGroup renameAndMoveGroup(
      DomainGroup oldDomainGroup,
      DomainGroup newDomainGroup,
      Dn newDn) {

    String oldCn = new Dn(oldDomainGroup.getDistinguishedName())
        .getRDn().getNameValue().getStringValue();
    String newCn = newDn
        .getRDn().getNameValue().getStringValue();
    boolean cnChanged = !Objects.equals(oldCn, newCn);
    Dn oldParentDn = oldDomainGroup.getDn().getParent();

    String oldSamAccountName = oldDomainGroup.getSamAccountName();
    String newSamAccountName = newDomainGroup.getSamAccountName();
    boolean samAccountNameChanged = !Objects.equals(oldSamAccountName, newSamAccountName);

    String oldEmail = oldDomainGroup.getEmail();
    String newEmail = newDomainGroup.getEmail();
    boolean emailChanged = !Objects.equals(oldEmail, newEmail);

    if (cnChanged || samAccountNameChanged || emailChanged) {
      List<String> commands = getCommands();
      commands.add("rename");
      commands.add(quote(oldSamAccountName));
      if (samAccountNameChanged) {
        commands.add("--samaccountname=" + quote(newSamAccountName));
      }
      if (cnChanged) {
        commands.add("--force-new-cn=" + quote(newCn));
      }
      if (emailChanged) {
        commands.add(" --mail-address=" + quote(oldEmail));
      }
      execute(commands, new GroupRenameValidator(oldDomainGroup, newDomainGroup));
    }
    Dn newParentDn = newDn.getParent();
    if (!oldParentDn.isSame(newParentDn)) {
      String ou = getDnTool().removeBaseDn(newParentDn).format();
      List<String> commands = getCommands();
      commands.add("move");
      commands.add(quote(newSamAccountName));
      commands.add(quote(ou));
      execute(commands, new GroupMoveValidator(newDomainGroup, newParentDn));
    }
    // TODO make method void
    return DomainGroup.builder()
        .from(newDomainGroup)
        .distinguishedName(newDn.format(rdn -> rdn))
        .build();
  }

  @Override
  public void deleteGroup(String samAccountName) {
    List<String> commands = getCommands();
    commands.add("delete");
    commands.add(quote(samAccountName));
    execute(commands, new GroupDeleteValidator(samAccountName));
  }

  boolean hasAllNisAttributes(DomainGroup domainGroup) {
    return !isEmpty(domainGroup)
        && !isEmpty(getNisDomain(domainGroup))
        && !isEmpty(domainGroup.getGidNumber());
  }

  String getNisDomain(NisDomainMember nisDomainMember) {
    return Optional.ofNullable(nisDomainMember)
        .map(NisDomainMember::getNisDomain)
        .orElseGet(() -> getProperties().getDomain().getDefaultNisDomain());
  }

}
