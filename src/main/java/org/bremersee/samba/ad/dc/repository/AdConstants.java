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

package org.bremersee.samba.ad.dc.repository;

import static org.bremersee.ldaptive.LdaptiveAttribute.define;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.transcoder.UserAccountControl;
import org.bremersee.ldaptive.transcoder.ValueTranscoderFactory;
import org.bremersee.samba.ad.dc.model.Sid;
import org.ldaptive.ad.SecurityIdentifier;
import org.ldaptive.dn.Dn;
import org.ldaptive.transcode.AbstractBinaryValueTranscoder;

/**
 * The ad constants.
 *
 * @author Christian Bremer
 */
public abstract class AdConstants {

  private AdConstants() {
    super();
  }

  /**
   * The constant OBJECT_CLASS.
   */
  public static final LdaptiveAttribute<String> OBJECT_CLASS = define("objectClass");

  /**
   * The constant OBJECT_CLASS_COMPUTER.
   */
  public static final String OBJECT_CLASS_COMPUTER = "computer";

  /**
   * The constant OBJECT_CLASS_GROUP.
   */
  public static final String OBJECT_CLASS_GROUP = "group";

  /**
   * The constant OBJECT_CLASS_CONTAINER.
   */
  public static final String OBJECT_CLASS_CONTAINER = "container";

  /**
   * The constant OBJECT_CLASS_OU.
   */
  public static final String OBJECT_CLASS_OU = "organizationalUnit";

  /**
   * The constant OBJECT_CLASS_USER.
   */
  public static final String OBJECT_CLASS_USER = "user";

  /**
   * The constant RDN_ATTR_NAME_OU.
   */
  public static final String RDN_ATTR_NAME_OU = "OU";

  /**
   * The constant BASE_DN_COMPUTERS.
   */
  public static final String BASE_DN_COMPUTERS = "CN=Computers";

  /**
   * The constant BASE_DN_USERS.
   */
  public static final String BASE_DN_USERS = "CN=Users";

  /**
   * The constant YELLOW_PAGES.
   */
  public static final String YELLOW_PAGES = "CN=ypservers,CN=ypServ30,CN=RpcServices,CN=System";

  /**
   * The constant DN.
   */
  public static final LdaptiveAttribute<Dn> DN = define(
      "distinguishedName",
      false,
      ValueTranscoderFactory.getDnValueTranscoderCaseSensitive());

  /**
   * The constant WHEN_CREATED.
   */
  public static final LdaptiveAttribute<OffsetDateTime> WHEN_CREATED = define(
      "whenCreated",
      false,
      ValueTranscoderFactory.getGeneralizedTimeToOffsetDateTimeValueTranscoder());

  /**
   * The constant WHEN_CHANGED.
   */
  public static final LdaptiveAttribute<OffsetDateTime> WHEN_CHANGED = define(
      "whenChanged",
      false,
      ValueTranscoderFactory.getGeneralizedTimeToOffsetDateTimeValueTranscoder());

  /**
   * The constant CN.
   */
  public static final LdaptiveAttribute<String> CN = define("CN");

  /**
   * The constant DESCRIPTION.
   */
  public static final LdaptiveAttribute<String> DESCRIPTION = define("description");

  /**
   * The constant GID_NUMBER.
   */
  public static final LdaptiveAttribute<Integer> GID_NUMBER = define(
      "gidNumber",
      false,
      ValueTranscoderFactory.getIntegerValueTranscoder());

  /**
   * The constant MAIL.
   */
  public static final LdaptiveAttribute<String> MAIL = define("mail");

  /**
   * The constant MEMBER_OF_GROUP.
   */
  public static final LdaptiveAttribute<Dn> MEMBER_OF_GROUP = define(
      "memberOf",
      false,
      ValueTranscoderFactory.getDnValueTranscoderCaseSensitive());

  /**
   * The constant NAME.
   */
  public static final LdaptiveAttribute<String> NAME = define("name");

  /**
   * The constant NIS_DOMAIN (rfc2307).
   */
  public static final LdaptiveAttribute<String> NIS_DOMAIN = define("msSFU30NisDomain");

  /**
   * The constant NIS_NAME. Attribute of the username / group name (rfc2307).
   */
  public static final LdaptiveAttribute<String> NIS_NAME = define("msSFU30Name");

