package org.bremersee.samba.ad.dc.misc;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public interface TemplateEngineContextSupplier {

  @NotNull
  Map<String, Object> getTemplateEngineContext();

}
