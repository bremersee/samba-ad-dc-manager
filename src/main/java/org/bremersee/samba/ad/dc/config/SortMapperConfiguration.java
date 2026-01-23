package org.bremersee.samba.ad.dc.config;

import java.util.List;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.samba.ad.dc.misc.SortMapperAware;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SortMapperConfiguration {

  public SortMapperConfiguration(
      SortMapper sortMapper,
      List<SortMapperAware> sortMapperComponents) {

    sortMapperComponents.forEach(component -> component.setSortMapper(sortMapper));
  }

}
