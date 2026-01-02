/*
 * Copyright 2019-2026 the original author or authors.
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

package org.bremersee.samba.ad.dc.config;

import java.io.Serial;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * The application properties.
 *
 * @author Christian Bremer
 */
@ConfigurationProperties(prefix = "bremersee.dc")
@Component
@Getter
@Setter
@ToString(exclude = "cryptoSecret")
@EqualsAndHashCode
@Slf4j
public class ApplicationProperties implements Serializable {

  @Serial
  private static final long serialVersionUID = 3L;

  private String baseDn;

  private String companyName = "Samba AD DC Manager";

  private String companyUrl = "#";

  private String cryptoSecret = "change-it";

  @NestedConfigurationProperty
  private CliProperties cli = new CliProperties();

  @NestedConfigurationProperty
  private DomainProperties domain = new DomainProperties();

  @NestedConfigurationProperty
  private EmailProperties email = new EmailProperties();

  @NestedConfigurationProperty
  private DomainComputerProperties computer = new DomainComputerProperties();

  @NestedConfigurationProperty
  private DomainUserProperties user = new DomainUserProperties();

  @NestedConfigurationProperty
  private DomainGroupProperties group = new DomainGroupProperties();

  @NestedConfigurationProperty
  private MockProperties mock = new MockProperties();

}
