package org.bremersee.samba.ad.dc.newmodel;

import java.time.OffsetDateTime;
import org.bremersee.ldaptive.LdaptiveEntryImmutableMapper;
import org.bremersee.samba.ad.dc.newmodel.AdEntry.Builder;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapEntry;

public class AdEntryMapper extends LdaptiveEntryImmutableMapper<AdEntry> {

  public AdEntryMapper() {
    super();
  }

  @Override
  public String[] getObjectClasses() {
    return new String[0];
  }

  @Override
  public String mapDn(AdEntry domainObject) {
    return "";
  }
  @Override
  public AdEntry map(LdapEntry source) {
    Builder builder = AdEntry.builder();
    builder.created(OffsetDateTime.now());
    return builder.build();
  }

  @Override
  public AttributeModification[] mapAndComputeModifications(AdEntry source, LdapEntry destination) {
    return new AttributeModification[0];
  }

}
