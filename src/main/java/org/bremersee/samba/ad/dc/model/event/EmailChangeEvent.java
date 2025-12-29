package org.bremersee.samba.ad.dc.model.event;

import java.io.Serial;
import lombok.Getter;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.springframework.context.ApplicationEvent;

@Getter
public class EmailChangeEvent extends ApplicationEvent {

  @Serial
  private static final long serialVersionUID = 1L;

  private final String baseUri;

  private final String newEmail;

  public EmailChangeEvent(DomainUser source, String newEmail, String baseUri) {
    super(source);
    this.newEmail = newEmail;
    this.baseUri = baseUri;
  }

  @Override
  public DomainUser getSource() {
    return (DomainUser) super.getSource();
  }

}
