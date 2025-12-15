/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.samba.ad.dc.common.controller.ui;

import static org.springframework.util.ObjectUtils.isEmpty;

import lombok.Getter;
import lombok.Setter;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.common.DefaultDnTool;
import org.bremersee.samba.ad.dc.common.DnTool;
import org.bremersee.samba.ad.dc.common.controller.DnToolProvider;
import org.bremersee.samba.ad.dc.common.controller.SortOrderConstants;
import org.bremersee.samba.ad.dc.common.controller.ui.shared.MessageProvider;
import org.bremersee.samba.ad.dc.common.controller.ui.shared.logger.LoggerProvider;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.springframework.context.MessageSource;
import org.springframework.util.Assert;
import org.springframework.web.servlet.LocaleResolver;

/**
 * The type UiController.
 *
 * @author Christian Bremer
 */
@Getter
public abstract class UiController implements DnToolProvider,
    LoggerProvider, MessageProvider, SortOrderConstants, ErrorCode {

  private final DomainControllerProperties properties;

  @Getter
  private final DnTool dnTool;

  @Getter
  private final LocaleResolver localeResolver;

  @Getter
  @Setter
  private MessageSource messageSource;

  protected UiController(
      DomainControllerProperties properties,
      LocaleResolver localeResolver) {
    this.properties = properties;
    this.dnTool = new DefaultDnTool(properties);
    this.localeResolver = localeResolver;
    Assert.notNull(getLogger(), "Logger is required.");
  }

  protected void logRedirectTo(String msg, String redirect) {
    if (isEmpty(msg)) {
      getLogger().debug("Redirecting to {}", redirect);
    } else {
      getLogger().debug("{} Redirecting to {}", msg, redirect);
    }
  }

}
