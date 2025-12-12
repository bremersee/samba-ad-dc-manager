package org.bremersee.samba.ad.dc.ou.repository.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.common.repository.cli.CommandExecutorResponse;
import org.bremersee.samba.ad.dc.common.repository.cli.validator.SambaToolValidator;
import org.ldaptive.dn.Dn;
import org.springframework.util.Assert;

public class OuMoveValidator extends SambaToolValidator {

  private final Dn organizationalUnit;

  private final Dn newParentOu;

  public OuMoveValidator(Dn organizationalUnit, Dn newParentOu) {
    Assert.notNull(organizationalUnit, "Organizational unit must not be null.");
    Assert.notNull(newParentOu, "New parent organizational unit must not be null.");
    this.organizationalUnit = organizationalUnit;
    this.newParentOu = newParentOu;
  }

  @Override
  protected String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Renaming organizational unit '%s' to '%s' failed. %s",
        organizationalUnit.format(rdn -> rdn),
        newParentOu.format(rdn -> rdn),
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  protected String getErrorCode() {
    return ErrorCode.EC_UPDATING_OU_FAILED;
  }

}
