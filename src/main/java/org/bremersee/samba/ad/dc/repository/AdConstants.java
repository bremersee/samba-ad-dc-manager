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

public abstract class AdConstants {

  private AdConstants() {
    super();
  }

  public static final LdaptiveAttribute<String> OBJECT_CLASS = define("objectClass");

  public static final String OBJECT_CLASS_COMPUTER = "computer";

  public static final String OBJECT_CLASS_GROUP = "group";

  public static final String OBJECT_CLASS_CONTAINER = "container";

  public static final String OBJECT_CLASS_OU = "organizationalUnit";

  public static final String OBJECT_CLASS_USER = "user";

  public static final String RDN_ATTR_NAME_OU = "OU";

  public static final String BASE_DN_COMPUTERS = "CN=Computers";

  public static final String BASE_DN_USERS = "CN=Users";

  public static final String YELLOW_PAGES = "CN=ypservers,CN=ypServ30,CN=RpcServices,CN=System";

  public static final LdaptiveAttribute<Dn> DN = define(
      "distinguishedName",
      false,
      ValueTranscoderFactory.getDnValueTranscoderCaseSensitive());

  public static final LdaptiveAttribute<OffsetDateTime> WHEN_CREATED = define(
      "whenCreated",
      false,
      ValueTranscoderFactory.getGeneralizedTimeToOffsetDateTimeValueTranscoder());

  public static final LdaptiveAttribute<OffsetDateTime> WHEN_CHANGED = define(
      "whenChanged",
      false,
      ValueTranscoderFactory.getGeneralizedTimeToOffsetDateTimeValueTranscoder());

  public static final LdaptiveAttribute<String> CN = define("CN");

  public static final LdaptiveAttribute<String> DESCRIPTION = define("description");

  public static final LdaptiveAttribute<Integer> GID_NUMBER = define(
      "gidNumber",
      false,
      ValueTranscoderFactory.getIntegerValueTranscoder());

  public static final LdaptiveAttribute<String> MAIL = define("mail");

  public static final LdaptiveAttribute<Dn> MEMBER_OF_GROUP = define(
      "memberOf",
      false,
      ValueTranscoderFactory.getDnValueTranscoderCaseSensitive());

  public static final LdaptiveAttribute<String> NAME = define("name");

  /**
   * The constant NIS_DOMAIN (rfc2307).
   */
  public static final LdaptiveAttribute<String> NIS_DOMAIN = define("msSFU30NisDomain");

  /**
   * The constant NIS_NAME. Attribute of the username / group name (rfc2307).
   */
  public static final LdaptiveAttribute<String> NIS_NAME = define("msSFU30Name");

  public static final LdaptiveAttribute<Sid> OBJECT_SID = define(
      "objectSid", true, new SidValueTranscoder());

  public static final LdaptiveAttribute<Integer> PRIMARY_GROUP_ID = define(
      "primaryGroupID",
      false,
      ValueTranscoderFactory.getIntegerValueTranscoder());

  public static final LdaptiveAttribute<String> SAM_ACCOUNT_NAME = define("sAMAccountName");

  public static final LdaptiveAttribute<Boolean> IS_CRITICAL_SYSTEM_OBJECT = define(
      "isCriticalSystemObject", false, ValueTranscoderFactory.getBooleanValueTranscoder());

  public static final LdaptiveAttribute<String> COMPUTER_DNS_HOST_NAME = define("dNSHostName");

  public static final LdaptiveAttribute<String> COMPUTER_NETWORK_ADDRESS = define("networkAddress");

  public static final LdaptiveAttribute<String> COMPUTER_OPERATING_SYSTEM = define(
      "operatingSystem");

  public static final LdaptiveAttribute<String> COMPUTER_OPERATING_SYSTEM_VERSION = define(
      "operatingSystemVersion");

  public static final LdaptiveAttribute<String> COMPUTER_SERVICE_PRINCIPAL_NAME = define(
      "servicePrincipalName");

