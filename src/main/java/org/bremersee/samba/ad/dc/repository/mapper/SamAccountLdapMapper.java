package org.bremersee.samba.ad.dc.repository.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.bremersee.ldaptive.LdaptiveEntryMapper.getAttributeValue;
import static org.bremersee.ldaptive.LdaptiveEntryMapper.getAttributeValuesAsList;
import static org.bremersee.ldaptive.LdaptiveEntryMapper.setAttribute;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.samba.ad.dc.model.SamAccount;
import org.bremersee.samba.ad.dc.model.Sid;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.ldaptive.dn.Dn;

public class SamAccountLdapMapper<T extends SamAccount> extends AdEntryLdapMapper<T> {

  @Getter(AccessLevel.PROTECTED)
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public SamAccountLdapMapper(Supplier<T> destinationSupplier) {
    super(destinationSupplier);
    mappedAttributes = initMappedAttributesOfSamAccount();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfSamAccount() {
    Set<LdaptiveAttribute<?>> attributeNames = new LinkedHashSet<>(super.getMappedAttributes());
    attributeNames.add(AdConstants.SAM_ACCOUNT_NAME);
    attributeNames.add(AdConstants.OBJECT_SID);
    attributeNames.add(AdConstants.IS_CRITICAL_SYSTEM_OBJECT);
    attributeNames.add(AdConstants.PRIMARY_GROUP_ID);
    attributeNames.add(AdConstants.MEMBER_OF_GROUP);
    return attributeNames;
  }

  @Override
  public void map(LdapEntry source, T destination) {
    if (isNull(source) || isNull(destination)) {
      return;
    }
    super.map(source, destination);

    String samAccountName = getAttributeValue(
        source, AdConstants.SAM_ACCOUNT_NAME, null);
    destination.setSamAccountName(samAccountName);

    Sid sid = getAttributeValue(source, AdConstants.OBJECT_SID, null);
    destination.setSid(sid);

    boolean isCriticalSystemObject = getAttributeValue(
        source, AdConstants.IS_CRITICAL_SYSTEM_OBJECT, false);
    destination.setCriticalSystemObject(isCriticalSystemObject);

    Integer primaryGroupId = getAttributeValue(
        source, AdConstants.PRIMARY_GROUP_ID, null);
    if (isNull(primaryGroupId) && nonNull(sid)) {
      // samAccount is a domain group
      destination.setPrimaryGroupId(sid.getSuffix());
    } else {
      // samAccount is a domain user or computer
      destination.setPrimaryGroupId(primaryGroupId);
    }

    List<Dn> memberships = getAttributeValuesAsList(
        source, AdConstants.MEMBER_OF_GROUP);
    destination.setMemberships(memberships.stream().map(Dn::format).toList());
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(T source, LdapEntry destination) {
    if (isNull(source) || isNull(destination)) {
      return new AttributeModification[0];
    }
    var modifications = toNewList(super.mapAndComputeModifications(source, destination));
    boolean isCriticalSystemObject = getAttributeValue(
        destination, AdConstants.IS_CRITICAL_SYSTEM_OBJECT, false);
    if (!isCriticalSystemObject) {
      setAttribute(
          destination,
          AdConstants.SAM_ACCOUNT_NAME,
          source.getSamAccountName(),
          modifications);
      Integer previousPrimaryGroupId = getAttributeValue(
          destination, AdConstants.PRIMARY_GROUP_ID, null);
      if (nonNull(source.getPrimaryGroupId()) && nonNull(previousPrimaryGroupId)) {
        setAttribute(
            destination,
            AdConstants.PRIMARY_GROUP_ID,
            source.getPrimaryGroupId(),
            modifications);
      }
    }
    return modifications.toArray(new AttributeModification[0]);
  }

}
