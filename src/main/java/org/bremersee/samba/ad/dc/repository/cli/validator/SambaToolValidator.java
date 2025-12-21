package org.bremersee.samba.ad.dc.repository.cli.validator;

import java.util.Optional;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponseValidator;

public abstract class SambaToolValidator implements CommandExecutorResponseValidator {

  @Override
  public void validate(CommandExecutorResponse response) {
    if (hasError(response)) {
      throw ServiceException.internalServerError(
          getExceptionReason(response),
          getErrorCode());
    }
  }

  protected boolean hasError(CommandExecutorResponse response) {
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

  protected abstract String getExceptionReason(CommandExecutorResponse response);

  protected abstract String getErrorCode();
}