  public static final LdaptiveAttribute<Integer> GROUP_TYPE = define(
      "groupType",
      false,
      ValueTranscoderFactory.getIntegerValueTranscoder());

  public static final LdaptiveAttribute<Dn> GROUP_MEMBER = define(
      "member",
      false,
      ValueTranscoderFactory.getDnValueTranscoderCaseSensitive());

  public static final LdaptiveAttribute<OffsetDateTime> USER_ACCOUNT_EXPIRES = define(
      "accountExpires",
      false,
      ValueTranscoderFactory.getFileTimeToOffsetDateTimeValueTranscoder()
  );

  public static final LdaptiveAttribute<String> USER_COMPANY = define("company");

  public static final LdaptiveAttribute<String> USER_DEPARTMENT = define("department");

  public static final LdaptiveAttribute<String> USER_DISPLAY_NAME = define("displayName");

  public static final LdaptiveAttribute<String> USER_GECOS = define("gecos");

  public static final LdaptiveAttribute<String> USER_GIVEN_NAME = define("givenName");

  public static final LdaptiveAttribute<String> USER_HOME_DIRECTORY = define("homeDirectory");

  public static final LdaptiveAttribute<String> USER_HOME_DRIVE = define("homeDrive");

  public static final LdaptiveAttribute<String> USER_INITIALS = define("initials");

  public static final LdaptiveAttribute<OffsetDateTime> USER_LAST_LOGON = define(
      "lastLogon",
      false,
      ValueTranscoderFactory.getFileTimeToOffsetDateTimeValueTranscoder());

  public static final LdaptiveAttribute<String> USER_LOGIN_SHELL = define("loginShell");

  public static final LdaptiveAttribute<Integer> USER_LOGON_COUNT = define(
      "logonCount", false, ValueTranscoderFactory.getIntegerValueTranscoder());

  public static final LdaptiveAttribute<String> USER_MOBILE = define("mobile");

  public static final LdaptiveAttribute<String> USER_OFFICE_NAME = define(
      "physicalDeliveryOfficeName");

  public static final LdaptiveAttribute<String> USER_PREFERRED_LANGUAGE = define(
      "preferredLanguage");

  public static final LdaptiveAttribute<String> USER_PROFILE_PATH = define("profilePath");

  public static final LdaptiveAttribute<OffsetDateTime> USER_PWD_LAST_SET = define(
      "pwdLastSet", false, ValueTranscoderFactory.getFileTimeToOffsetDateTimeValueTranscoder());

  public static final LdaptiveAttribute<String> USER_SCRIPT_PATH = define("scriptPath");

  public static final LdaptiveAttribute<String> USER_SN = define("sn");

  public static final LdaptiveAttribute<String> USER_TELEPHONE_NUMBER = define("telephoneNumber");

  public static final LdaptiveAttribute<String> USER_TITLE = define("title");

  public static final LdaptiveAttribute<String> USER_UID = define("uid");

  public static final LdaptiveAttribute<Integer> USER_UID_NUMBER = define(
      "uidNumber",
      false,
      ValueTranscoderFactory.getIntegerValueTranscoder());

  public static final LdaptiveAttribute<String> USER_UNIX_HOME_DIRECTORY = define(
      "unixHomeDirectory");

  public static final LdaptiveAttribute<String> USER_PRINCIPAL_NAME = define(
      "userPrincipalName");

  public static final LdaptiveAttribute<UserAccountControl> USER_USER_ACCOUNT_CONTROL = define(
      "userAccountControl",
      false,
      ValueTranscoderFactory.getUserAccountControlValueTranscoder());

  public static final LdaptiveAttribute<String> USER_UNICODE_PWD = define(
      "unicodePwd",
      true,
      ValueTranscoderFactory.getUnicodePwdValueTranscoder());

  public static final LdaptiveAttribute<byte[]> USER_JPEG_PHOTO = define(
      "jpegPhoto",
      true,
      ValueTranscoderFactory.getByteArrayValueTranscoder());

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
