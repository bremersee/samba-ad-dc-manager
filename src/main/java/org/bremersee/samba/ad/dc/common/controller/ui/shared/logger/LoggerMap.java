package org.bremersee.samba.ad.dc.common.controller.ui.shared.logger;

import java.util.HashMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class LoggerMap extends HashMap<Class<?>, Logger> {

  static final LoggerMap INSTANCE = new LoggerMap();

}
