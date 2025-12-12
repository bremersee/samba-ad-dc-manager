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

package org.bremersee.samba.ad.dc.oldmodel;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.ErrorCode;

/**
 * The dns record type.
 *
 * @author Christian Bremer
 */
@Getter
public enum DnsEntryType {

  /**
   * All dns record types.
   */
  ALL(-1, false, false, true, v -> v),

  /**
   * A dns record type.
   */
  A(1, true, true, true, v -> v),

  /**
   * Ns dns record type.
   */
  NS(2, true, true, true, ToSambaToolValueTransformers.FQDN_TRANSFORMER),

  /**
   * Md dns record type.
   */
  MD(3, false, false, false, v -> v),

  /**
   * Mf dns record type.
   */
  MF(4, false, false, false, v -> v),

  /**
   * Cname dns record type.
   */
  CNAME(5, true, true, true, ToSambaToolValueTransformers.FQDN_TRANSFORMER),

  /**
   * Soa dns record type.
   */
  SOA(6, false, true, true, ToSambaToolValueTransformers.SOA_TRANSFORMER),

  /**
   * Mb dns record type.
   */
  MB(7, false, false, false, v -> v),

  /**
   * Mg dns record type.
   */
  MG(8, false, false, false, v -> v),

  /**
   * Mr dns record type.
   */
  MR(9, false, false, false, v -> v),

  /**
   * Null dns record type.
   */
  NULL(10, false, false, false, v -> v),

  /**
   * Wks dns record type.
   */
  WKS(11, false, false, false, v -> v),

  /**
   * Ptr dns record type.
   */
  PTR(12, true, true, true, ToSambaToolValueTransformers.FQDN_TRANSFORMER),

  /**
   * Hinfo dns record type.
   */
  HINFO(13, false, false, false, v -> v),

  /**
   * Minfo dns record type.
   */
  MINFO(14, false, false, false, v -> v),

  /**
   * Mx dns record type.
   */
  MX(15, true, true, true, ToSambaToolValueTransformers.MX_TRANSFORMER),

  /**
   * Txt dns record type.
   */
  TXT(16, true, true, true, ToSambaToolValueTransformers.TXT_TRANSFORMER),

  /**
   * Rp dns record type.
   */
  RP(17, false, false, false, v -> v),

  /**
   * Afsdb dns record type.
   */
  AFSDB(18, false, false, false, v -> v),

  /**
   * X 25 dns record type.
   */
  X25(19, false, false, false, v -> v),

  /**
   * Isdn dns record type.
   */
  ISDN(20, false, false, false, v -> v),

  /**
   * Rt dns record type.
   */
  RT(21, false, false, false, v -> v),

  /**
   * Nsap dns record type.
   */
  NSAP(22, false, false, false, v -> v),

  /**
   * Nsap ptr dns record type.
   */
  NSAP_PTR(23, false, false, false, v -> v),

  /**
   * Sig dns record type.
   */
  SIG(24, false, false, false, v -> v),

  /**
   * Key dns record type.
   */
  KEY(25, false, false, false, v -> v),

  /**
   * Px dns record type.
   */
  PX(26, false, false, false, v -> v),

  /**
   * Gpos dns record type.
   */
  GPOS(27, false, false, false, v -> v),

  /**
   * Aaaa dns record type.
   */
  AAAA(28, true, true, true, v -> v),

  /**
   * Loc dns record type.
   */
  LOC(29, false, false, false, v -> v),

  /**
   * Nxt dns record type.
   */
  NXT(30, false, false, false, v -> v),

  /**
   * Eid dns record type.
   */
  EID(31, false, false, false, v -> v),

  /**
   * Nimloc dns record type.
   */
  NIMLOC(32, false, false, false, v -> v),

  /**
   * Srv dns record type.
   */
  SRV(33, true, true, true, ToSambaToolValueTransformers.SRV_TRANSFORMER),

  /**
   * Atma dns record type.
   */
  ATMA(34, false, false, false, v -> v),

  /**
   * Naptr dns record type.
   */
  NAPTR(35, false, false, false, v -> v),

  /**
   * Kx dns record type.
   */
  KX(36, false, false, false, v -> v),

