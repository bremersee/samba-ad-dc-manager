package org.bremersee.samba.ad.dc.service;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.config.DomainUserProperties.DefaultLoginPage;
import org.bremersee.samba.ad.dc.misc.TemplateEngineContextSupplier;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.spring.security.core.NormalizedPrincipal;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * The abstract email service.
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractEmailService {

  private final ApplicationProperties properties;

  private final MessageSource messageSource;

  private final TemplateEngine templateEngine;

  private final List<TemplateEngineContextSupplier> contextSuppliers;

  private final JavaMailSender javaMailSender;

  /**
   * Instantiates a new abstract email service.
   *
   * @param properties the properties
   * @param messageSource the message source
   * @param templateEngine the template engine
   * @param contextSuppliers the context suppliers
   * @param javaMailSender the java mail sender
   */
  protected AbstractEmailService(ApplicationProperties properties, MessageSource messageSource,
      TemplateEngine templateEngine, List<TemplateEngineContextSupplier> contextSuppliers,
      JavaMailSender javaMailSender) {
    this.properties = properties;
    this.messageSource = messageSource;
    this.templateEngine = templateEngine;
    this.contextSuppliers = contextSuppliers;
    this.javaMailSender = javaMailSender;
  }

  /**
   * Create context.
   *
   * @param user the user
   * @param baseUri the base uri
   * @return the context
   */
  protected Context createContext(DomainUser user, String baseUri) {
    Context ctx = new Context(user.getLocale());
    contextSuppliers.forEach(contextSupplier -> contextSupplier
        .getTemplateEngineContext().forEach(ctx::setVariable));
    ctx.setVariable("user", user);
    ctx.setVariable("properties", properties);
    ctx.setVariable("baseUri", isEmpty(baseUri) ? "" : baseUri);
    // only for invitation
    ctx.setVariable("regards", getEmailRegards());
    ctx.setVariable("defaultLoginPage", getDefaultLoginPage(baseUri));
    return ctx;
  }

  /**
   * Gets default login page.
   *
   * @param baseUri the base uri
   * @return the default login page
   */
  protected DefaultLoginPage getDefaultLoginPage(String baseUri) {
    DefaultLoginPage loginPage = getProperties().getUser().getDefaultLoginPage();
    if (!isEmpty(loginPage.getUrl()) && !"#".equals(loginPage.getUrl())) {
      return loginPage;
    }
    DefaultLoginPage fallback = new DefaultLoginPage();
    fallback.setUrl(isEmpty(baseUri) ? "#" : baseUri + "/login");
    fallback.setUsingNetbiosDomainPrefix(false);
    return fallback;
  }

  /**
   * Gets email regards.
   *
   * @return the email regards
   */
  protected String getEmailRegards() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .filter(Authentication::isAuthenticated)
        .map(Authentication::getPrincipal)
        .filter(Principal.class::isInstance)
        .map(Principal.class::cast)
        .map(this::getEmailRegards)
        .orElse("Your domain administrator");
  }

  private String getEmailRegards(Principal principal) {
    StringBuilder sb = new StringBuilder();
    if (principal instanceof NormalizedPrincipal normalizedPrincipal) {
      if (!isEmpty(normalizedPrincipal.getFirstName())) {
        sb.append(normalizedPrincipal.getFirstName());
        if (!isEmpty(normalizedPrincipal.getLastName())) {
          sb.append(" ");
        }
      }
      if (!isEmpty(normalizedPrincipal.getLastName())) {
        sb.append(normalizedPrincipal.getLastName());
      }
    }
    if (!sb.isEmpty()) {
      return sb.toString();
    }
    return principal.getName();
  }

}
