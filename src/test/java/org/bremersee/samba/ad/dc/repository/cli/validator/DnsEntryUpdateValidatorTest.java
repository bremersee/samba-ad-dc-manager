/*
 * Copyright 2026 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The dns entry update validator test.
 *
 * @author Christian Bremer
 */
class DnsEntryUpdateValidatorTest {

  private static final DnsEntryUpdateValidator target = new DnsEntryUpdateValidator();

  /**
   * Gets expected response.
   */
  @Test
  void getExpectedResponse() {
    assertThat(target.getExpectedResponse())
        .isEqualTo("Record updated successfully");
  }

  /**
   * Gets action.
   */
  @Test
  void getAction() {
    assertThat(target.getAction())
        .isEqualTo("Updating");
  }

  /**
   * Gets error code.
   */
  @Test
  void getErrorCode() {
    assertThat(target.getErrorCode())
        .isEqualTo(ErrorCode.EC_UPDATING_DNS_ENTRY_FAILED);
  }

  /**
   * Validate.
   */
  @Test
  void validate() {
    String stdout = target.getExpectedResponse();
    CommandExecutorResponse response = new CommandExecutorResponse(stdout, null);
    assertThatNoException()
        .isThrownBy(() -> target.validate(response));
  }

  /**
   * Validate.
   *
   * @param value the value
   */
  @ParameterizedTest
  @ValueSource(strings = {"unexpected", ""})
  void validate(String value) {
    String stdout = value.isEmpty() ? null : value;
    CommandExecutorResponse response = new CommandExecutorResponse(stdout, "");
    assertThatExceptionOfType(ServiceException.class)
        .isThrownBy(() -> target.validate(response));
  }
}