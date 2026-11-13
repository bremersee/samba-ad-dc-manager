/*
 * Copyright 2025-2026  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.samba.ad.dc.config;

import java.util.List;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.samba.ad.dc.misc.SortMapperAware;
import org.springframework.context.annotation.Configuration;

/**
 * The sort mapper configuration.
 *
 * @author Christian Bremer
 */
@Configuration
public class SortMapperConfiguration {

  /**
   * Instantiates a new sort mapper configuration.
   *
   * @param sortMapper the sort mapper
   * @param sortMapperComponents the sort mapper components
   */
  public SortMapperConfiguration(
      SortMapper sortMapper,
      List<SortMapperAware> sortMapperComponents) {

    sortMapperComponents.forEach(component -> component.setSortMapper(sortMapper));
  }

}
