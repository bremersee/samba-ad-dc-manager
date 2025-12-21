package org.bremersee.samba.ad.dc.model;

public interface Translatable {

  String getI18nCode();

  String getDefaultTranslation(Object... args);

}
