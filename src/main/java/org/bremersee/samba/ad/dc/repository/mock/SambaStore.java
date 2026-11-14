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

package org.bremersee.samba.ad.dc.repository.mock;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveException;
import org.bremersee.ldaptive.serializable.SerLdapEntry;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.DefaultDnTool;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.model.Sid;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.jspecify.annotations.Nullable;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import tools.jackson.databind.json.JsonMapper;

/**
 * The samba store.
 *
 * @author Christian Bremer
 */
@Component
@Profile("mock")
@Slf4j
class SambaStore {

  /**
   * The Domain sid.
   */
  static final String DOMAIN_SID = Sid.DEFAULT_SID_PREFIX + "1111111111-111111111-1111111111";

  private final AtomicInteger sidPostfix = new AtomicInteger(Sid.MAX_SYSTEM_SID_SUFFIX + 1);

  private final JsonMapper jsonMapper;

  @Getter(AccessLevel.PACKAGE)
  private final DnTool dnTool;

  @Getter(AccessLevel.PACKAGE)
  private final LdapEntryFactory entryFactory;

  private DomainInfo domainInfo;

  private PasswordInformation passwordInformation;

  private final LdapNode root;

  @Getter(AccessLevel.PACKAGE)
  private final Map<DnsZone, List<DnsEntry>> dns;

  /**
   * Instantiates a new samba store.
   *
   * @param properties the properties
   * @param jsonMapperBuilder the json mapper builder
   */
  SambaStore(ApplicationProperties properties, JsonMapper.Builder jsonMapperBuilder) {
    this.jsonMapper = jsonMapperBuilder.build();
    this.dnTool = new DefaultDnTool(properties);
    this.root = new LdapNode(properties.getBaseDn());
    this.dns = new ConcurrentHashMap<>();
    AdConstants.OBJECT_CLASS.setValues(root, List.of(
        "domain", "domainDNS", "top"
    ));
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(this.root, true);
    AdConstants.OBJECT_SID.setValue(this.root, Sid.builder()
        .value(DOMAIN_SID)
        .build());
    this.entryFactory = new LdapEntryFactory(this);
    new SambaStoreInit(this).init();
  }

  /**
   * Gets domain info.
   *
   * @return the domain info
   */
  DomainInfo getDomainInfo() {
    if (isEmpty(domainInfo)) {
      domainInfo = DomainInfo.builder()
          .domain("samdom.example.org")
          .forest("samdom.example.org")
          .netbiosDomain("SAMDOM")
          .domainControllerName("dc.samdom.example.org")
          .domainControllerNetbiosName("DC")
          .serverSite("Default-First-Site-Name")
          .clientSite("Default-First-Site-Name")
          .build();
    }
    return domainInfo;
  }

  /**
   * Gets password information.
   *
   * @return the password information
   */
  PasswordInformation getPasswordInformation() {
    if (isEmpty(passwordInformation)) {
      passwordInformation = PasswordInformation.builder().build();
    }
    return passwordInformation;
  }

  /**
   * Gets next sid.
   *
   * @return the next sid
   */
  Sid getNextSid() {
    return getSid(sidPostfix.getAndIncrement());
  }

  /**
   * Gets sid.
   *
   * @param sidPostfix the sid postfix
   * @return the sid
   */
  Sid getSid(int sidPostfix) {
    return Sid.builder()
        .value(DOMAIN_SID + '-' + sidPostfix)
        .build();
  }

  /**
   * Find by dn optional.
   *
   * @param dn the dn
   * @return the optional
   */
  Optional<LdapNode> findByDn(Dn dn) {
    return findByDn(DnTool.toString(dn));
  }

  /**
   * Find by dn optional.
   *
   * @param dn the dn
   * @return the optional
   */
  Optional<LdapNode> findByDn(String dn) {
    if (!dnTool.isValidDnWithBaseDn(dn)) {
      return Optional.empty();
    }
    if (DnTool.isSameDn(dnTool.addBaseDn(AdConstants.YELLOW_PAGES), dn)) {
      LdapNode ypServers = new LdapNode(dn);
      AdConstants.OBJECT_CLASS.setValues(ypServers, List.of("container", "top"));
      return Optional.of(ypServers);
    }
    return findByDn(root, dn);
  }

  private Optional<LdapNode> findByDn(LdapNode node, String dn) {
    if (DnTool.isSameDn(node.getDn(), dn)) {
      return Optional.of(node);
    }
    for (LdapNode child : node.getChildren()) {
      Optional<LdapNode> found = findByDn(child, dn);
      if (found.isPresent()) {
        return found;
      }
    }
    return Optional.empty();
  }

