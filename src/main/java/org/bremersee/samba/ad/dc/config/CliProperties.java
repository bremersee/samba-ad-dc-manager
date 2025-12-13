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

package org.bremersee.samba.ad.dc.config;

import java.io.Serial;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type CliProperties.
 *
 * @author Christian Bremer
 */
@Getter
@Setter
@ToString(exclude = {"sambaToolCredentialsOptions"})
@EqualsAndHashCode
@NoArgsConstructor
public class CliProperties implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String execDir = System.getProperty("java.io.tmpdir");

  private String hostnameBinary = "/usr/bin/hostname";

  private String hostnameOptions; // maybe '--fqdn'

  private String sambaToolBinary = "/usr/bin/samba-tool";

  private String sambaToolCredentialsOptions;

  private String dhcpLeaseListBinary = "/usr/sbin/dhcp-lease-list";

  private SudoProperties sudo = new SudoProperties();

  private SshProperties ssh = new SshProperties();

  @Getter
  @Setter
  @ToString
  @EqualsAndHashCode
  @NoArgsConstructor
  public static class SudoProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean usingSudo = true;

    private String sudoCommand = "/usr/bin/sudo";
  }

  @Getter
  @Setter
  @ToString
  @EqualsAndHashCode
  @NoArgsConstructor
  public static class SshProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean usingSsh = false;

    private String sshCommand = "/usr/bin/ssh root@dc1";
  }

}
