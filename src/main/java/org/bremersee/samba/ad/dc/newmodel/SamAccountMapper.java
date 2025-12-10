package org.bremersee.samba.ad.dc.newmodel;

import org.bremersee.ldaptive.LdaptiveEntryImmutableMapper;
import org.bremersee.samba.ad.dc.newmodel.SamAccount.Builder;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;

public class SamAccountMapper extends LdaptiveEntryImmutableMapper<SamAccount> {

  private final AdEntryMapper adEntryMapper = new AdEntryMapper();

  @Override
  public String[] getObjectClasses() {
    return new String[0];
  }

  @Override
  public String mapDn(SamAccount domainObject) {
    return "";
  }

  @Override
  public SamAccount map(LdapEntry source) {
    AdEntry adEntry = adEntryMapper.map(source);
    Builder builder = SamAccount.builder().from(adEntry);
    AdConstants.SAM_ACCOUNT_NAME
        .getValue(source)
        .consume(builder::samAccountName);
    return builder.build();
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(SamAccount source,
      LdapEntry destination) {
    return new AttributeModification[0];
  }

}
