package org.bremersee.samba.ad.dc.repository.mapper;

import static java.util.Objects.isNull;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.samba.ad.dc.model.DomainGroupMember;
import org.bremersee.samba.ad.dc.model.DomainGroupMemberType;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.dn.Dn;

public class DomainGroupMemberLdapMapper extends SamAccountLdapMapper<DomainGroupMember> {

  @Getter(AccessLevel.PROTECTED)
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public DomainGroupMemberLdapMapper() {
    super(DomainGroupMember::new);
    mappedAttributes = initMappedAttributesOfDomainGroupMember();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfDomainGroupMember() {
    Set<LdaptiveAttribute<?>> attributeNames = new LinkedHashSet<>(super.getMappedAttributes());
    attributeNames.add(AdConstants.OBJECT_CLASS);
    attributeNames.add(AdConstants.USER_GIVEN_NAME);
    attributeNames.add(AdConstants.USER_SN);
    attributeNames.add(AdConstants.USER_DISPLAY_NAME);
    attributeNames.add(AdConstants.NAME);
    return attributeNames;
  }

  @Override
  public void map(LdapEntry source, DomainGroupMember destination) {

    if (isNull(source) || isNull(destination)) {
      return;
    }
    super.map(source, destination);
    destination.setMemberType(Optional
        .ofNullable(source.getAttribute(AdConstants.OBJECT_CLASS.getName()))
        .map(LdapAttribute::getStringValues)
        .map(DomainGroupMemberType::fromObjectClasses)
        .orElse(DomainGroupMemberType.UNKNOWN));
    destination.setDisplayName(getMemberDisplayName(source));
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
