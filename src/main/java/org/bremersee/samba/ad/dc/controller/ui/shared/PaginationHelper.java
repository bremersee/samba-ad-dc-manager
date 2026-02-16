/*
 * Copyright 2025-2026 the original author or authors.
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

import org.bremersee.pagebuilder.model.AbstractPageDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * The pagination helper.
 *
 * @author Christian Bremer
 */
@Component("paginationHelper")
public class PaginationHelper {

  private static final String PAGE_MUST_NOT_BE_NULL = "Page must not be null.";

  private int ceil(Number a, Number b) {
    return (int) Math.ceil(a.doubleValue() / b.doubleValue());
  }

  /**
   * Gets total pages.
   *
   * @param page the page
   * @return the total pages
   */
  public int getTotalPages(AbstractPageDto<?> page) {
    Assert.notNull(page, PAGE_MUST_NOT_BE_NULL);
    return ceil(page.getTotalElements(), page.getSize());
  }

  /**
   * Gets total pages.
   *
   * @param page the page
   * @return the total pages
   */
  public int getTotalPages(Page<?> page) {
    Assert.notNull(page, PAGE_MUST_NOT_BE_NULL);
    return ceil(page.getTotalElements(), page.getSize());
  }

  /**
   * Determines whether the given page number is valid or not.
   *
   * @param page the page
   * @param number the page number
   * @return {@code true} if the page number is valid, otherwise {@code false}
   */
  public boolean isValidPageNumber(AbstractPageDto<?> page, int number) {
    Assert.notNull(page, PAGE_MUST_NOT_BE_NULL);
    int totalPages = getTotalPages(page);
    return 0 <= number && number < totalPages;
  }

  /**
   * Determines whether the given page number is valid or not.
   *
   * @param page the page
   * @param number the page number
   * @return {@code true} if the page number is valid, otherwise {@code false}
   */
  public boolean isValidPageNumber(Page<?> page, int number) {
    Assert.notNull(page, PAGE_MUST_NOT_BE_NULL);
    int totalPages = getTotalPages(page);
    return 0 <= number && number < totalPages;
  }

}
