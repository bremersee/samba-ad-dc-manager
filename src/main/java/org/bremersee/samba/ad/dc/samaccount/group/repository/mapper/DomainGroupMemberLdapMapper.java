package org.bremersee.samba.ad.dc.samaccount.group.repository.mapper;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveEntryImmutableMapper;
import org.bremersee.samba.ad.dc.samaccount.group.model.DomainGroupMember;
import org.bremersee.samba.ad.dc.samaccount.group.model.DomainGroupMemberType;
import org.bremersee.samba.ad.dc.common.repository.AdConstants;
import org.bremersee.samba.ad.dc.samaccount.common.repository.mapper.SamAccountLdapMapper;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.dn.Dn;
import org.springframework.util.Assert;

public class DomainGroupMemberLdapMapper extends LdaptiveEntryImmutableMapper<DomainGroupMember> {

  private final SamAccountLdapMapper samAccountLdapMapper;

  @Getter
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public DomainGroupMemberLdapMapper() {
    samAccountLdapMapper = new SamAccountLdapMapper();
    mappedAttributes = initMappedAttributesOfDomainGroupMember();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfDomainGroupMember() {
    var attributeNames = new LinkedHashSet<>(samAccountLdapMapper.getMappedAttributes());
    attributeNames.add(AdConstants.OBJECT_CLASS);
    attributeNames.add(AdConstants.USER_GIVEN_NAME);
    attributeNames.add(AdConstants.USER_SN);
    attributeNames.add(AdConstants.USER_DISPLAY_NAME);
    attributeNames.add(AdConstants.NAME);
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
  public String mapDn(DomainGroupMember domainObject) {
    Assert.hasText(domainObject.getDistinguishedName(), "DN of ldap entry is required.");
    return domainObject.getDistinguishedName();
  }

  @Override
  public DomainGroupMember map(LdapEntry source) {
    if (isEmpty(source)) {
      return null;
    }
    DomainGroupMember.Builder builder = DomainGroupMember.builder()
        .from(samAccountLdapMapper.map(source));
    builder.memberType(Optional
        .ofNullable(source.getAttribute(AdConstants.OBJECT_CLASS.getName()))
        .map(LdapAttribute::getStringValues)
        .map(DomainGroupMemberType::fromObjectClasses)
        .orElse(DomainGroupMemberType.UNKNOWN));
    builder.displayName(getMemberDisplayName(source));
    return builder.build();
  }

  private String getMemberDisplayName(LdapEntry member) {
    return Optional.ofNullable(
            member.getAttribute(AdConstants.USER_GIVEN_NAME.getName()))
        .map(LdapAttribute::getStringValue)
        .flatMap(firstName -> Optional
            .ofNullable(member.getAttribute(AdConstants.USER_SN.getName()))
            .map(LdapAttribute::getStringValue)
            .map(lastName -> firstName + " " + lastName))
        .or(() -> Optional.ofNullable(
                member.getAttribute(AdConstants.USER_DISPLAY_NAME.getName()))
            .map(LdapAttribute::getStringValue))
        .or(() -> Optional.ofNullable(member.getAttribute(AdConstants.NAME.getName()))
            .map(LdapAttribute::getStringValue))
        .orElseGet(() -> new Dn(member.getDn()).getRDn().getNameValue().getStringValue());
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(
      DomainGroupMember source, LdapEntry destination) {
    return new AttributeModification[0];
  }

}
