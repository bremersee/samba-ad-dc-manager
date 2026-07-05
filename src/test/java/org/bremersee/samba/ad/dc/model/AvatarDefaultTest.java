/*
 * Copyright 2019-2026 the original author or authors.
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

package org.bremersee.samba.ad.dc.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The avatar default test.
 */
@ExtendWith(SoftAssertionsExtension.class)
class AvatarDefaultTest {

  /**
   * From value.
   *
   * @param softly the softly
   */
  @Test
  void fromValue(SoftAssertions softly) {
    for (AvatarDefault avatar : AvatarDefault.values()) {
      String source = avatar.toString();
      AvatarDefault actual = AvatarDefault.fromValue(source);
      softly
          .assertThat(actual)
          .isEqualTo(avatar);
    }
  }

  /**
   * From value with default.
   */
  @Test
  void fromValueWithDefault() {
    AvatarDefault actual = AvatarDefault.fromValue(" ", AvatarDefault.MP);
    assertThat(actual)
        .isEqualTo(AvatarDefault.MP);
  }
}