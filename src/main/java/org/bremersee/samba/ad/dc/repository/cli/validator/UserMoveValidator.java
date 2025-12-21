package org.bremersee.samba.ad.dc.repository.cli.validator;

import java.util.Optional;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.ldaptive.dn.Dn;
import org.springframework.util.Assert;

public class UserMoveValidator extends SambaToolValidator {

  private final DomainUser domainUser;

  private final Dn newOu;

  public UserMoveValidator(DomainUser domainUser, Dn newOu) {
    Assert.notNull(domainUser, "Domain user cannot be null.");
    this.domainUser = domainUser;
    this.newOu = newOu;
  }

  @Override
  protected String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Moving user '%s' to '%s' failed. %s",
        domainUser.getSamAccountName(),
        Optional.ofNullable(newOu).map(Dn::format).orElse(null),
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  protected String getErrorCode() {
    return ErrorCode.EC_UPDATING_USER_FAILED;
  }

}
