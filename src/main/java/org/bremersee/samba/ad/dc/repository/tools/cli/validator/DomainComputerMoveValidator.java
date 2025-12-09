package org.bremersee.samba.ad.dc.repository.tools.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.repository.tools.cli.CommandExecutorResponse;
import org.ldaptive.dn.Dn;
import org.springframework.util.Assert;

public class DomainComputerMoveValidator extends SambaToolValidator {

  private final DomainComputer domainComputer;

  private final Dn newOu;

  public DomainComputerMoveValidator(DomainComputer domainComputer, Dn newOu) {
    Assert.notNull(domainComputer, "Domain computer cannot be null.");
    Assert.notNull(newOu, "New organizational unit cannot be null.");
    this.domainComputer = domainComputer;
    this.newOu = newOu;
  }

  @Override
  String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Moving computer '%s' to '%s' failed. %s",
        domainComputer.getSamAccountName(),
        newOu.format(),
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  String getErrorCode() {
    return ErrorCode.EC_UPDATING_COMPUTER_FAILED;
  }

}
