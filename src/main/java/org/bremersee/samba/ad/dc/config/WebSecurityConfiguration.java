/*
 * Copyright 2025-2026  the original author or authors.
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

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.LinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.spring.boot.autoconfigure.security.authentication.AuthenticationProperties;
import org.bremersee.spring.security.ldaptive.authentication.LdaptiveRememberMeServices;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.RequestMatcherDelegatingAccessDeniedHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatchers;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * The web security configuration.
 *
 * @author Christian Bremer
 */
@EnableWebSecurity
@EnableConfigurationProperties({
    OAuth2ResourceServerProperties.class, AuthenticationProperties.class
})
@Configuration
@Slf4j
public class WebSecurityConfiguration {

  private static final String API_PATH_PATTERN = "/api/**";

  private final Environment env;

  private final AuthenticationProperties authenticationProperties;

  private final OAuth2ResourceServerProperties resourceServerProperties;

  private final CorsConfigurationSource corsConfigurationSource;

  private final Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter;

  private final LdaptiveRememberMeServices rememberMeServices;

  public WebSecurityConfiguration(
      Environment env,
      AuthenticationProperties authenticationProperties,
      OAuth2ResourceServerProperties resourceServerProperties,
      CorsConfigurationSource corsConfigurationSource,
      ObjectProvider<Converter<Jwt, AbstractAuthenticationToken>> jwtConverterProvider,
      ObjectProvider<LdaptiveRememberMeServices> rememberMeServices) {
    this.env = env;
    this.authenticationProperties = authenticationProperties;
    this.resourceServerProperties = resourceServerProperties;
    this.corsConfigurationSource = corsConfigurationSource;
    this.jwtAuthenticationConverter = jwtConverterProvider
        .getIfAvailable(JwtAuthenticationConverter::new);
    this.rememberMeServices = rememberMeServices.getIfAvailable();
  }

  private AccessDeniedHandler getAccessDeniedHandler() {
    AccessDeniedHandler accessDeniedHandler = new AccessDeniedHandlerImpl();
    LinkedHashMap<RequestMatcher, AccessDeniedHandler> handlers = LinkedHashMap
        .newLinkedHashMap(2);
    handlers.put(PathPatternRequestMatcher.withDefaults().matcher(API_PATH_PATTERN),
        accessDeniedHandler);
    handlers.put(EndpointRequest.toAnyEndpoint(), accessDeniedHandler);
    AccessDeniedHandlerImpl defaultHandler = new AccessDeniedHandlerImpl();
    defaultHandler.setErrorPage("/forbidden");
    return new RequestMatcherDelegatingAccessDeniedHandler(handlers, defaultHandler);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) {
    var e = System.getenv();
    for (var entry : e.entrySet()) {
      log.info("=======> Environment variable: {}={}", entry.getKey(), entry.getValue());
    }


    String appName = env.getProperty("spring.application.name", "samba-ad-dc-manager");
    String[] actuatorReadRoles = authenticationProperties.getActuator().getReadRoles()
        .toArray(new String[0]);
    String[] actuatorWriteRoles = authenticationProperties.getActuator().getWriteRoles()
        .toArray(new String[0]);
    http
        .exceptionHandling(customizer -> customizer
            .accessDeniedHandler(getAccessDeniedHandler()))

        .authorizeHttpRequests(customizer -> customizer
            .requestMatchers(HttpMethod.OPTIONS, "/**")
            .permitAll()

            .requestMatchers(
                EndpointRequest.to(InfoEndpoint.class),
                EndpointRequest.to(HealthEndpoint.class))
            .permitAll()

            .requestMatchers(new AndRequestMatcher(
                EndpointRequest.toAnyEndpoint(),
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/**")))
            .hasAnyAuthority(actuatorReadRoles)

            .requestMatchers(EndpointRequest.toAnyEndpoint())
            .hasAnyAuthority(actuatorWriteRoles)

            .requestMatchers(PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.GET, "/api/users/*/avatar"))
            .permitAll()

            .requestMatchers(API_PATH_PATTERN, "/management/**")
            .hasAuthority("ROLE_ADMIN")

            .requestMatchers("/user/**")
            .hasAuthority("ROLE_LOCAL_USER")

            /*
            .requestMatchers("/css/**").permitAll()
            .requestMatchers("/fonts/**").permitAll()
            .requestMatchers("/lib/**").permitAll()

            .requestMatchers("/v3/**").permitAll()
            .requestMatchers("/webjars/**").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/swagger-ui.html").permitAll()

            .requestMatchers("/login").permitAll()
            .requestMatchers("/logged-out").permitAll()

            .requestMatchers("/").permitAll()
            .requestMatchers("/index.html").permitAll()
            .requestMatchers("/passwd").permitAll()
            .requestMatchers("/passwd/**").permitAll()
             */

            .anyRequest().permitAll())

        .csrf(customizer -> customizer
            .ignoringRequestMatchers(RequestMatchers.anyOf(
                EndpointRequest.toAnyEndpoint(),
                PathPatternRequestMatcher.withDefaults().matcher(API_PATH_PATTERN))))

        .cors(customizer -> customizer
            .configurationSource(corsConfigurationSource))

        .headers(configurer -> configurer
            .frameOptions(FrameOptionsConfig::sameOrigin))

        .sessionManagement(customizer -> customizer
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

        .httpBasic(customizer -> customizer.realmName(appName))

        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/user/profile"))

        .logout(logout -> logout.logoutSuccessUrl("/index"));

    if (!isEmpty(rememberMeServices)) {
      http.rememberMe(customizer -> customizer
          .userDetailsService(rememberMeServices.getUserDetailsService())
          .rememberMeServices(rememberMeServices));
    }

    if (!isEmpty(resourceServerProperties.getJwt().getJwkSetUri())) {
      http
          .oauth2ResourceServer(configurer -> configurer
              .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
              .jwt(jwtConfigurer -> jwtConfigurer

                  .jwtAuthenticationConverter(jwtAuthenticationConverter)
                  .jwkSetUri(resourceServerProperties.getJwt().getJwkSetUri())));
    }

    return http.build();
  }

}
