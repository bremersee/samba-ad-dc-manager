package org.bremersee.samba.ad.dc.samaccount.user.repository.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.common.repository.cli.validator.SambaToolValidator;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.common.repository.cli.CommandExecutorResponse;
import org.ldaptive.dn.Dn;
import org.springframework.util.Assert;

public class UserMoveValidator extends SambaToolValidator {

  private final DomainUser domainUser;

  private final Dn newOu;

  public UserMoveValidator(DomainUser domainUser, Dn newOu) {
    Assert.notNull(domainUser, "Domain user cannot be null.");
    Assert.notNull(newOu, "New organizational unit cannot be null.");
    this.domainUser = domainUser;
    this.newOu = newOu;
  }

  @Override
  protected String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Moving user '%s' to '%s' failed. %s",
        domainUser.getSamAccountName(),
        newOu.format(),
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  protected String getErrorCode() {
    return ErrorCode.EC_UPDATING_USER_FAILED;
  }

}
