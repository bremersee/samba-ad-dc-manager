package org.bremersee.samba.ad.dc.ou.repository.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.bremersee.samba.ad.dc.repository.cli.validator.SambaToolValidator;
import org.ldaptive.dn.Dn;
import org.springframework.util.Assert;

public class OuAddValidator extends SambaToolValidator {

  private final Dn organizationalUnit;

  public OuAddValidator(Dn organizationalUnit) {
    Assert.notNull(organizationalUnit, "Organizational unit must not be null.");
    this.organizationalUnit = organizationalUnit;
  }

  @Override
  protected String getExceptionReason(CommandExecutorResponse response) {
    return String.format("Adding organizational unit '%s' failed. %s",
        organizationalUnit.format(rdn -> rdn),
        CommandExecutorResponse.toExceptionMessage(response));
  }

  @Override
  protected String getErrorCode() {
    return ErrorCode.EC_ADDING_OU_FAILED;
  }

}
