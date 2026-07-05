package org.bremersee.samba.ad.dc.repository.mock;

import static java.util.Objects.nonNull;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.model.DnsZoneType;
import org.bremersee.samba.ad.dc.repository.SambaToolDns;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Primary
@Component
@Profile("mock")
class MockedSambaToolDns implements SambaToolDns {

  private static final AtomicInteger serial = new AtomicInteger(10000);

  private final SambaStore store;

  MockedSambaToolDns(SambaStore store) {
    this.store = store;
  }

  @Override
  public List<String> getDnsZoneNames(String hostName, DnsZoneType zoneType) {
    DnsZoneType type = Objects.requireNonNullElse(zoneType, DnsZoneType.PRIMARY);
    return store.getDns().keySet().stream()
        .filter(dnsZone -> isDnsZoneOfType(dnsZone, type))
        .map(DnsZone::getName)
        .toList();
  }

  private boolean isDnsZoneOfType(DnsZone dnsZone, DnsZoneType type) {
    boolean isReverseType = DnsZoneType.REVERSE.equals(type);
    return (isReverseType && Boolean.TRUE.equals(dnsZone.getReverseZone())) || Optional
        .ofNullable(dnsZone.getZoneType())
        .map(dnsZoneType -> dnsZoneType.equalsIgnoreCase(type.name()))
        .orElse(false);
  }

  @Override
  public Optional<DnsZone> findDnsZone(String hostName, String zoneName) {
    return store.getDns().keySet().stream()
        .filter(dnsZone -> dnsZone.getName().equalsIgnoreCase(zoneName))
        .findFirst();
  }

  @Override
  public void createDnsZone(String hostName, String zoneName) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    DnsZone dnsZone = DnsZone.builder()
        .distinguishedName("")
        .created(now)
        .modified(now)
        .name(zoneName)
        .zoneType(DnsZoneType.PRIMARY.name())
        .reverseZone(zoneName.toLowerCase().endsWith(".in-addr.arpa"))
        .fqdn("DomainDnsZones." + zoneName)
        .allowUpdate("DNS_ZONE_UPDATE_SECURE")
        .build();
    store.getDns().put(dnsZone, new CopyOnWriteArrayList<>());
  }

  @Override
  public void deleteDnsZone(String hostName, String zoneName) {
    findDnsZone(hostName, zoneName)
        .ifPresent(dnsZone -> store.getDns().remove(dnsZone));
  }

  @Override
  public List<DnsEntry> getDnsEntries(String hostName, String zoneName) {
    return store.getDns().entrySet()
        .stream()
        .filter(entry -> entry.getKey().getName().equalsIgnoreCase(zoneName))
        .findFirst()
        .map(Entry::getValue)
        .orElseGet(List::of);
  }

  @Override
  public void addDnsEntry(String hostName, DnsEntry entry) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    DnsEntry dnsEntry = DnsEntry.builder()
        .from(entry)
        .distinguishedName("")
        .created(now)
        .modified(now)
        .flags("f0")
        .serial(serial.getAndIncrement())
        .ttlSeconds(3600)
        .build();
    store.getDns().entrySet().stream()
        .filter(mapEntry -> mapEntry.getKey().getName()
            .equalsIgnoreCase(entry.getZoneName()))
        .findFirst()
        .map(Entry::getValue)
        .ifPresent(dnsEntries -> dnsEntries.add(dnsEntry));
  }

  @Override
  public void updateDnsEntry(String hostName, DnsEntry entry, String newValue) {
    deleteDnsEntry(hostName, entry);
    addDnsEntry(hostName, DnsEntry.builder()
        .from(entry)
        .value(newValue)
        .build());
  }

  @Override
  public void deleteDnsEntry(String hostName, DnsEntry entry) {
    store.getDns().entrySet()
        .stream()
        .filter(mapEntry -> mapEntry.getKey().getName()
            .equalsIgnoreCase(entry.getZoneName()))
        .findFirst()
        .map(Entry::getValue)
        .ifPresent(dnsEntries -> dnsEntries
            .removeIf(dnsEntry -> areEqual(dnsEntry, entry)));

  }

  private boolean areEqual(DnsEntry e1, DnsEntry e2) {
    return nonNull(e2)
        && nonNull(e1)
        && nonNull(e1.getName())
        && nonNull(e1.getValue())
        && nonNull(e1.getType())
        && e1.getName().equalsIgnoreCase(e2.getName())
        && e1.getValue().equalsIgnoreCase(e2.getValue())
        && e1.getType().equals(e2.getType());
  }
}
