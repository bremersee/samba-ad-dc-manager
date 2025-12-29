package org.bremersee.samba.ad.dc.model.event;

import java.io.Serial;
import lombok.Getter;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.springframework.context.ApplicationEvent;

@Getter
public class PasswordResetEvent extends ApplicationEvent {

  @Serial
  private static final long serialVersionUID = 1L;

  private final String baseUri;

  public PasswordResetEvent(DomainUser source, String baseUri) {
    super(source);
    this.baseUri = baseUri;
  }

  @Override
  public DomainUser getSource() {
    return (DomainUser) super.getSource();
  }

}
