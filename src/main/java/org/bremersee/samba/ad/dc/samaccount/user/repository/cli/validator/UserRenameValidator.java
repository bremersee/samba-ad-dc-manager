package org.bremersee.samba.ad.dc.samaccount.user.repository.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.common.repository.cli.validator.SambaToolValidator;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.common.repository.cli.CommandExecutorResponse;
import org.springframework.util.Assert;

public class UserRenameValidator extends SambaToolValidator {

  private final DomainUser oldDomainUser;

  private final DomainUser newDomainUser;

  public UserRenameValidator(DomainUser oldDomainUser, DomainUser newDomainUser) {
    Assert.notNull(oldDomainUser, "Old Domain User is null.");
    Assert.notNull(newDomainUser, "New Domain User is null.");
    this.oldDomainUser = oldDomainUser;
    this.newDomainUser = newDomainUser;
  }

  @Override
  protected String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Renaming user '%s' to '%s' failed. %s",
        oldDomainUser.getSamAccountName(),
        newDomainUser.getSamAccountName(),
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  protected String getErrorCode() {
    return ErrorCode.EC_UPDATING_USER_FAILED;
  }

}
