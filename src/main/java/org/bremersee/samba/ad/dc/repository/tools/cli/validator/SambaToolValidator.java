package org.bremersee.samba.ad.dc.repository.tools.cli.validator;

import java.util.Optional;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.repository.tools.cli.CommandExecutorResponse;
import org.bremersee.samba.ad.dc.repository.tools.cli.CommandExecutorResponseValidator;

abstract class SambaToolValidator implements CommandExecutorResponseValidator {

  @Override
  public void validate(CommandExecutorResponse response) {
    if (hasError(response)) {
      throw ServiceException.internalServerError(
          getExceptionReason(response),
          getErrorCode());
    }
  }

  boolean hasError(CommandExecutorResponse response) {
    String stdout = Optional.ofNullable(response.getStdout())
        .map(String::trim)
        .map(String::toLowerCase)
        .orElse("");
    String stderr = Optional.ofNullable(response.getStderr())
        .map(String::trim)
        .map(String::toLowerCase)
        .orElse("");
    return stdout.startsWith("error") || stderr.startsWith("error");
  }

  abstract String getExceptionReason(CommandExecutorResponse response);

  abstract String getErrorCode();
}
