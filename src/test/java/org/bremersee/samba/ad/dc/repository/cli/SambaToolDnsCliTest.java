package org.bremersee.samba.ad.dc.repository.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.bremersee.samba.ad.dc.repository.DnsEntryRepository.ZONE_ENTRIES_NODE_NAME;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsEntryType;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.model.DnsZoneType;
import org.bremersee.samba.ad.dc.repository.cli.parser.DnsEntriesParser;
import org.bremersee.samba.ad.dc.repository.cli.parser.DnsZoneListParser;
import org.bremersee.samba.ad.dc.repository.cli.parser.DnsZoneParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SambaToolDnsCliTest {

  private static final DnsZoneListParser zoneListParser = DnsZoneListParser.defaultParser();

  private static final DnsZoneParser zoneParser = DnsZoneParser.defaultParser();

  private static final Function<String, DnsEntriesParser> dnsEntriesParserFn
      = zoneName -> DnsEntriesParser.defaultParser(zoneName, ZONE_ENTRIES_NODE_NAME);

  private SambaToolDnsCli target;

  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    target = spy(new SambaToolDnsCli(properties));
    target.setDnsEntriesParserFn(dnsEntriesParserFn);
    target.setZoneListParser(zoneListParser);
    target.setZoneParser(zoneParser);
  }

  @Test
  void getSubCommand() {
    assertThat(target.getSubCommand())
        .isEqualTo("dns");
  }

  @Test
  void needsSambaToolCredentials() {
    assertThat(target.needsSambaToolCredentials())
        .isTrue();
  }

  @Test
  void getDnsZoneNames() {
    CommandExecutorResponse response = getZoneListResponse();
    doReturn(response)
        .when(target)
        .execute(anyList());
    List<String> expected = zoneListParser.parse(response);
    List<String> actual = target.getDnsZoneNames("dc1", DnsZoneType.PRIMARY);
    assertThat(actual)
        .containsExactlyElementsOf(expected);
  }

  @Test
  void findDnsZone() {
    CommandExecutorResponse response = getZoneResponse();
    doReturn(response)
        .when(target)
        .execute(anyList());
    DnsZone expected = zoneParser.parse(response).orElse(null);
    assertThat(expected)
        .isNotNull();
    Optional<DnsZone> actual = target.findDnsZone("dc1", "samdon.example.org");
    assertThat(actual)
        .hasValue(expected);
  }

  @Test
  void createDnsZone() {
    CommandExecutorResponse response = new CommandExecutorResponse(
        "Zone intranet created successfully", null);
    doReturn(response)
        .when(target)
        .execute(anyList());
    assertThatNoException()
        .isThrownBy(() -> target.createDnsZone("dc1", "intranet"));
  }

  @Test
  void deleteDnsZone() {
    CommandExecutorResponse response = new CommandExecutorResponse(
        "Zone intranet deleted successfully", null);
    doReturn(response)
        .when(target)
        .execute(anyList());
    assertThatNoException()
        .isThrownBy(() -> target.deleteDnsZone("dc1", "intranet"));
  }

  @Test
  void getDnsEntries() {
    CommandExecutorResponse response = getDnsEntriesResponse();
    doReturn(response)
        .when(target)
        .execute(anyList());
    List<DnsEntry> expected = dnsEntriesParserFn.apply("samdon.example.org")
        .parse(response)
        .toList();
    List<DnsEntry> actual = target.getDnsEntries("dc1", "samdon.example.org");
    assertThat(actual)
        .containsExactlyElementsOf(expected);
  }

  @Test
  void addDnsEntry() {
    CommandExecutorResponse response = new CommandExecutorResponse(
        "Record added successfully", null);
    doReturn(response)
        .when(target)
        .execute(anyList());
    DnsEntry dnsEntry = DnsEntry.builder()
        .zoneName("samdon.example.org")
        .name("data")
        .type(DnsEntryType.A)
        .value("192.168.1.7")
        .build();
    assertThatNoException()
        .isThrownBy(() -> target.addDnsEntry("dc1", dnsEntry));
  }

  @Test
  void updateDnsEntry() {
    CommandExecutorResponse response = new CommandExecutorResponse(
        "Record updated successfully", null);
    doReturn(response)
        .when(target)
        .execute(anyList());
    DnsEntry dnsEntry = DnsEntry.builder()
        .zoneName("samdon.example.org")
        .name("data")
        .type(DnsEntryType.A)
        .value("192.168.1.7")
        .build();
    assertThatNoException()
        .isThrownBy(() -> target.updateDnsEntry("dc1", dnsEntry, "192.168.1.8"));
  }

  @Test
  void deleteDnsEntry() {
    CommandExecutorResponse response = new CommandExecutorResponse(
        "Record deleted successfully", null);
    doReturn(response)
        .when(target)
        .execute(anyList());
    DnsEntry dnsEntry = DnsEntry.builder()
        .zoneName("samdon.example.org")
        .name("data")
        .type(DnsEntryType.A)
        .value("192.168.1.7")
        .build();
    assertThatNoException()
        .isThrownBy(() -> target.deleteDnsEntry("dc1", dnsEntry));
  }

  private static CommandExecutorResponse getZoneListResponse() {
    String stdout = """
          2 zone(s) found
        
          pszZoneName                 : 1.168.192.in-addr.arpa
          Flags                       : DNS_RPC_ZONE_DSINTEGRATED DNS_RPC_ZONE_UPDATE_SECURE
          ZoneType                    : DNS_ZONE_TYPE_PRIMARY
          Version                     : 50
          dwDpFlags                   : DNS_DP_AUTOCREATED DNS_DP_DOMAIN_DEFAULT DNS_DP_ENLISTED
          pszDpFqdn                   : DomainDnsZones.samdom.example.org
        
          pszZoneName                 : samdom.example.org
          Flags                       : DNS_RPC_ZONE_DSINTEGRATED DNS_RPC_ZONE_UPDATE_SECURE
          ZoneType                    : DNS_ZONE_TYPE_PRIMARY
          Version                     : 50
          dwDpFlags                   : DNS_DP_AUTOCREATED DNS_DP_DOMAIN_DEFAULT DNS_DP_ENLISTED
          pszDpFqdn                   : DomainDnsZones.samdom.example.org
        """;
    return new CommandExecutorResponse(stdout, null);
  }

  private static CommandExecutorResponse getZoneResponse() {
    String stdout = """
        pszZoneName                 : samdom.example.org
        dwZoneType                  : DNS_ZONE_TYPE_PRIMARY
        fReverse                    : FALSE
        fAllowUpdate                : DNS_ZONE_UPDATE_SECURE
        fPaused                     : FALSE
        fShutdown                   : FALSE
        fAutoCreated                : FALSE
        fUseDatabase                : TRUE
        pszDataFile                 : None
        aipMasters                  : []
        fSecureSecondaries          : DNS_ZONE_SECSECURE_NO_XFER
        fNotifyLevel                : DNS_ZONE_NOTIFY_LIST_ONLY
        aipSecondaries              : []
        aipNotify                   : []
        fUseWins                    : FALSE
        fUseNbstat                  : FALSE
        fAging                      : FALSE
        dwNoRefreshInterval         : 168
        dwRefreshInterval           : 168
        dwAvailForScavengeTime      : 0
        aipScavengeServers          : []
        dwRpcStructureVersion       : 0x2
        dwForwarderTimeout          : 0
        fForwarderSlave             : 0
        aipLocalMasters             : []
        dwDpFlags                   : DNS_DP_AUTOCREATED DNS_DP_DOMAIN_DEFAULT DNS_DP_ENLISTED
        pszDpFqdn                   : DomainDnsZones.samdom.example.org
        pwszZoneDn                  : DC=samdom.example.org,CN=MicrosoftDNS,DC=DomainDnsZones,DC=samdom,DC=example,DC=org
        dwLastSuccessfulSoaCheck    : 0
        dwLastSuccessfulXfr         : 0
        fQueuedForBackgroundLoad    : FALSE
        fBackgroundLoadInProgress   : FALSE
        fReadOnlyZone               : FALSE
        dwLastXfrAttempt            : 0
        dwLastXfrResult             : 0
        """;
    return new CommandExecutorResponse(stdout, null);
  }

  private static CommandExecutorResponse getDnsEntriesResponse() {
    String stdout = """
        Name=, Records=3, Children=0
          SOA: serial=449810, refresh=900, retry=600, expire=86400, minttl=3600, ns=dc1.samdom.example.org., email=hostmaster.samdom.example.org. (flags=600000f0, serial=449809, ttl=3600)
          NS: dc1.samdom.example.org. (flags=600000f0, serial=449809, ttl=900)
          A: 192.168.1.3 (flags=600000f0, serial=449809, ttl=900)
        Name=_sites, Records=0, Children=1
        Name=_tcp, Records=0, Children=4
        Name=_udp, Records=0, Children=2
        Name=data, Records=1, Children=0
          A: 192.168.1.4 (flags=f0, serial=428109, ttl=3600)
        Name=ha, Records=1, Children=0
          A: 192.168.1.5 (flags=f0, serial=428110, ttl=3600)
        Name=proxy, Records=1, Children=0
          CNAME: ha.samdom.example.org. (flags=f0, serial=446881, ttl=900)
        """;
    return new CommandExecutorResponse(stdout, null);
  }

}