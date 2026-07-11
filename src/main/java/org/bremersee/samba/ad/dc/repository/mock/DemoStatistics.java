package org.bremersee.samba.ad.dc.repository.mock;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

@Getter
public class DemoStatistics implements Serializable {

  // + Profile detector for banner in UI

  @Serial
  private static final long serialVersionUID = 1L;

  private final OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);

  private AtomicInteger loginCounter = new AtomicInteger();

  private AtomicInteger entriesCounter = new AtomicInteger();

}
