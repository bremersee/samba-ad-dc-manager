package org.bremersee.samba.ad.dc.model;

import lombok.Getter;
import org.springframework.core.Ordered;

public enum AdEntryType {

  AD_ENTRY(Ordered.LOWEST_PRECEDENCE),

  DNS_ENTRY(300),

  DNS_ZONE(200),

  SAM_ACCOUNT(30),

  COMPUTER(50),

  GROUP(20),

  GROUP_MEMBER(100),

  USER(40),

  ORGANIZATIONAL_UNIT(10);

  @Getter
  private final int sortOrder;

  AdEntryType(int sortOrder) {
    this.sortOrder = sortOrder;
  }

}
