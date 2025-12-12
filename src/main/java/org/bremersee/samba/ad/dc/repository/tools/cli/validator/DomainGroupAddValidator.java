package org.bremersee.samba.ad.dc.repository.tools.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.common.repository.cli.validator.SambaToolValidator;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.common.repository.cli.CommandExecutorResponse;
import org.springframework.util.Assert;

public class DomainGroupAddValidator extends SambaToolValidator {

  private final DomainGroup domainGroup;

  public DomainGroupAddValidator(DomainGroup domainGroup) {
    Assert.notNull(domainGroup, "Domain group is required.");
    this.domainGroup = domainGroup;
  }

  @Override
  protected String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Adding group '%s' failed. %s",
        domainGroup.getSamAccountName(),
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  protected String getErrorCode() {
    return ErrorCode.EC_ADDING_GROUP_FAILED;
  }
}
