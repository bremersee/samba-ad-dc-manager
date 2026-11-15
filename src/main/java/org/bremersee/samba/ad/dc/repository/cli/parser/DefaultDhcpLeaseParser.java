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

import static java.util.Objects.requireNonNullElseGet;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.bremersee.samba.ad.dc.model.DhcpLease;
import org.bremersee.samba.ad.dc.repository.cli.AbstractCommandExecutorResponseParser;
import org.springframework.util.StringUtils;

/**
 * The default dhcp lease parser.
 *
 * @author Christian Bremer
 */
class DefaultDhcpLeaseParser extends AbstractCommandExecutorResponseParser<List<DhcpLease>>
    implements DhcpLeaseParser {

  private final BinaryOperator<String> unknownHostConverter;

  /**
   * Instantiates a new default parser.
   */
  DefaultDhcpLeaseParser() {
    this(null);
  }

  /**
   * Instantiates a new default parser.
   *
   * <p>The unknown host converter converts the unknown host name
   * {@link DhcpLeaseParser#HOSTNAME_UNKNOWN} into another host name. The parameters of the function
   * are MAC and IP.
   *
   * @param unknownHostConverter the unknown host converter
   */
  DefaultDhcpLeaseParser(BinaryOperator<String> unknownHostConverter) {
    this.unknownHostConverter = requireNonNullElseGet(unknownHostConverter,
        DefaultUnknownHostConverter::new);
  }

  @Override
  protected List<DhcpLease> getDefaultValue() {
    return List.of();
  }

  @Override
  protected List<DhcpLease> doParse(BufferedReader reader) throws IOException {
    List<DhcpLease> leases = new ArrayList<>();
    String line;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      String mac = findDhcpLeasePart(line, MAC, IP);
      String ip = findDhcpLeasePart(line, IP, HOSTNAME);
      String hostname = findDhcpLeasePart(line, HOSTNAME, BEGIN);
      if (HOSTNAME_UNKNOWN.equalsIgnoreCase(hostname) && ip != null) {
        hostname = unknownHostConverter.apply(mac, ip);
      }
      String begin = findDhcpLeasePart(line, BEGIN, END);
      String end = findDhcpLeasePart(line, END, MANUFACTURER);
      String manufacturer = findDhcpLeasePart(line, MANUFACTURER, null);
      if (mac != null && ip != null && hostname != null && begin != null && end != null) {
        leases.add(DhcpLease.builder()
            .mac(mac.replace("-", ":").trim().toLowerCase())
            .ip(ip)
            .hostname(hostname)
            .begin(parseDhcpLeaseTime(begin))
            .end(parseDhcpLeaseTime(end))
            .manufacturer(manufacturer)
            .build());
      }
    }
    return leases;
  }

  private String findDhcpLeasePart(String line, String field, String nextField) {
    if (!StringUtils.hasText(line) || !StringUtils.hasText(field)) {
      return null;
    }
    int start = line.indexOf(field);
    if (start < 0) {
      return null;
    }
    start = start + field.length();
    int end = StringUtils.hasText(nextField)
        ? line.indexOf(nextField, start)
        : line.length();
    if (end < 0 || end <= start) {
      return null;
    }
    return line.substring(start, end).trim();
  }

  private OffsetDateTime parseDhcpLeaseTime(String time) {
    return Optional.ofNullable(time)
        .filter(Predicate.not(String::isBlank))
        .map(timeStr -> LocalDateTime
            .parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        // As of https://linux.die.net/man/5/dhcpd.leases the time zone is always UTC
        .map(localDateTime -> OffsetDateTime.of(localDateTime, ZoneOffset.UTC))
        .orElse(null);
  }

  /**
   * The default unknown host converter.
   *
   * @author Christian Bremer
   */
  static class DefaultUnknownHostConverter implements BinaryOperator<String> {

    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    /**
     * Instantiates a new default unknown host converter.
     */
    DefaultUnknownHostConverter() {
      super();
    }

    @Override
    public String apply(String mac, String ip) {
      if (IPV4_PATTERN.matcher(ip).matches()) {
        return "dhcp-" + ip.replace('.', '-');
      }
      return HOSTNAME_UNKNOWN;
    }
  }
}