  /**
   * Cert dns record type.
   */
  CERT(37, false, false, false, v -> v),

  /**
   * A 6 dns record type.
   */
  A6(38, false, false, false, v -> v),

  /**
   * Dname dns record type.
   */
  DNAME(39, false, false, false, v -> v),

  /**
   * Sink dns record type.
   */
  SINK(40, false, false, false, v -> v),

  /**
   * Opt dns record type.
   */
  OPT(41, false, false, false, v -> v),

  /**
   * Apl dns record type.
   */
  APL(42, false, false, false, v -> v),

  /**
   * Ds dns record type.
   */
  DS(43, false, false, false, v -> v),

  /**
   * Sshfp dns record type.
   */
  SSHFP(44, false, false, false, v -> v),

  /**
   * Ipseckey dns record type.
   */
  IPSECKEY(45, false, false, false, v -> v),

  /**
   * Rrsig dns record type.
   */
  RRSIG(46, false, false, false, v -> v),

  /**
   * Nsec dns record type.
   */
  NSEC(47, false, false, false, v -> v),

  /**
   * Dnskey dns record type.
   */
  DNSKEY(48, false, false, false, v -> v),

  /**
   * Dhcid dns record type.
   */
  DHCID(49, false, false, false, v -> v),

  /**
   * Nsec 3 dns record type.
   */
  NSEC3(50, false, false, false, v -> v),

  /**
   * Nsec 3 param dns record type.
   */
  NSEC3PARAM(51, false, false, false, v -> v),

  /**
   * Tlsa dns record type.
   */
  TLSA(52, false, false, false, v -> v),

  /**
   * Hip dns record type.
   */
  HIP(55, false, false, false, v -> v),

  /**
   * Ninfo dns record type.
   */
  NINFO(56, false, false, false, v -> v),

  /**
   * Rkey dns record type.
   */
  RKEY(57, false, false, false, v -> v),

  /**
   * Talink dns record type.
   */
  TALINK(58, false, false, false, v -> v),

  /**
   * Cds dns record type.
   */
  CDS(59, false, false, false, v -> v),

  /**
   * Cdnskey dns record type.
   */
  CDNSKEY(60, false, false, false, v -> v),

  /**
   * Openpgpkey dns record type.
   */
  OPENPGPKEY(61, false, false, false, v -> v),

  /**
   * Csync dns record type.
   */
  CSYNC(62, false, false, false, v -> v),

  /**
   * Spf dns record type.
   */
  SPF(99, false, false, false, v -> v),

  /**
   * Uinfo dns record type.
   */
  UINFO(100, false, false, false, v -> v),

  /**
   * Uid dns record type.
   */
  UID(101, false, false, false, v -> v),

  /**
   * Gid dns record type.
   */
  GID(102, false, false, false, v -> v),

  /**
   * Unspec dns record type.
   */
  UNSPEC(103, false, false, false, v -> v),

  /**
   * Nid dns record type.
   */
  NID(104, false, false, false, v -> v),

  /**
   * L 32 dns record type.
   */
  L32(105, false, false, false, v -> v),

  /**
   * L 64 dns record type.
   */
  L64(106, false, false, false, v -> v),

  /**
   * Lp dns record type.
   */
  LP(107, false, false, false, v -> v),

  /**
   * Eui 48 dns record type.
   */
  EUI48(108, false, false, false, v -> v),

  /**
   * Eui 64 dns record type.
   */
  EUI64(109, false, false, false, v -> v),

  /**
   * Tkey dns record type.
   */
  TKEY(249, false, false, false, v -> v),

  /**
   * Tsig dns record type.
   */
  TSIG(250, false, false, false, v -> v),

  /**
   * Ixfr dns record type.
   */
  IXFR(251, false, false, false, v -> v),

  /**
   * Axfr dns record type.
   */
  AXFR(252, false, false, false, v -> v),

  /**
   * Mailb dns record type.
   */
  MAILB(253, false, false, false, v -> v),

  /**
   * Maila dns record type.
   */
  MAILA(254, false, false, false, v -> v),

  /**
   * Any dns record type.
   */
  ANY(255, false, false, false, v -> v),

