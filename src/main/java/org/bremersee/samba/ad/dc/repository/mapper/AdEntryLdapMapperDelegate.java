package org.bremersee.samba.ad.dc.repository.mapper;

import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.ldaptive.LdapEntry;

public interface AdEntryLdapMapperDelegate<T extends AdEntry> extends LdaptiveEntryMapper<T> {

  boolean canMap(LdapEntry ldapEntry);

}
