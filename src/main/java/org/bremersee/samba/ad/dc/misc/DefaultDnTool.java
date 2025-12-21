package org.bremersee.samba.ad.dc.misc;

import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.ldaptive.dn.Dn;
import org.springframework.stereotype.Component;

@Component
public class DefaultDnTool implements DnTool {

  private final DomainControllerProperties properties;

  public DefaultDnTool(DomainControllerProperties properties) {
    this.properties = properties;
  }

  @Override
  public Dn getBaseDn() {
    return new Dn(properties.getBaseDn());
  }

}
