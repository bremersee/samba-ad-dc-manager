package org.bremersee.samba.ad.dc.repository.tools.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.repository.tools.cli.CommandExecutorResponse;

public class DomainComputerDeleteValidator extends SambaToolValidator {

  private final String samAccountName;

  public DomainComputerDeleteValidator(String samAccountName) {
    this.samAccountName = samAccountName;
  }

  @Override
  String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Deleting computer '%s' failed. %s",
        samAccountName,
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  String getErrorCode() {
    return ErrorCode.EC_DELETING_COMPUTER_FAILED;
  }
}
