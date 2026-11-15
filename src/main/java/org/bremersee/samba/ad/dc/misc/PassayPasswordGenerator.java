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

package org.bremersee.samba.ad.dc.misc;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.passay.data.CharacterData;
import org.passay.rule.CharacterRule;
import org.springframework.stereotype.Service;

/**
 * The passay password generator.
 *
 * @author Christian Bremer
 */
@Service
public class PassayPasswordGenerator implements PasswordGenerator {

  private final Random random;

  /**
   * Instantiates a new passay password generator.
   */
  public PassayPasswordGenerator() {
    this.random = new SecureRandom();
  }

  @Override
  public String generatePassword(PasswordInformation passwordInformation) {
    int minLength = passwordInformation.getMinimumPasswordLength();
    int maxLength = passwordInformation.getMaximumPasswordLength();
    int maxPlus = Math.min(maxLength - minLength, 9);
    int length = Optional.of(minLength + (maxPlus > 0 ? random.nextInt(maxPlus) : 0))
        .filter(len -> len >= 4)
        .orElse(4);
    int lower = Math.max((int) Math.floor(length * 0.3), 1);
    int upper = Math.max((int) Math.floor(length * 0.3), 1);
    int digit = Math.max((int) Math.floor(length * 0.2), 1);
    int special = Math.max((int) Math.floor(length * 0.1), 1);
    List<CharacterRule> rules = getCharacterRules(lower, upper, digit, special);
    return new org.passay.generate.PasswordGenerator(random, length, 3, rules)
        .generate()
        .toString();
  }

  /**
   * Gets character rules.
   *
   * @param lowerNum the lower num
   * @param upperNum the upper num
   * @param digitNum the digit num
   * @param specialNum the special num
   * @return the character rules
   */
  private List<CharacterRule> getCharacterRules(
      int lowerNum, int upperNum, int digitNum, int specialNum) {
    return List.of(
        new CharacterRule(new SpecialCharacterData(), specialNum > 0 ? specialNum : 1),
        new CharacterRule(new DigitCharacterData(), digitNum > 0 ? digitNum : 1),
        new CharacterRule(new UpperCharacterData(), upperNum > 0 ? upperNum : 1),
        new CharacterRule(new LowerCharacterData(), lowerNum > 0 ? lowerNum : 1)
    );
  }

  /**
   * The special character data.
   *
   * @author Christian Bremer
   */
  private static class SpecialCharacterData implements CharacterData {

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
   * The digit character data.
   *
   * @author Christian Bremer
   */
  private static class DigitCharacterData implements CharacterData {

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
   * The pper character data.
   *
   * @author Christian Bremer
   */
  private static class UpperCharacterData implements CharacterData {

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
   * The lower character data.
   *
   * @author Christian Bremer
   */
  private static class LowerCharacterData implements CharacterData {

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
