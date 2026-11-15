/*
 * Copyright 2025-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.samba.ad.dc.repository.cli.validator;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponseValidator;

/**
 * The dns entry validator.
 *
 * @author Christian Bremer
 */
abstract class DnsEntryValidator implements CommandExecutorResponseValidator {

  /**
   * Gets expected response.
   *
   * @return the expected response
   */
  abstract String getExpectedResponse();

  /**
   * Gets action.
   *
   * @return the action
   */
  abstract String getAction();

  /**
   * Gets error code.
   *
   * @return the error code
   */
  abstract String getErrorCode();

  @Override
  public void validate(CommandExecutorResponse response) {
    if (isNull(response.getStdout())
        || !response.getStdout().toLowerCase().contains(getExpectedResponse().toLowerCase())) {

      String error;
      if (nonNull(response.getStderr())) {
        int index = response.getStderr().indexOf('\n');
        if (index > 0) {
          error = ": " + response.getStderr().substring(0, index);
        } else {
          error = ": " + response.getStderr();
        }
      } else {
        error = ".";
      }
      throw ServiceException.internalServerError(
          String.format("%s dns entry failed%s", getAction(), error),
          getErrorCode());
    }
  }
}
