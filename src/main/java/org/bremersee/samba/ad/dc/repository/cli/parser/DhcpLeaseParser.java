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

package org.bremersee.samba.ad.dc.repository.cli.parser;

import java.util.List;
import java.util.function.BinaryOperator;
import org.bremersee.samba.ad.dc.model.DhcpLease;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponseParser;

/**
 * The dhcp lease list parser parses the response of the Linux command line tool
 * {@code dhcp-lease-list}.
 *
 * <p>A response of {@code dhcp-lease-list} looks like this:
 * <pre>
 * MAC b8:xx:xx:xx:xx:xx IP 192.168.1.109 HOSTNAME ukelei BEGIN 2019-08-18 11:20:33 END 2019-08-18 11:50:33 MANUFACTURER Apple, Inc.
 * MAC ac:xx:xx:xx:xx:yy IP 192.168.1.188 HOSTNAME -NA- BEGIN 2019-08-18 11:25:48 END 2019-08-18 11:55:48 MANUFACTURER
 * </pre>
 *
 * @author Christian Bremer
 */
public interface DhcpLeaseParser extends CommandExecutorResponseParser<List<DhcpLease>> {

  /**
   * The constant MAC.
   */
  String MAC = "MAC ";

  /**
   * The constant IP.
   */
  String IP = " IP ";

  /**
   * The constant HOSTNAME.
   */
  String HOSTNAME = " HOSTNAME ";

  /**
   * The constant HOSTNAME_UNKNOWN.
   */
  String HOSTNAME_UNKNOWN = "-NA-";

  /**
   * The constant BEGIN.
   */
  String BEGIN = " BEGIN ";

  /**
   * The constant END.
   */
  String END = " END ";

  /**
   * The constant MANUFACTURER.
   */
  String MANUFACTURER = " MANUFACTURER";

  /**
   * Default parser dhcp leases parser.
   *
   * @return the dhcp leases parser
   */
  static DhcpLeaseParser defaultParser() {
    return new DefaultDhcpLeaseParser();
  }

  /**
   * Default parser dhcp leases parser.
   *
   * <p>The unknown host converter converts the unknown host name
   * {@link DhcpLeaseParser#HOSTNAME_UNKNOWN} into another host name. The parameters of the function
   * are MAC and IP.
   *
   * @param unknownHostConverter the unknown host converter; first parameter is mac, second is
   *     ip
   * @return the dhcp leases parser
   */
  static DhcpLeaseParser defaultParser(BinaryOperator<String> unknownHostConverter) {
    return new DefaultDhcpLeaseParser(unknownHostConverter);
  }

}
