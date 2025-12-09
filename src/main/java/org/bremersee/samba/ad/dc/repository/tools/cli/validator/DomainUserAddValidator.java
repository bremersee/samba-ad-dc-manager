package org.bremersee.samba.ad.dc.repository.tools.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.repository.tools.cli.CommandExecutorResponse;
import org.springframework.util.Assert;

public class DomainUserAddValidator extends SambaToolValidator {

  private final DomainUser domainUser;

  public DomainUserAddValidator(DomainUser domainUser) {
    Assert.notNull(domainUser, "Domain user must not be null.");
    this.domainUser = domainUser;
  }

  @Override
  String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Adding user '%s' failed. %s",
        domainUser.getSamAccountName(),
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  String getErrorCode() {
    return ErrorCode.EC_ADDING_USER_FAILED;
  }

}
