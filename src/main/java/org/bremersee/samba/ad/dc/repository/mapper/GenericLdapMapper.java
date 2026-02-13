package org.bremersee.samba.ad.dc.repository.mapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import org.bremersee.samba.ad.dc.model.AdEntry;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.LdapEntry;
import org.springframework.stereotype.Component;

@Component
public class GenericLdapMapper {

  private final List<AdEntryLdapMapperDelegate<?>> delegates;

  @Getter
  private final String[] mappedAttributeNames;

  @Getter
  private final String[] binaryAttributeNames;

  public GenericLdapMapper(List<AdEntryLdapMapperDelegate<?>> delegates) {
    this.delegates = delegates;
    this.mappedAttributeNames = this.delegates.stream()
        .flatMap(delegate -> Stream.concat(
            Stream.of(AdConstants.OBJECT_CLASS.getName()),
            Arrays.stream(delegate.getMappedAttributeNames())))
        .distinct()
        .toArray(String[]::new);
    this.binaryAttributeNames = delegates.stream()
        .flatMap(delegate -> Arrays.stream(delegate.getBinaryAttributeNames()))
        .distinct()
        .toArray(String[]::new);
  }

  public Optional<AdEntry> map(LdapEntry ldapEntry) {
    return delegates.stream()
        .filter(delegate -> delegate.canMap(ldapEntry))
        .findFirst()
        .map(delegate -> delegate.map(ldapEntry));
  }

}