  /**
   * Uri dns record type.
   */
  URI(256, false, false, false, v -> v),

  /**
   * Caa dns record type.
   */
  CAA(257, false, false, false, v -> v),

  /**
   * Ta dns record type.
   */
  TA(32768, false, false, false, v -> v),

  /**
   * Dlv dns record type.
   */
  DLV(32769, false, false, false, v -> v);

  /**
   * Internal lookup table to map values to types.
   */
  private static final Map<Integer, DnsEntryType> VALUE_TYPE_MAP = new HashMap<>();

  /**
   * Internal lookup table to map strings to types.
   */
  private static final Map<String, DnsEntryType> STRING_TYPE_MAP = new HashMap<>();

  static {
    for (DnsEntryType t : DnsEntryType.values()) {
      VALUE_TYPE_MAP.put(t.getValue(), t);
      STRING_TYPE_MAP.put(t.name(), t);
    }
  }

  /**
   * The value of this dns record type.
   */
  private final int value;

  private final boolean addable;

  private final boolean updatable;

  private final boolean queryable;

  private final UnaryOperator<String> toSambaToolValueTransformer;

  DnsEntryType(
      int value,
      boolean addable,
      boolean updatable,
      boolean queryable,
      UnaryOperator<String> toSambaToolValueTransformer) {
    this.value = value;
    this.addable = addable;
    this.updatable = updatable;
    this.queryable = queryable;
    this.toSambaToolValueTransformer = toSambaToolValueTransformer;
  }

  /**
   * Find dns record type by integer.
   *
   * @param value the value
   * @return the dns record type
   */
  public static DnsEntryType fromValue(int value) {
    return VALUE_TYPE_MAP.getOrDefault(value, ALL);
  }

  /**
   * Find dns record type by string.
   *
   * @param value the value
   * @param defaultType the default type
   * @return the dns record type
   */
  public static DnsEntryType fromValue(String value, DnsEntryType defaultType) {
    if (value == null) {
      return defaultType;
    }
    return STRING_TYPE_MAP.getOrDefault(value.toUpperCase(), defaultType);
  }

  public static List<DnsEntryType> getSupportedAddOrDeleteTypes() {
    return Arrays.stream(values())
        .filter(DnsEntryType::isAddable)
        .sorted(Comparator.comparing(DnsEntryType::name))
        .toList();
  }

  public static List<DnsEntryType> getSupportedUpdateTypes(DnsEntry dnsEntry) {
    return Arrays.stream(values())
        .filter(type -> SOA.equals(type) ? isSoa(dnsEntry) : type.isUpdatable())
        .sorted(Comparator.comparing(DnsEntryType::name))
        .toList();
  }

