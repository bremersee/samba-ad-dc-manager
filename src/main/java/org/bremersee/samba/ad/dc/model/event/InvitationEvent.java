package org.bremersee.samba.ad.dc.model.event;

import java.io.Serial;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.springframework.context.ApplicationEvent;

public class InvitationEvent extends ApplicationEvent {

  @Serial
  private static final long serialVersionUID = 1L;

  public InvitationEvent(DomainUser source) {
    super(source);
  }

  @Override
  public DomainUser getSource() {
    return (DomainUser) super.getSource();
  }

}
