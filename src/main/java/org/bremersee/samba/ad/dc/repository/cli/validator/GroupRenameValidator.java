package org.bremersee.samba.ad.dc.repository.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.springframework.util.Assert;

public class GroupRenameValidator extends SambaToolValidator {

  private final DomainGroup oldDomainGroup;

  private final DomainGroup newDomainGroup;

  public GroupRenameValidator(DomainGroup oldDomainGroup, DomainGroup newDomainGroup) {
    Assert.notNull(oldDomainGroup, "Old Domain Group is null.");
    Assert.notNull(newDomainGroup, "New Domain Group is null.");
    this.oldDomainGroup = oldDomainGroup;
    this.newDomainGroup = newDomainGroup;
  }

  @Override
  protected String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Renaming group '%s' to '%s' failed. %s",
        oldDomainGroup.getSamAccountName(),
        newDomainGroup.getSamAccountName(),
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  protected String getErrorCode() {
    return ErrorCode.EC_UPDATING_GROUP_FAILED;
  }

}
