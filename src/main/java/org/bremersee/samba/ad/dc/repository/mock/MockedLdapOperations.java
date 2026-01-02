package org.bremersee.samba.ad.dc.repository.mock;

import java.time.OffsetDateTime;
import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.ldaptive.LdaptiveErrorHandler;
import org.bremersee.ldaptive.LdaptiveException;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AddRequest;
import org.ldaptive.BindRequest;
import org.ldaptive.CompareRequest;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteRequest;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyRequest;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.extended.ExtendedResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Primary
@Component
@Profile({"test", "mock"})
class MockedLdapOperations implements LdaptiveOperations {

  private final SambaStore store;

  MockedLdapOperations(SambaStore store) {
    this.store = store;
  }

  @Override
  public ConnectionFactory getConnectionFactory() {
    throw new UnsupportedOperationException("getConnectionFactory() is not implemented.");
  }

  @Override
  public LdaptiveOperations copy() {
    return copy(null);
  }

  @Override
  public LdaptiveOperations copy(LdaptiveErrorHandler errorHandler) {
    return this;
  }

  @Override
  public void add(AddRequest addRequest) {
    throw new UnsupportedOperationException("add(AddRequest) is not implemented.");
  }

  @Override
  public boolean bind(BindRequest request) {
    throw new UnsupportedOperationException("bind(BindRequest) is not implemented.");
  }

  @Override
  public boolean compare(CompareRequest request) {
    throw new UnsupportedOperationException("compare(CompareRequest) is not implemented.");
  }

  @Override
  public void delete(DeleteRequest request) {
    store.remove(request.getDn());
  }

  @Override
  public ExtendedResponse executeExtension(ExtendedRequest request) {
    throw new UnsupportedOperationException(
        "executeExtension(ExtendedRequest request) is not implemented.");
  }

  @Override
  public void modify(ModifyRequest request) {
    // nothing to do, only for passwords
  }

  @Override
  public void modifyDn(ModifyDnRequest request) {
    throw new UnsupportedOperationException("modifyDn(ModifyDnRequest) is not implemented.");
  }

  @Override
  public SearchResponse search(SearchRequest request) {
    return SearchResponse.builder()
        .entry(store.find(request))
        .build();
  }

  @Override
  public boolean exists(String dn) {
    return store.findByDn(dn).isPresent();
  }

  @Override
  public <T> T save(T domainObject, LdaptiveEntryMapper<T> entryMapper) {
    String dn = entryMapper.mapDn(domainObject);
    return store.findByDn(dn)
        .map(node -> {
          entryMapper.map(domainObject, node);
          AdConstants.WHEN_CHANGED.setValue(node, OffsetDateTime.now());
          return entryMapper.map(node);
        })
        .orElseThrow(() -> LdaptiveException.builder()
            .reason(String.format("No ldap entry found with dn = %s", dn))
            .httpStatus(404)
            .build());
  }
}
