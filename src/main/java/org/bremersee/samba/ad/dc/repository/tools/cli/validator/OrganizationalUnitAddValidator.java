package org.bremersee.samba.ad.dc.repository.tools.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.repository.tools.cli.CommandExecutorResponse;
import org.ldaptive.dn.Dn;
import org.springframework.util.Assert;

public class OrganizationalUnitAddValidator extends SambaToolValidator {

  private final Dn organizationalUnit;

  public OrganizationalUnitAddValidator(Dn organizationalUnit) {
    Assert.notNull(organizationalUnit, "Organizational unit must not be null.");
    this.organizationalUnit = organizationalUnit;
  }

  @Override
  String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Adding organizational unit '%s' failed. %s",
        organizationalUnit.format(rdn -> rdn),
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  String getErrorCode() {
    return ErrorCode.EC_ADDING_OU_FAILED;
  }

}
