package org.bremersee.samba.ad.dc.repository.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;

public class ComputerDeleteValidator extends SambaToolValidator {

  private final String samAccountName;

  public ComputerDeleteValidator(String samAccountName) {
    this.samAccountName = samAccountName;
  }

  @Override
  protected String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Deleting computer '%s' failed. %s",
        samAccountName,
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  protected String getErrorCode() {
    return ErrorCode.EC_DELETING_COMPUTER_FAILED;
  }
}