  /**
   * The constant OBJECT_SID.
   */
  public static final LdaptiveAttribute<Sid> OBJECT_SID = define(
      "objectSid", true, new SidValueTranscoder());

  /**
   * The constant PRIMARY_GROUP_ID.
   */
  public static final LdaptiveAttribute<Integer> PRIMARY_GROUP_ID = define(
      "primaryGroupID",
      false,
      ValueTranscoderFactory.getIntegerValueTranscoder());

  /**
   * The constant SAM_ACCOUNT_NAME.
   */
  public static final LdaptiveAttribute<String> SAM_ACCOUNT_NAME = define("sAMAccountName");

  /**
   * The constant IS_CRITICAL_SYSTEM_OBJECT.
   */
  public static final LdaptiveAttribute<Boolean> IS_CRITICAL_SYSTEM_OBJECT = define(
      "isCriticalSystemObject", false, ValueTranscoderFactory.getBooleanValueTranscoder());

  /**
   * The constant COMPUTER_DNS_HOST_NAME.
   */
  public static final LdaptiveAttribute<String> COMPUTER_DNS_HOST_NAME = define("dNSHostName");

  /**
   * The constant COMPUTER_NETWORK_ADDRESS.
   */
  public static final LdaptiveAttribute<String> COMPUTER_NETWORK_ADDRESS = define("networkAddress");

  /**
   * The constant COMPUTER_OPERATING_SYSTEM.
   */
  public static final LdaptiveAttribute<String> COMPUTER_OPERATING_SYSTEM = define(
      "operatingSystem");

  /**
   * The constant COMPUTER_OPERATING_SYSTEM_VERSION.
   */
  public static final LdaptiveAttribute<String> COMPUTER_OPERATING_SYSTEM_VERSION = define(
      "operatingSystemVersion");

  /**
   * The constant COMPUTER_SERVICE_PRINCIPAL_NAME.
   */
  public static final LdaptiveAttribute<String> COMPUTER_SERVICE_PRINCIPAL_NAME = define(
      "servicePrincipalName");

  /**
   * The constant GROUP_TYPE.
   */
  public static final LdaptiveAttribute<Integer> GROUP_TYPE = define(
      "groupType",
      false,
      ValueTranscoderFactory.getIntegerValueTranscoder());

  /**
   * The constant GROUP_MEMBER.
   */
  public static final LdaptiveAttribute<Dn> GROUP_MEMBER = define(
      "member",
      false,
      ValueTranscoderFactory.getDnValueTranscoderCaseSensitive());

  /**
   * The constant USER_ACCOUNT_EXPIRES.
   */
  public static final LdaptiveAttribute<OffsetDateTime> USER_ACCOUNT_EXPIRES = define(
      "accountExpires",
      false,
      ValueTranscoderFactory.getFileTimeToOffsetDateTimeValueTranscoder()
  );

  /**
   * The constant USER_COMPANY.
   */
  public static final LdaptiveAttribute<String> USER_COMPANY = define("company");

  /**
   * The constant USER_DEPARTMENT.
   */
  public static final LdaptiveAttribute<String> USER_DEPARTMENT = define("department");

  /**
   * The constant USER_DISPLAY_NAME.
   */
  public static final LdaptiveAttribute<String> USER_DISPLAY_NAME = define("displayName");

  /**
   * The constant USER_GECOS.
   */
  public static final LdaptiveAttribute<String> USER_GECOS = define("gecos");

  /**
   * The constant USER_GIVEN_NAME.
   */
  public static final LdaptiveAttribute<String> USER_GIVEN_NAME = define("givenName");

  /**
   * The constant USER_HOME_DIRECTORY.
   */
  public static final LdaptiveAttribute<String> USER_HOME_DIRECTORY = define("homeDirectory");

  /**
   * The constant USER_HOME_DRIVE.
   */
  public static final LdaptiveAttribute<String> USER_HOME_DRIVE = define("homeDrive");

  /**
   * The constant USER_INITIALS.
   */
  public static final LdaptiveAttribute<String> USER_INITIALS = define("initials");

  /**
   * The constant USER_LAST_LOGON.
   */
  public static final LdaptiveAttribute<OffsetDateTime> USER_LAST_LOGON = define(
      "lastLogon",
      false,
      ValueTranscoderFactory.getFileTimeToOffsetDateTimeValueTranscoder());

