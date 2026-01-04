package org.bremersee.samba.ad.dc.repository.mock;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.time.OffsetDateTime;
import java.util.Arrays;
import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.ldaptive.LdaptiveErrorHandler;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.AddRequest;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModification.Type;
import org.ldaptive.BindRequest;
import org.ldaptive.CompareRequest;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
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
@Profile("mock")
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
    synchronized (store) {
      store.findByDn(request.getDn())
          .ifPresent(node -> modify(node, request.getModifications()));
    }
  }

  private void modify(LdapEntry entry, AttributeModification[] modifications) {
    if (!isEmpty(modifications)) {
      for (AttributeModification modification : modifications) {
        modify(entry, modification);
      }
    }
  }

  private void modify(LdapEntry entry, AttributeModification modification) {
    LdapAttribute attr = modification.getAttribute();
    Type type = modification.getOperation();
    if (Type.ADD.equals(type) || Type.REPLACE.equals(type)) {
      addAttribute(entry, attr);
      if (AdConstants.USER_UNICODE_PWD.getName().equalsIgnoreCase(attr.getName())) {
        AdConstants.USER_PWD_LAST_SET.setValue(entry, OffsetDateTime.now());
      }
    } else if (Type.DELETE.equals(type)) {
      removeAttribute(entry, attr);
    }
  }

  private void removeAttribute(LdapEntry entry, LdapAttribute attr) {
    if (entry.hasAttribute(attr.getName())) {
      entry.removeAttribute(attr.getName());
    }
  }

  private void addAttribute(LdapEntry entry, LdapAttribute attr) {
    removeAttribute(entry, attr);
    entry.addAttributes(attr);
  }

  @Override
  public void modifyDn(ModifyDnRequest request) {
    store.modifyDn(request);
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
        .orElseGet(() -> {
          LdapEntry entry = new LdapEntry();
          entry.setDn(entryMapper.mapDn(domainObject));
          AdConstants.OBJECT_CLASS.setValues(entry, Arrays.asList(entryMapper.getObjectClasses()));
          entryMapper.map(domainObject, entry);
          store.getEntryFactory().setDefaultValues(entry);
          store.add(entry);
          return entryMapper.map(entry);
        });
  }
}
