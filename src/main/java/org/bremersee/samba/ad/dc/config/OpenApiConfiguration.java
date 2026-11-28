/*
 * Copyright 2026 the original author or authors.
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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * The OpenAPI configuration.
 *
 * @author Christian Bremer
 */
@Configuration
@Profile("api")
@OpenAPIDefinition(
    info = @Info(
        title = "Samba AD DC Manager API",
        summary = "The Samba AD DC Manager API",
        description = "RESTful service for Samba AD DC Manager.",
        version = "3.0.6-SNAPSHOT",
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    )
)
class OpenApiConfiguration {

  /**
   * Instantiates a new OpenAPI configuration.
   *
   * @author Christian Bremer
   */
  OpenApiConfiguration() {
    super();
  }

  /**
   * For jwt.
   *
   * @author Christian Bremer
   */
  @Configuration
  @Profile("jwt & api")
  @SecurityScheme(
      name = "bearer-jwt",
      type = SecuritySchemeType.OAUTH2,
      flows = @OAuthFlows(
          authorizationCode = @OAuthFlow(
              authorizationUrl = "${spring.security.oauth2.client.provider.swagger-ui.authorization-uri:}",
              tokenUrl = "${spring.security.oauth2.client.provider.swagger-ui.token-uri:}",
              scopes = {
                  @OAuthScope(name = "openid")
              }
          )
      )
  )
  static class ForJwt {

    /**
     * Instantiates a new instance for jwt.
     */
    ForJwt() {
      super();
    }
  }

}