  /**
   * Find list.
   *
   * @param request the request
   * @return the list
   */
  List<LdapEntry> find(SearchRequest request) {
    List<LdapEntry> response = new ArrayList<>();
    if (isEmpty(request) || isEmpty(request.getBaseDn())) {
      return response;
    }
    Optional<LdapNode> foundNode = findByDn(request.getBaseDn());
    if (foundNode.isEmpty()) {
      return response;
    }
    LdapNode node = foundNode.get();
    if (node.matches(request.getFilter())) {
      response.add(node);
    }
    if (SearchScope.OBJECT.equals(request.getSearchScope())) {
      return response;
    }
    find(request, node.getChildren(), response);
    if (request.getSizeLimit() > 0 && response.size() > request.getSizeLimit()) {
      throw LdaptiveException.builder()
          .reason("Response size exceeds limit of " + request.getSizeLimit())
          .build();
    }
    return response;
  }

  private void find(SearchRequest request, List<LdapNode> children, List<LdapEntry> response) {
    for (LdapNode node : children) {
      if (node.matches(request.getFilter())) {
        response.add(node);
      }
      if (SearchScope.SUBTREE.equals(request.getSearchScope())) {
        find(request, node.getChildren(), response);
      }
    }
  }

  /**
   * Add.
   *
   * @param entry the entry
   */
  void add(LdapEntry entry) {
    Assert.notNull(entry, "Ldap entry must not be null.");
    log.info("Adding entry: {}", toJson(new SerLdapEntry(entry)));
    Assert.isTrue(dnTool.isValidDnWithBaseDn(entry.getDn()), "Dn is invalid.");
    findByDn(new Dn(entry.getDn()).getParent()).ifPresentOrElse(
        parentNode -> new LdapNode(entry, parentNode),
        () -> {
          throw ServiceException.notFoundWithErrorCode(
              OrganizationalUnit.class.getSimpleName(),
              new Dn(entry.getDn()).getParent().format(),
              ErrorCode.EC_OU_NOT_FOUND);
        });
  }

  /**
   * Remove.
   *
   * @param dn the dn
   */
  void remove(String dn) {
    findByDn(dn).ifPresent(node -> {
      node.getParent().getChildren().remove(node);
      adjustDns(new Dn(dn), null);
    });
  }

  /**
   * Modify dn.
   *
   * @param request the request
   */
  void modifyDn(ModifyDnRequest request) {
    findByDn(request.getOldDn()).ifPresent(entry -> {
      Dn oldDn = new Dn(entry.getDn());
      Dn newDn = new Dn(request.getNewRDn());
      if (isEmpty(request.getNewSuperiorDn())) {
        newDn.add(oldDn.getParent());
      } else {
        newDn.add(new Dn(request.getNewSuperiorDn()));
      }
      if (!oldDn.getParent().isSame(newDn.getParent())) {
        entry.getParent().removeChild(entry);
        findByDn(newDn).ifPresent(newParent -> newParent.addChild(entry));
      }
      AdConstants.NAME.setValue(entry, newDn.getRDn().getNameValue().getStringValue());
      adjustDns(oldDn, newDn);
    });
  }

  private void adjustDns(Dn oldDn, @Nullable Dn newDn) {
    modifyAll(
        entry -> {
          Dn adjustedDn = renameDn(oldDn, newDn, new Dn(entry.getDn()));
          entry.setDn(DnTool.toString(adjustedDn));
          AdConstants.DN.setValue(entry, adjustedDn);
        },
        entry -> true);
    List<LdaptiveAttribute<Dn>> attrList = List.of(
        AdConstants.GROUP_MEMBER,
        AdConstants.MEMBER_OF_GROUP);
    for (LdaptiveAttribute<Dn> attr : attrList) {
      modifyAll(
          entry -> {
            List<Dn> list = attr.getValues(entry)
                .map(dn -> renameDn(oldDn, newDn, dn))
                .filter(dn -> !isEmpty(dn) && !dn.isEmpty())
                .toList();
            attr.setValues(entry, list);
          },
          attr::exists);
    }
  }

  private Dn renameDn(Dn oldDn, @Nullable Dn newDn, Dn targetDn) {
    if (oldDn.isSame(targetDn)) {
      return newDn;
    }
    if (oldDn.isAncestor(targetDn)) {
      return DnTool.replaceAncestor(targetDn, oldDn, newDn);
    }
    return targetDn;
  }

  private void modifyAll(Consumer<LdapEntry> modification, Predicate<LdapEntry> condition) {
    if (condition.test(root)) {
      modification.accept(root);
    }
    modifyAll(root.getChildren(), modification, condition);
  }

  private void modifyAll(
      List<LdapNode> children,
      Consumer<LdapEntry> modification,
      Predicate<LdapEntry> condition) {
    for (LdapNode child : children) {
      if (condition.test(child)) {
        modification.accept(child);
      }
      modifyAll(child.getChildren(), modification, condition);
    }
  }

  private String toJson(Object value) {
    if (value == null) {
      return null;
    }
    return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
  }
}