  private static boolean isSoa(DnsEntry dnsEntry) {
    if (isEmpty(dnsEntry) || isEmpty(dnsEntry.getType())) {
      return false;
    }
    return SOA.equals(dnsEntry.getType());
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  static class ToSambaToolValueTransformers {

    private static final String FQDN_DNS = "ns";

    private static final String FQDN_MAIL = "email";

    private static final String SERIAL = "serial";

    private static final String REFRESH = "refresh";

    private static final String RETRY = "retry";

    private static final String EXPIRE = "expire";

    private static final String MINIMUM_TTL = "minttl";

    public static final UnaryOperator<String> FQDN_TRANSFORMER = fqdn -> {
      if (isEmpty(fqdn)) {
        throw ServiceException.badRequest("FQDN must be present.",
            ErrorCode.EC_DNS_FQDN_MISSING);
      }
      if (!fqdn.endsWith(".")) {
        return fqdn + '.';
      }
      return fqdn;
    };

    public static final UnaryOperator<String> SOA_TRANSFORMER = soa -> {
      if (isEmpty(soa)) {
        throw ServiceException.badRequest("SOA value must be present.",
            ErrorCode.EC_DNS_SOA_MISSING);
      }
      // input: serial=7, refresh=900, retry=600, expire=86400, minttl=3600, ns=ns1.exampleorg., email=hostmaster.example.org.
      // output: "fqdn_dns fqdn_email serial refresh retry expire minimumttl"
      return '"'
          + FQDN_TRANSFORMER.apply(getSoaValue(FQDN_DNS, soa)) + " "
          + FQDN_TRANSFORMER.apply(getSoaValue(FQDN_MAIL, soa)) + " "
          + getSoaValue(SERIAL, soa) + " "
          + getSoaValue(REFRESH, soa) + " "
          + getSoaValue(RETRY, soa) + " "
          + getSoaValue(EXPIRE, soa) + " "
          + getSoaValue(MINIMUM_TTL, soa)
          + '"';
    };

    private static String getSoaValue(String name, String input) {
      int start = input.indexOf(name + "=");
      if (start == -1) {
        throw ServiceException.badRequest(String.format("SOA entry '%s' was not found.", name),
            ErrorCode.EC_DNS_SOA_MISSING + '-' + name);
      }
      int end = input.indexOf(',', start);
      if (end == -1) {
        end = input.length();
      }
      return input.substring(start + name.length() + 1, end);
    }

    public static final UnaryOperator<String> MX_TRANSFORMER = mx -> {
      if (isEmpty(mx)) {
        throw ServiceException.badRequest("MX value must be present.",
            ErrorCode.EC_DNS_MX_MISSING);
      }
      // input: mail.example.org. (preference)
      String fqdn;
      String pref;
      int index = mx.indexOf(' ');
      if (index > 0) {
        fqdn = mx.substring(0, index).trim();
        pref = mx.substring(index + 1).trim();
        if (pref.startsWith("(") && pref.endsWith(")")) {
          pref = pref.substring(1, pref.length() - 1).trim();
        }
      } else {
        fqdn = mx.trim();
        pref = "0";
      }
      // output: "fqdn_string preference"
      return '"'
          + FQDN_TRANSFORMER.apply(fqdn)
          + ' '
          + pref
          + '"';
    };

    public static final UnaryOperator<String> SRV_TRANSFORMER = srv -> {
      if (isEmpty(srv)) {
        throw ServiceException.badRequest("SRV value must be present.",
            ErrorCode.EC_DNS_SRV_MISSING);
      }
      // input: foo.example.org. (port, priority, weight)
      String fqdn;
      String port;
      String priority;
      String weight;
      int index = srv.indexOf(' ');
      if (index > 0) {
        fqdn = srv.substring(0, index).trim();
        String tmp = srv.substring(index + 1).trim();
        if (tmp.startsWith("(") && tmp.endsWith(")")) {
          tmp = tmp.substring(1, tmp.length() - 1).trim();
        }
        String[] values = tmp.split(",");
        if (values.length > 0) {
          port = values[0].trim();
        } else {
          port = "0";
        }
        if (values.length > 1) {
          priority = values[1].trim();
        } else {
          priority = "0";
        }
        if (values.length > 2) {
          weight = values[2].trim();
        } else {
          weight = "0";
        }

      } else {
        fqdn = srv.trim();
        port = "0";
        priority = "0";
        weight = "0";
      }
      // output: "fqdn_string port priority weight"
      return '"'
          + FQDN_TRANSFORMER.apply(fqdn) + ' '
          + port + ' '
          + priority + ' '
          + weight
          + '"';
    };

    public static final UnaryOperator<String> TXT_TRANSFORMER = txt -> {
      if (isEmpty(txt)) {
        throw ServiceException.badRequest("TXT value must be present.",
            ErrorCode.EC_DNS_TXT_MISSING);
      }
      // input: "string1","string2","string3"
      String parameter = Arrays.stream(txt.split(Pattern.quote("\",\"")))
          .map(String::trim)
          .map(txtToken -> {
            if (txtToken.startsWith("\"")) {
              return txtToken.substring(1).trim();
            }
            return txtToken;
          })
          .map(txtToken -> {
            if (txtToken.endsWith("\"")) {
              return txtToken.substring(0, txtToken.length() - 1).trim();
            }
            return txtToken;
          })
          .map(String::trim)
          .map(txtToken -> "'" + txtToken + "'")
          .collect(Collectors.joining(" "));
      // output: "'string1' 'string2' ..."
      return '"' + parameter + '"';
    };

  }
}
