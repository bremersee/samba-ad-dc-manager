package org.bremersee.samba.ad.dc.samaccount.group.repository.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.common.repository.cli.validator.SambaToolValidator;
import org.bremersee.samba.ad.dc.samaccount.group.model.DomainGroup;
import org.bremersee.samba.ad.dc.common.repository.cli.CommandExecutorResponse;
import org.ldaptive.dn.Dn;
import org.springframework.util.Assert;

public class GroupMoveValidator extends SambaToolValidator {

  private final DomainGroup domainGroup;

  private final Dn newOu;

  public GroupMoveValidator(DomainGroup domainGroup, Dn newOu) {
    Assert.notNull(domainGroup, "Domain group cannot be null.");
    Assert.notNull(newOu, "New organizational unit cannot be null.");
    this.domainGroup = domainGroup;
    this.newOu = newOu;
  }

  @Override
  protected String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Moving group '%s' to '%s' failed. %s",
        domainGroup.getSamAccountName(),
        newOu.format(),
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  protected String getErrorCode() {
    return ErrorCode.EC_UPDATING_GROUP_FAILED;
  }

}
