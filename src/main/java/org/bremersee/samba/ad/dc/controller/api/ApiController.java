package org.bremersee.samba.ad.dc.controller.api;

import lombok.AccessLevel;
import lombok.Getter;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.samba.ad.dc.controller.AbstractController;

public abstract class ApiController extends AbstractController {

  @Getter(AccessLevel.PROTECTED)
  private final SortMapper sortMapper;

  protected ApiController(SortMapper sortMapper) {
    this.sortMapper = sortMapper;
  }

}
