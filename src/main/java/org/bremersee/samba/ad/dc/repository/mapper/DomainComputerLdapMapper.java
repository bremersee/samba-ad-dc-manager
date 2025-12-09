package org.bremersee.samba.ad.dc.repository.mapper;

import static java.util.Objects.isNull;
import static org.bremersee.ldaptive.LdaptiveEntryMapper.getAttributeValue;
import static org.bremersee.ldaptive.LdaptiveEntryMapper.getAttributeValuesAsList;
import static org.bremersee.ldaptive.LdaptiveEntryMapper.setAttribute;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;

public class DomainComputerLdapMapper extends SamAccountLdapMapper<DomainComputer> {

  @Getter(AccessLevel.PROTECTED)
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public DomainComputerLdapMapper() {
    super(DomainComputer::new);
    mappedAttributes = initMappedAttributesOfDomainComputer();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfDomainComputer() {
    Set<LdaptiveAttribute<?>> attributeNames = new LinkedHashSet<>(super.getMappedAttributes());
    attributeNames.add(AdConstants.NAME);
    attributeNames.add(AdConstants.COMPUTER_DNS_HOST_NAME);
    attributeNames.add(AdConstants.COMPUTER_NETWORK_ADDRESS);
    attributeNames.add(AdConstants.COMPUTER_OPERATING_SYSTEM);
    attributeNames.add(AdConstants.COMPUTER_OPERATING_SYSTEM_VERSION);
    attributeNames.add(AdConstants.DESCRIPTION);
    attributeNames.add(AdConstants.COMPUTER_SERVICE_PRINCIPAL_NAME);
    return attributeNames;
  }

  @Override
  public void map(LdapEntry source, DomainComputer destination) {
    if (isNull(source) || isNull(destination)) {
      return;
    }
    super.map(source, destination);

    String name = getAttributeValue(source, AdConstants.NAME, null);
    destination.setName(name);

    String dnsHostName = getAttributeValue(
        source, AdConstants.COMPUTER_DNS_HOST_NAME, null);
    destination.setDnsHostName(dnsHostName);

    List<String> networkAddresses = getAttributeValuesAsList(
        source, AdConstants.COMPUTER_NETWORK_ADDRESS);
    destination.setNetworkAddresses(networkAddresses);

    String operatingSystem = getAttributeValue(
        source, AdConstants.COMPUTER_OPERATING_SYSTEM, null);
    destination.setOperatingSystem(operatingSystem);

    String operatingSystemVersion = getAttributeValue(
        source, AdConstants.COMPUTER_OPERATING_SYSTEM_VERSION, null);
    destination.setOperatingSystemVersion(operatingSystemVersion);

    String description = getAttributeValue(source, AdConstants.DESCRIPTION, null);
    destination.setDescription(description);

    List<String> servicePrincipalNames = getAttributeValuesAsList(
        source, AdConstants.COMPUTER_SERVICE_PRINCIPAL_NAME);
    destination.setServicePrincipalNames(servicePrincipalNames);
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(DomainComputer source,
      LdapEntry destination) {

    if (isNull(source) || isNull(destination)) {
      return new AttributeModification[0];
    }
    var modifications = toNewList(super.mapAndComputeModifications(source, destination));

    setAttribute(
        destination,
        AdConstants.DESCRIPTION,
        source.getDescription(),
        modifications);

    return modifications.toArray(new AttributeModification[0]);
  }
}
