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

import java.util.stream.Stream;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.repository.cli.CommandExecutorResponseParser;

/**
 * The dns entries parser parses the response of the Linux command line tool
 * {@code samba-tool dns query <server> <zone> <name> <A|AAAA|PTR|CNAME|MX|NS|SOA|SRV|TXT|ALL>
 * [options]}***, for example {@code samba-tool dns query dc1 samdom.example.org @ ALL}.
 *
 * <p>A response of this command looks like this:
 * <pre>
 *   Name=, Records=3, Children=0
 *     SOA: serial=449810, refresh=900, retry=600, expire=86400, minttl=3600, ns=dc1.samdom.example.org., email=hostmaster.samdom.example.org. (flags=600000f0, serial=449809, ttl=3600)
 *     NS: dc1.samdom.example.org. (flags=600000f0, serial=449809, ttl=900)
 *     A: 192.168.1.3 (flags=600000f0, serial=449809, ttl=900)
 *   Name=_sites, Records=0, Children=1
 *   Name=_tcp, Records=0, Children=4
 *   Name=_udp, Records=0, Children=2
 *   Name=data, Records=1, Children=0
 *     A: 192.168.1.4 (flags=f0, serial=428109, ttl=3600)
 *   Name=ha, Records=1, Children=0
 *     A: 192.168.1.5 (flags=f0, serial=428110, ttl=3600)
 *   Name=proxy, Records=1, Children=0
 *     CNAME: ha.samdom.example.org. (flags=f0, serial=446881, ttl=900)
 * </pre>
 *
 * @author Christian Bremer
 */
public interface DnsEntriesParser
    extends CommandExecutorResponseParser<Stream<DnsEntry>> {

  /**
   * Returns a new default dns entries parser.
   *
   * @param zoneName the zone name
   * @param name the name
   * @return the dns entries parser
   */
  static DnsEntriesParser defaultParser(String zoneName, String name) {
    return new DefaultDnsEntriesParser(zoneName, name);
  }

}
