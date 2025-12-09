package org.bremersee.samba.ad.dc.repository.tools.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.repository.tools.cli.CommandExecutorResponse;
import org.ldaptive.dn.Dn;
import org.springframework.util.Assert;

public class OrganizationalUnitRenameValidator extends SambaToolValidator {

  private final Dn organizationalUnit;

  private final String newName;

  public OrganizationalUnitRenameValidator(Dn organizationalUnit, String newName) {
    Assert.notNull(organizationalUnit, "Organizational unit must not be null.");
    this.organizationalUnit = organizationalUnit;
    this.newName = newName;
  }

  @Override
  String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Renaming organizational unit '%s' to '%s' failed. %s",
        organizationalUnit.format(rdn -> rdn),
        newName,
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  String getErrorCode() {
    return ErrorCode.EC_UPDATING_OU_FAILED;
  }

}
