package org.bremersee.samba.ad.dc.samaccount.common.repository.mapper;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveEntryImmutableMapper;
import org.bremersee.samba.ad.dc.common.DnTool;
import org.bremersee.samba.ad.dc.common.repository.AdConstants;
import org.bremersee.samba.ad.dc.common.repository.mapper.AdEntryLdapMapper;
import org.bremersee.samba.ad.dc.samaccount.common.model.SamAccount;
import org.bremersee.samba.ad.dc.samaccount.common.model.SamAccountModel;
import org.bremersee.samba.ad.dc.samaccount.common.model.Sid;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;
import org.springframework.util.Assert;

public class SamAccountLdapMapper extends LdaptiveEntryImmutableMapper<SamAccount> {

  private final AdEntryLdapMapper adEntryLdapMapper;

  @Getter
  private final Set<LdaptiveAttribute<?>> mappedAttributes;

  public SamAccountLdapMapper() {
    adEntryLdapMapper = new AdEntryLdapMapper();
    mappedAttributes = initMappedAttributesOfSamAccount();
  }

  private Set<LdaptiveAttribute<?>> initMappedAttributesOfSamAccount() {
    var attributeNames = new LinkedHashSet<>(adEntryLdapMapper.getMappedAttributes());
    attributeNames.add(AdConstants.SAM_ACCOUNT_NAME);
    attributeNames.add(AdConstants.OBJECT_SID);
    attributeNames.add(AdConstants.IS_CRITICAL_SYSTEM_OBJECT);
    attributeNames.add(AdConstants.PRIMARY_GROUP_ID);
    attributeNames.add(AdConstants.MEMBER_OF_GROUP);
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
  public String mapDn(SamAccount domainObject) {
    Assert.hasText(domainObject.getDistinguishedName(), "DN of ldap entry is required.");
    return domainObject.getDistinguishedName();
  }

  @Override
  public SamAccount map(LdapEntry source) {
    if (isEmpty(source)) {
      return null;
    }
    SamAccountModel.Builder builder = SamAccountModel.builder()
        .from(adEntryLdapMapper.map(source));
    AdConstants.SAM_ACCOUNT_NAME
        .getValue(source)
        .ifPresent(builder::samAccountName);
    Optional<Sid> sid = AdConstants.OBJECT_SID
        .getValue(source);
    sid.ifPresent(builder::sid);
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT
        .getValue(source, false)
        .ifPresent(builder::criticalSystemObject);
    AdConstants.PRIMARY_GROUP_ID
        .getValue(source, sid.map(Sid::getSuffix).orElse(null))
        .ifPresent(builder::primaryGroupId);
    builder.memberships(AdConstants.MEMBER_OF_GROUP
        .getValues(source)
        .map(dn -> dn.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER))
        .toList());
    return builder.build();
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(SamAccount source,
      LdapEntry destination) {

    if (isNull(source) || isNull(destination)) {
      return new AttributeModification[0];
    }
    var modifications = new ArrayList<>(Arrays.asList(adEntryLdapMapper
        .mapAndComputeModifications(source, destination)));

    boolean isCriticalSystemObject = AdConstants.IS_CRITICAL_SYSTEM_OBJECT
        .getValue(destination, source.isCriticalSystemObject())
        .orElse(false);
    if (!isCriticalSystemObject) {
      AdConstants.SAM_ACCOUNT_NAME
          .setValue(destination, source.getSamAccountName())
          .ifPresent(modifications::add);
      AdConstants.PRIMARY_GROUP_ID
          .setValue(
              destination,
              source.getPrimaryGroupId(),
              (oldValue, newValue) -> !isEmpty(oldValue) && !isEmpty(newValue))
          .ifPresent(modifications::add);
    }
    return modifications.toArray(AttributeModification[]::new);
  }

}
