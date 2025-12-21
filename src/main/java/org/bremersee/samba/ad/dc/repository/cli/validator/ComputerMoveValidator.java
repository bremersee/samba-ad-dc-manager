package org.bremersee.samba.ad.dc.repository.cli.validator;

import java.util.Optional;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.ldaptive.dn.Dn;
import org.springframework.util.Assert;

public class ComputerMoveValidator extends SambaToolValidator {

  private final DomainComputer domainComputer;

  private final Dn newOu;

  public ComputerMoveValidator(DomainComputer domainComputer, Dn newOu) {
    Assert.notNull(domainComputer, "Domain computer cannot be null.");
    this.domainComputer = domainComputer;
    this.newOu = newOu;
  }

  @Override
  protected String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Moving computer '%s' to '%s' failed. %s",
        domainComputer.getSamAccountName(),
        Optional.ofNullable(newOu).map(Dn::format).orElse(null),
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  protected String getErrorCode() {
    return ErrorCode.EC_UPDATING_COMPUTER_FAILED;
  }

}
