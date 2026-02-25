package org.bremersee.samba.ad.dc.controller.api;

import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.controller.AbstractController;

public abstract class ApiController extends AbstractController {

  protected ApiController() {
    super();
  }

  static int getAsNumber(String value) {
    try {
      return Integer.parseInt(value);
    } catch (RuntimeException ignored) {
      throw ServiceException
          .badRequest(String.format("Value '%s' is not a number.", value));
    }
  }

}
