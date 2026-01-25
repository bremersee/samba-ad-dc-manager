/*
 * Copyright 2025 the original author or authors.
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

package org.bremersee.samba.ad.dc.controller.ui.model;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.ldaptive.dn.Dn;

/**
 * The abstract user model.
 *
 * @author Christian Bremer
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
abstract class UserModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The new organizational unit.
   */
  private String newOu;

  /**
   * The sam account name.
   */
  private String samAccountName;

  /**
   * The user principal name.
   */
  private String userPrincipalName;

  /**
   * The enabled flag.
   */
  private boolean enabled = true;

  /**
   * The password expiration enabled flag.
   */
  private boolean passwordExpirationEnabled = false;

  /**
   * The no-expiry flag.
   */
  private boolean noExpiry = true;

  /**
   * The date-time of account expiration.
   */
  private OffsetDateTime accountExpires;

  /**
   * User's first name.
   */
  private String firstName;

  /**
   * User's last name.
   */
  private String lastName;

  /**
   * User's display name.
   */
  private String displayName;

  /**
   * User's initials.
   */
  private String initials;

  /**
   * User's preferred language. ISO 639-1 language codes. The combinations like de-DE and en-US with
   * ISO-639 and ISO-3166 also work.
   */
  private String preferredLanguage;

  /**
   * User's email address.
   */
  private String email;

  /**
   * User's mobile phone number.
   */
  private String mobile;

  /**
   * User's telephone number.
   */
  private String telephoneNumber;

  /**
   * A description of the user.
   */
  private String description;

  /**
   * User's home directory path.
   */
  private String homeDirectory;

  /**
   * User's home drive letter.
   */
  private String homeDrive;

  /**
   * User's profile path.
   */
  private String profilePath;

  /**
   * User's logon script path.
   */
  private String scriptPath;

  /**
   * User's company.
   */
  private String company;

  /**
   * User's job title.
   */
  private String title;

  /**
   * User's department.
   */
  private String department;

  /**
   * User's office location.
   */
  private String physicalDeliveryOfficeName;

  /**
   * User's Unix/RFC2307 username.
   */
  private String uid;

  /**
   * User's Unix/RFC2307 numeric UID.
   */
  private Integer uidNumber;

  /**
   * User's Unix/RFC2307 primary GID number.
   */
  private Integer gidNumber;

  /**
   * User's Unix/RFC2307 login shell.
   */
  private String loginShell;

  /**
   * User's Unix/RFC2307 home directory.
   */
  private String unixHomeDirectory;

  /**
   * User's Unix/RFC2307 GECOS field.
   */
  private String gecos;

  /**
   * User's Unix/RFC2307 NIS domain.
   */
  private String nisDomain;

  /**
   * Instantiates a new user model.
   */
  UserModel() {
    super();
  }

  /**
   * Gets distinguished name of new parent organizational unit.
   *
   * @return the new ou dn
   */
  public Optional<Dn> getNewOuDn() {
    return Optional.ofNullable(newOu)
        .filter(DnTool::isValidDn)
        .map(Dn::new);
  }

  /**
   * Gets account expires.
   *
   * @return the account expires
   */
  public OffsetDateTime getAccountExpires() {
    if (noExpiry) {
      return null;
    }
    return accountExpires;
  }

  /**
   * Gets account expires iso formatted.
   *
   * @return the account expires iso
   */
  public String getAccountExpiresIso() {
    OffsetDateTime dateTime;
    if (isEmpty(accountExpires)) {
      dateTime = OffsetDateTime.now(ZoneOffset.UTC);
    } else {
      dateTime = accountExpires.withOffsetSameInstant(ZoneOffset.UTC);
    }
    return dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
  }

  /**
   * Sets account expires iso formatted.
   *
   * @param accountExpiresIso the account expires iso
   */
  public void setAccountExpiresIso(String accountExpiresIso) {
    if (isEmpty(accountExpiresIso)) {
      this.accountExpires = null;
      return;
    }
    this.accountExpires = OffsetDateTime.parse(accountExpiresIso, DateTimeFormatter.ISO_DATE_TIME);
  }

}
