package org.bremersee.samba.ad.dc.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("api")
@OpenAPIDefinition(
    info = @Info(
        title = "Samba AD DC Manager API",
        description = "RESTful service for Samba AD DC Manager.",
        version = "v1"
    )
)
class OpenApiConfiguration {

  OpenApiConfiguration() {
    super();
  }

  @Configuration
  @Profile("!jwt & api")
  @SecurityScheme(
      name = "basic-auth",
      scheme = "basic",
      type = SecuritySchemeType.HTTP,
      in = SecuritySchemeIn.HEADER)
  static class ForBasicAuth {

    ForBasicAuth() {
      super();
    }
  }

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
  @SecurityScheme(
      name = "basic-auth",
      scheme = "basic",
      type = SecuritySchemeType.HTTP,
      in = SecuritySchemeIn.HEADER)
  static class ForJwt {

    ForJwt() {
      super();
    }
  }

}
