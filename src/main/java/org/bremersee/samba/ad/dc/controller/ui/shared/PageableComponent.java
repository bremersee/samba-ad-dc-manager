/*
 * Copyright 2024 the original author or authors.
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

package org.bremersee.samba.ad.dc.controller.ui.shared;

import java.util.Optional;
import org.bremersee.comparator.model.SortOrder;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.comparator.spring.web.SortOrderRequestParam;
import org.bremersee.samba.ad.dc.controller.AbstractController;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The pageable component.
 *
 * @author Christian Bremer
 */
public interface PageableComponent {

  @ModelAttribute(AbstractController.PAGE)
  default int addPage(
      @RequestParam(name = AbstractController.PAGE, defaultValue = AbstractController.PAGE_DEFAULT)
      int page) {
    return page;
  }

  @ModelAttribute(AbstractController.SIZE)
  default int addSize(
      @RequestParam(name = AbstractController.SIZE, defaultValue = AbstractController.SIZE_DEFAULT)
      int size) {
    return size;
  }

  SortMapper getSortMapper();

  String getDefaultSort();

  @ModelAttribute(AbstractController.SORT)
  default String addSort(@SortOrderRequestParam SortOrder sort) {
    return Optional.ofNullable(sort)
        .filter(s -> !s.isUnsorted())
        .map(sortOrder -> getSortMapper().getSortOrderText(sortOrder))
        .filter(sortOrderText -> !sortOrderText.isBlank())
        .orElse(getDefaultSort());
  }

  @ModelAttribute(AbstractController.QUERY)
  default String addQuery(
      @RequestParam(name = AbstractController.QUERY,
          defaultValue = AbstractController.QUERY_DEFAULT)
      String q) {
    return q;
  }

}
