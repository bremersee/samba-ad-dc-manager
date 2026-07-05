package org.bremersee.samba.ad.dc.repository.mock;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.ldaptive.transcoder.UserAccountControl;
import org.bremersee.samba.ad.dc.config.MockProperties;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.bremersee.spring.security.ldaptive.authentication.LdaptiveAuthenticationToken;
import org.bremersee.spring.security.ldaptive.userdetails.LdaptiveUserDetails;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.EqualityFilter;
import org.ldaptive.filter.OrFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@Profile("mock")
@Slf4j
class MockedAuthenticationManager implements AuthenticationManager, AuthenticationProvider {

  private final SambaStore store;

  MockedAuthenticationManager(SambaStore store) {
    this.store = store;
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    if (!supports(authentication.getClass())) {
      return null;
    }
    String username = authentication.getName();
    String password = String.valueOf(authentication.getCredentials());
    SearchRequest request = SearchRequest.builder()
        .dn(store.getDnTool().getBaseDn().format())
        .filter(new AndFilter(
            new EqualityFilter(AdConstants.OBJECT_CLASS.getName(), AdConstants.OBJECT_CLASS_USER),
            new OrFilter(
                new EqualityFilter(AdConstants.SAM_ACCOUNT_NAME.getName(), username),
                new EqualityFilter(AdConstants.MAIL.getName(), username))))
        .sizeLimit(1)
        .build();
    return store.find(request).stream()
        .findFirst()
        .map(entry -> authenticate(entry, password))
        .orElseThrow(() -> new UsernameNotFoundException(String.format("Not found %s", username)));
  }

  private Authentication authenticate(LdapEntry entry, String password) {
    return AdConstants.USER_UNICODE_PWD.getValue(entry)
        .filter(pwd -> Objects.equals(pwd, password)
            || (isAdmin(entry) && MockProperties.ADMIN_FALLBACK_PASSWORD.equals(password)))
        .map(noop -> authenticated(entry))
        .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
  }

  private Authentication authenticated(LdapEntry entry) {
    return new LdaptiveAuthenticationToken(getUserDetails(entry));
  }

  private LdaptiveUserDetails getUserDetails(LdapEntry entry) {
    UserAccountControl control = AdConstants.USER_USER_ACCOUNT_CONTROL.getValue(entry)
        .orElseGet(UserAccountControl::new);
    AdConstants.USER_LAST_LOGON.setValue(entry, OffsetDateTime.now(ZoneOffset.UTC));
    AdConstants.USER_LOGON_COUNT.getValue(entry)
        .ifPresentOrElse(
            counter -> AdConstants.USER_LOGON_COUNT.setValue(entry, counter + 1),
            () -> AdConstants.USER_LOGON_COUNT.setValue(entry, 1));
    var userDetailsBuilder = LdaptiveUserDetails.builder()
        .dn(entry.getDn())
        .username(AdConstants.SAM_ACCOUNT_NAME.getValue(entry).orElse(""))
        .accountNonExpired(true)
        .accountNonLocked(true)
        .credentialsNonExpired(true)
        .enabled(control.isEnabled() || isAdmin(entry))
        .authorities(getAuthorities(entry));
    AdConstants.USER_GIVEN_NAME.getValue(entry)
        .ifPresent(userDetailsBuilder::firstName);
    AdConstants.USER_SN.getValue(entry)
        .ifPresent(userDetailsBuilder::lastName);
    AdConstants.MAIL.getValue(entry)
        .ifPresent(userDetailsBuilder::email);
    return userDetailsBuilder.build();
  }

  private List<GrantedAuthority> getAuthorities(LdapEntry entry) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_LOCAL_USER"));
    if (isAdmin(entry)) {
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
      authorities.add(new SimpleGrantedAuthority("ROLE_ACTUATOR_ADMIN"));
    }
    return authorities;
  }

  private static boolean isAdmin(LdapEntry entry) {
    return AdConstants.SAM_ACCOUNT_NAME.getValue(entry)
        .map(MockProperties.ADMIN_NAME::equalsIgnoreCase)
        .orElse(false);
  }
}
