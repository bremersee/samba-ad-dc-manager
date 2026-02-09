package org.bremersee.samba.ad.dc.misc;

import org.bremersee.samba.ad.dc.model.DomainGroupMemberType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DomainGroupMemberTypeConverter implements Converter<String, DomainGroupMemberType> {

  @Override
  public @Nullable DomainGroupMemberType convert(@NonNull String source) {
    return DomainGroupMemberType.fromString(source);
  }
}
