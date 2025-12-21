package org.bremersee.samba.ad.dc.samaccount.user.repository.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.repository.cli.validator.SambaToolValidator;
import org.bremersee.samba.ad.dc.samaccount.user.model.DomainUser;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.springframework.util.Assert;

public class UserAddValidator extends SambaToolValidator {

  private final DomainUser domainUser;

  public UserAddValidator(DomainUser domainUser) {
    Assert.notNull(domainUser, "Domain user must not be null.");
    this.domainUser = domainUser;
  }

  @Override
  protected String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Adding user '%s' failed. %s",
        domainUser.getSamAccountName(),
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  protected String getErrorCode() {
    return ErrorCode.EC_ADDING_USER_FAILED;
  }

}
