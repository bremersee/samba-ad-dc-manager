package org.bremersee.samba.ad.dc.repository.mapper;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveEntryImmutableMapper;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.springframework.util.Assert;

public class DomainComputerLdapMapper extends LdaptiveEntryImmutableMapper<DomainComputer> {

  private final SamAccountLdapMapper samAccountLdapMapper;

  @Getter
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public DomainComputerLdapMapper() {
    samAccountLdapMapper = new SamAccountLdapMapper();
    mappedAttributes = initMappedAttributesOfDomainComputer();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfDomainComputer() {
    var attributeNames = new LinkedHashSet<>(samAccountLdapMapper.getMappedAttributes());
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
  public String[] getObjectClasses() {
    return new String[0];
  }

  @Override
  public String[] getMappedAttributeNames() {
    return getMappedAttributes().stream()
        .map(LdaptiveAttribute::getName)
        .toArray(String[]::new);
  }

  @Override
  public String[] getBinaryAttributeNames() {
    return getMappedAttributes().stream()
        .filter(LdaptiveAttribute::isBinary)
        .map(LdaptiveAttribute::getName)
        .toArray(String[]::new);
  }

  @Override
  public String mapDn(DomainComputer domainObject) {
    Assert.hasText(domainObject.getDistinguishedName(), "DN of ldap entry is required.");
    return domainObject.getDistinguishedName();
  }

  @Override
  public DomainComputer map(LdapEntry source) {
    if (isEmpty(source)) {
      return null;
    }
    DomainComputer.Builder builder = DomainComputer.builder()
        .from(samAccountLdapMapper.map(source));
    AdConstants.NAME
        .getValue(source)
        .ifPresent(builder::name);
    AdConstants.COMPUTER_DNS_HOST_NAME
        .getValue(source)
        .ifPresent(builder::dnsHostName);
    builder.networkAddresses(AdConstants.COMPUTER_NETWORK_ADDRESS
        .getValues(source)
        .toList());
    AdConstants.COMPUTER_OPERATING_SYSTEM
        .getValue(source)
        .ifPresent(builder::operatingSystem);
    AdConstants.COMPUTER_OPERATING_SYSTEM_VERSION
        .getValue(source)
        .ifPresent(builder::operatingSystemVersion);
    AdConstants.DESCRIPTION
        .getValue(source)
        .ifPresent(builder::description);
    builder.servicePrincipalNames(AdConstants.COMPUTER_SERVICE_PRINCIPAL_NAME
        .getValues(source)
        .toList());
    return builder.build();
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(
      DomainComputer source,
      LdapEntry destination) {

    if (isNull(source) || isNull(destination)) {
      return new AttributeModification[0];
    }
    var modifications = new ArrayList<>(Arrays.asList(samAccountLdapMapper
        .mapAndComputeModifications(source, destination)));
    AdConstants.DESCRIPTION
        .setValue(destination, source.getDescription())
        .ifPresent(modifications::add);
    return modifications.toArray(new AttributeModification[0]);
  }
}
