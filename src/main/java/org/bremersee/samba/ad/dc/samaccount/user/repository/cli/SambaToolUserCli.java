package org.bremersee.samba.ad.dc.samaccount.user.repository.cli;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.common.repository.cli.SambaToolCli;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.NisDomainMember;
import org.bremersee.samba.ad.dc.samaccount.user.repository.SambaToolUser;
import org.bremersee.samba.ad.dc.samaccount.user.repository.cli.validator.UserAddValidator;
import org.bremersee.samba.ad.dc.samaccount.user.repository.cli.validator.UserDeleteValidator;
import org.bremersee.samba.ad.dc.samaccount.user.repository.cli.validator.UserMoveValidator;
import org.bremersee.samba.ad.dc.samaccount.user.repository.cli.validator.UserRenameValidator;
import org.ldaptive.dn.Dn;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SambaToolUserCli extends SambaToolCli implements SambaToolUser {

  public SambaToolUserCli(DomainControllerProperties properties) {
    super(properties);
  }

  @Override
  protected String getSubCommand() {
    return "user";
  }

  @Override
  public void addUser(
      DomainUser domainUser,
      Dn ou,
      Boolean useUsernameAsCn,
      Boolean isRfc2307Enabled) {

    boolean usernameAsCn = requireNonNullElse(
        useUsernameAsCn, getProperties().getUser().isUseUsernameAsCn());
    log.debug("addUser({}, {}, {})", domainUser.getSamAccountName(), ou, usernameAsCn);
    List<String> commands = getCommands();
    commands.add("add");
    commands.add(quote(domainUser.getSamAccountName()));
    commands.add("--random-password");
    if (!isEmpty(ou)) {
      commands.add("--userou=" + quote(ou.format()));
    }
    if (usernameAsCn || isEmpty(domainUser.getFirstName()) || isEmpty(domainUser.getLastName())) {
      commands.add("--use-username-as-cn");
    }
    if (!isEmpty(domainUser.getLastName())) {
      commands.add("--surname=" + quote(domainUser.getLastName()));
    }
    if (!isEmpty(domainUser.getFirstName())) {
      commands.add("--given-name=" + quote(domainUser.getFirstName()));
    }
    if (Boolean.TRUE.equals(isRfc2307Enabled) && hasAllNisAttributes(domainUser)) {
      commands.add("--nis-domain=" + quote(getNisDomain(domainUser)));
      commands.add("--uidNumber=" + domainUser.getUidNumber());
      commands.add("--login-shell=" + quote(domainUser.getLoginShell()));
      commands.add("--unix-home=" + quote(domainUser.getUnixHomeDirectory()));
      commands.add("--gid-number=" + domainUser.getGidNumber());
      commands.add("--uid=" + quote(domainUser.getUid()));
    }
    execute(commands, new UserAddValidator(domainUser));
  }

  @Override
  public DomainUser renameAndMoveUser(
      DomainUser oldDomainUser,
      DomainUser newDomainUser,
      Dn newDn) {

    String oldCn = new Dn(oldDomainUser.getDistinguishedName())
        .getRDn().getNameValue().getStringValue();
    String newCn = newDn
        .getRDn().getNameValue().getStringValue();
    Dn oldParentDn = oldDomainUser.getDn().getParent();
    String oldSamAccountName = oldDomainUser.getSamAccountName();
    String newSamAccountName = newDomainUser.getSamAccountName();
    if (!oldCn.equals(newCn) || !oldSamAccountName.equals(newSamAccountName)) {
      List<String> commands = getCommands();
      commands.add("rename");
      commands.add(quote(oldSamAccountName));
      commands.add("--samaccountname=" + quote(newSamAccountName));
      commands.add("--force-new-cn=" + quote(newCn));
      execute(commands, new UserRenameValidator(oldDomainUser, newDomainUser));
    }
    Dn newParentDn = newDn.getParent();
    if (!oldParentDn.isSame(newParentDn)) {
      String ou = getProperties().removeBaseDn(newParentDn).format();
      List<String> commands = getCommands();
      commands.add("move");
      commands.add(quote(newSamAccountName));
      commands.add(quote(ou));
      execute(commands, new UserMoveValidator(newDomainUser, newParentDn));
    }
    return DomainUser.builder()
        .from(newDomainUser)
        .distinguishedName(newDn.format(rdn -> rdn))
        .build();
  }

  @Override
  public void deleteUser(String samAccountName) {
    List<String> commands = getCommands();
    commands.add("delete");
    commands.add(quote(samAccountName));
    execute(commands, new UserDeleteValidator(samAccountName));
  }

  boolean hasAllNisAttributes(DomainUser domainUser) {
    return !isEmpty(domainUser)
        && !isEmpty(getNisDomain(domainUser))
        && !isEmpty(domainUser.getUid())
        && !isEmpty(domainUser.getUidNumber())
        && !isEmpty(domainUser.getLoginShell())
        && !isEmpty(domainUser.getUnixHomeDirectory())
        && !isEmpty(domainUser.getGidNumber());
  }

  String getNisDomain(NisDomainMember nisDomainMember) {
    return Optional.ofNullable(nisDomainMember)
        .map(NisDomainMember::getNisDomain)
        .orElseGet(() -> getProperties().getDomain().getDefaultNisDomain());
  }

}