  /**
   * The constant USER_LOGIN_SHELL.
   */
  public static final LdaptiveAttribute<String> USER_LOGIN_SHELL = define("loginShell");

  /**
   * The constant USER_LOGON_COUNT.
   */
  public static final LdaptiveAttribute<Integer> USER_LOGON_COUNT = define(
      "logonCount", false, ValueTranscoderFactory.getIntegerValueTranscoder());

  /**
   * The constant USER_MOBILE.
   */
  public static final LdaptiveAttribute<String> USER_MOBILE = define("mobile");

  /**
   * The constant USER_OFFICE_NAME.
   */
  public static final LdaptiveAttribute<String> USER_OFFICE_NAME = define(
      "physicalDeliveryOfficeName");

  /**
   * The constant USER_PREFERRED_LANGUAGE.
   */
  public static final LdaptiveAttribute<String> USER_PREFERRED_LANGUAGE = define(
      "preferredLanguage");

  /**
   * The constant USER_PROFILE_PATH.
   */
  public static final LdaptiveAttribute<String> USER_PROFILE_PATH = define("profilePath");

  /**
   * The constant USER_PWD_LAST_SET.
   */
  public static final LdaptiveAttribute<OffsetDateTime> USER_PWD_LAST_SET = define(
      "pwdLastSet", false, ValueTranscoderFactory.getFileTimeToOffsetDateTimeValueTranscoder());

  /**
   * The constant USER_SCRIPT_PATH.
   */
  public static final LdaptiveAttribute<String> USER_SCRIPT_PATH = define("scriptPath");

  /**
   * The constant USER_SN.
   */
  public static final LdaptiveAttribute<String> USER_SN = define("sn");

  /**
   * The constant USER_TELEPHONE_NUMBER.
   */
  public static final LdaptiveAttribute<String> USER_TELEPHONE_NUMBER = define("telephoneNumber");

  /**
   * The constant USER_TITLE.
   */
  public static final LdaptiveAttribute<String> USER_TITLE = define("title");

  /**
   * The constant USER_UID.
   */
  public static final LdaptiveAttribute<String> USER_UID = define("uid");

  /**
   * The constant USER_UID_NUMBER.
   */
  public static final LdaptiveAttribute<Integer> USER_UID_NUMBER = define(
      "uidNumber",
      false,
      ValueTranscoderFactory.getIntegerValueTranscoder());

  /**
   * The constant USER_UNIX_HOME_DIRECTORY.
   */
  public static final LdaptiveAttribute<String> USER_UNIX_HOME_DIRECTORY = define(
      "unixHomeDirectory");

  /**
   * The constant USER_PRINCIPAL_NAME.
   */
  public static final LdaptiveAttribute<String> USER_PRINCIPAL_NAME = define(
      "userPrincipalName");

  /**
   * The constant USER_USER_ACCOUNT_CONTROL.
   */
  public static final LdaptiveAttribute<UserAccountControl> USER_USER_ACCOUNT_CONTROL = define(
      "userAccountControl",
      false,
      ValueTranscoderFactory.getUserAccountControlValueTranscoder());

  /**
   * The constant USER_UNICODE_PWD.
   */
  public static final LdaptiveAttribute<String> USER_UNICODE_PWD = define(
      "unicodePwd",
      true,
      ValueTranscoderFactory.getUnicodePwdValueTranscoder());

  /**
   * The constant USER_JPEG_PHOTO.
   */
  public static final LdaptiveAttribute<byte[]> USER_JPEG_PHOTO = define(
      "jpegPhoto",
      true,
      ValueTranscoderFactory.getByteArrayValueTranscoder());

  /**
   * The sid value transcoder.
   *
   * @author Christian Bremer
   */
  static class SidValueTranscoder extends AbstractBinaryValueTranscoder<Sid> {

    @Override
    public Sid decodeBinaryValue(byte[] value) {
      return Optional.ofNullable(value)
          .map(SecurityIdentifier::toString)
          .map(objectSid -> Sid.builder()
              .value(objectSid)
              .build())
          .orElse(null);
    }

    @Override
    public byte[] encodeBinaryValue(Sid value) {
      return Optional.ofNullable(value)
          .map(Sid::getValue)
          .map(SecurityIdentifier::toBytes)
          .orElse(null);
    }

    @Override
    public Class<Sid> getType() {
      return Sid.class;
    }
  }

}
