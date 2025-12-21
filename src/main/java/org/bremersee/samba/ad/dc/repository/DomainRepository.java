/*
 * Copyright 2019-2020 the original author or authors.
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

package org.bremersee.samba.ad.dc.repository;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.springframework.validation.annotation.Validated;

/**
 * The domain repository interface.
 *
 * @author Christian Bremer
 */
@Validated
public interface DomainRepository {

  /**
   * Gets host name.
   *
   * @return the host name
   */
  String getHostName();

  /**
   * Gets domain sid.
   *
   * @return the domain sid
   */
  String getDomainSid();

  /**
   * Specifies whether NIS extensions (rfc2307) are installed on the AD Domain Controller. See <a
   * href="https://wiki.samba.org/index.php/Setting_up_RFC2307_in_AD">Setting up RFC2307 in AD</a>
   *
   * @return the boolean
   */
  boolean isRfc2307Enabled();

  /**
   * Gets domain info.
   *
   * @param ipOrHostname the ip or hostname
   * @return the domain info
   */
  @NotNull
  DomainInfo getDomainInfo(@NotEmpty String ipOrHostname);

  /**
   * Gets password information.
   *
   * @return the password information
   */
  PasswordInformation getPasswordInformation();

  /**
   * Create random password.
   *
   * @return the random password
   */
  String createRandomPassword();

  /**
   * Gets character rules.
   *
   * @param lowerNum the lower num
   * @param upperNum the upper num
   * @param digitNum the digit num
   * @param specialNum the special num
   * @return the character rules
   */
  default List<CharacterRule> getCharacterRules(
      int lowerNum, int upperNum, int digitNum, int specialNum) {
    return List.of(
        new CharacterRule(new SpecialCharacterData(), specialNum > 0 ? specialNum : 1),
        new CharacterRule(new DigitCharacterData(), digitNum > 0 ? digitNum : 1),
        new CharacterRule(new UpperCharacterData(), upperNum > 0 ? upperNum : 1),
        new CharacterRule(new LowerCharacterData(), lowerNum > 0 ? lowerNum : 1)
    );
  }

  /**
   * The type Special character data.
   */
  class SpecialCharacterData implements CharacterData {

    @Override
    public String getErrorCode() {
      return "INSUFFICIENT_SPECIAL";
    }

    @Override
    public String getCharacters() {
      return "!#$%&*+-.:<=>?@_";
    }
  }

  /**
   * The type Digit character data.
   */
  class DigitCharacterData implements CharacterData {

    @Override
    public String getErrorCode() {
      return "INSUFFICIENT_DIGIT";
    }

    @Override
    public String getCharacters() {
      return "123456789";
    }
  }

  /**
   * The type Upper character data.
   */
  class UpperCharacterData implements CharacterData {

    @Override
    public String getErrorCode() {
      return "INSUFFICIENT_UPPER";
    }

    @Override
    public String getCharacters() {
      return "ABCDEFGHJKLMNPQRSTUVWXYZ";
    }
  }

  /**
   * The type Lower character data.
   */
  class LowerCharacterData implements CharacterData {

    @Override
    public String getErrorCode() {
      return "INSUFFICIENT_LOWER";
    }

    @Override
    public String getCharacters() {
      return "abcdefghijkmnpqrstuvwxyz";
    }
  }

}
