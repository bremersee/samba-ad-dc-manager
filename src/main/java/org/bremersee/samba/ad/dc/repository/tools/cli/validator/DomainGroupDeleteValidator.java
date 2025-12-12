package org.bremersee.samba.ad.dc.repository.tools.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.common.repository.cli.CommandExecutorResponse;
import org.bremersee.samba.ad.dc.common.repository.cli.validator.SambaToolValidator;

public class DomainGroupDeleteValidator extends SambaToolValidator {

  private final String samAccountName;

  public DomainGroupDeleteValidator(String samAccountName) {
    this.samAccountName = samAccountName;
  }

  @Override
  protected String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Deleting group '%s' failed. %s",
        samAccountName,
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  protected String getErrorCode() {
    return ErrorCode.EC_DELETING_GROUP_FAILED;
  }
}
