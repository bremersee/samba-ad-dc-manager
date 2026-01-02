package org.bremersee.samba.ad.dc.repository.mock;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveException;
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
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.NameValue;
import org.ldaptive.dn.RDn;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Profile("mock")
@Slf4j
class SambaStore {

  static final String DOMAIN_SID = Sid.DEFAULT_SID_PREFIX + "1111111111-111111111-1111111111";

  @Getter(AccessLevel.PACKAGE)
  private final DnTool dnTool;

  private final AtomicInteger sidPostfix = new AtomicInteger(Sid.MAX_SYSTEM_SID_SUFFIX + 1);

  private DomainInfo domainInfo;

  private PasswordInformation passwordInformation;

  private final LdapNode root;

  private final Map<DnsZone, List<DnsEntry>> dns;

  SambaStore(ApplicationProperties properties) {
    this.dnTool = new DefaultDnTool(properties);
    this.root = new LdapNode(properties.getBaseDn());
    this.dns = new HashMap<>();
    AdConstants.OBJECT_CLASS.setValues(root, List.of(
        "domain", "domainDNS", "top"
    ));
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(this.root, true);
    AdConstants.OBJECT_SID.setValue(this.root, Sid.builder()
        .value(DOMAIN_SID)
        .build());
    log.info("Base entry {}.", root.getDn());
    new SambaStoreInit(this).init();
  }

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

  PasswordInformation getPasswordInformation() {
    if (isEmpty(passwordInformation)) {
      passwordInformation = PasswordInformation.builder().build();
    }
    return passwordInformation;
  }

  Sid getNextSid() {
    return getSid(sidPostfix.getAndIncrement());
  }

  Sid getSid(int sidPostfix) {
    return Sid.builder()
        .value(DOMAIN_SID + '-' + sidPostfix)
        .build();
  }

  synchronized Optional<LdapNode> findByDn(String dn) {
    log.info("find by dn: {}", dn);
    if (!dnTool.isValidDnWithBaseDn(dn)) {
      return Optional.empty();
    }
    if (DnTool.isSameDn(dnTool.addBaseDn(AdConstants.YELLOW_PAGES), dn)) {
      log.info("ypservers requested");
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

  synchronized List<LdapEntry> find(SearchRequest request) {
    log.info("find request: {}", request);
    List<LdapEntry> response = new ArrayList<>();
    if (isEmpty(request) || isEmpty(request.getBaseDn())) {
      return response;
    }
    Optional<LdapNode> foundNode = findByDn(request.getBaseDn());
    if (foundNode.isEmpty()) {
      return response;
    }
    LdapNode node = foundNode.get();
    log.info("request filter: {}", request.getFilter());
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

  synchronized void add(LdapEntry entry) {
    Assert.notNull(entry, "Ldap entry must not be null.");
    log.info("Adding entry: {}", entry.getDn());
    Assert.isTrue(dnTool.isValidDnWithBaseDn(entry.getDn()), "Dn is invalid.");
    findByDn(new Dn(entry.getDn()).getParent().format()).ifPresentOrElse(
        parentNode -> new LdapNode(entry, parentNode),
        () -> {
          throw ServiceException.notFoundWithErrorCode(
              OrganizationalUnit.class.getSimpleName(),
              new Dn(entry.getDn()).getParent().format(),
              ErrorCode.EC_OU_NOT_FOUND);
        });
  }

  private Optional<Dn> move(Dn dn, Dn newParentDn) {
    if (!DnTool.isValidDn(dn)) {
      return Optional.empty();
    }
    if (!DnTool.isValidDn(newParentDn)) {
      return Optional.of(dn);
    }
    Dn validatedDn = dnTool.addBaseDn(dn);
    Dn newValidatedParentDn = dnTool.addBaseDn(newParentDn);
    if (DnTool.isSameDn(newValidatedParentDn, validatedDn.getParent())) {
      return Optional.of(validatedDn);
    }
    LdapNode newParent = findByDn(newValidatedParentDn.format())
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            OrganizationalUnit.class.getSimpleName(),
            newValidatedParentDn.format(),
            ErrorCode.EC_OU_NOT_FOUND));
    return findByDn(validatedDn.format())
        .map(node -> {
          node.getParent().removeChild(node);
          newParent.addChild(node);
          Dn newDn = new Dn(validatedDn.getRDn());
          newDn.add(newValidatedParentDn);
          node.setDn(newDn.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER));
          AdConstants.DN.setValue(node, newDn);
          return newDn;
        });
  }

  synchronized void moveEntry(Dn dn, Dn newParentDn) {
    Dn validatedDn = dnTool.addBaseDn(dn);
    move(dn, newParentDn)
        .ifPresent(newDn -> onEntryDnChange(validatedDn, newDn));
  }

  synchronized Dn moveOrganizationalUnit(Dn ouDn, Dn newParentOu) {
    return move(ouDn, newParentOu)
        .map(newDn -> {
          onOrganizationalUnitDnChange(ouDn, newDn);
          return newDn;
        })
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            OrganizationalUnit.class.getSimpleName(),
            Optional.ofNullable(ouDn).map(Dn::format).orElse("null"),
            ErrorCode.EC_OU_NOT_FOUND));
  }

  private Optional<Dn> rename(Dn dn, String newName) {
    return findByDn(dn.format())
        .map(entry -> {
          log.debug("Renaming dn [{}] to [{}]", dn.format(), newName);
          String rdnType = dn.getRDn().getNameValue().getName();
          RDn rdn = new RDn(new NameValue(rdnType, newName));
          Dn newDn = Dn.builder().add(rdn).add(dn.getParent()).build();
          log.debug("Renaming dn [{}] to [{}]", dn.format(), newDn.format());
          entry.setDn(newDn.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER));
          AdConstants.DN.setValue(entry, newDn);
          return newDn;
        });
  }

  synchronized Dn renameEntry(Dn dn, String newName) {
    String dnStr = dn.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER);
    return rename(dn, newName)
        .map(newDn -> {
          onEntryDnChange(dn, newDn);
          return newDn;
        })
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            OrganizationalUnit.class.getSimpleName(), dnStr, ErrorCode.EC_OU_NOT_FOUND));
  }

  synchronized Dn renameOrganizationalUnit(Dn ouDn, String newName) {
    String dn = ouDn.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER);
    return rename(ouDn, newName)
        .flatMap(newDn -> findByDn(newDn.format()))
        .map(node -> {
          AdConstants.NAME.setValue(node, newName);
          return new Dn(node.getDn());
        })
        .map(newDn -> {
          onOrganizationalUnitDnChange(ouDn, newDn);
          return newDn;
        })
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            OrganizationalUnit.class.getSimpleName(), dn, ErrorCode.EC_OU_NOT_FOUND));
  }

  synchronized void remove(String dn) {
    findByDn(dn).ifPresent(node -> {
      node.getParent().getChildren().remove(node);
      onEntryDnChange(new Dn(dn), null);
    });
  }

  private void onEntryDnChange(Dn oldDn, @Nullable Dn newDn) {
    List<LdaptiveAttribute<Dn>> attrList = List.of(
        AdConstants.GROUP_MEMBER,
        AdConstants.MEMBER_OF_GROUP);
    for (LdaptiveAttribute<Dn> attr : attrList) {
      modifyAll(
          entry -> {
            List<Dn> list = attr.getValues(entry)
                .map(dn -> DnTool.replaceAncestor(dn, oldDn, newDn))
                .filter(dn -> !isEmpty(dn) && !dn.isEmpty())
                .toList();
            attr.setValues(entry, list);
          },
          attr::exists);
    }
  }

  private void onOrganizationalUnitDnChange(Dn oldDn, Dn newDn) {
    log.debug("on ou dn change [{}] to [{}]", DnTool.toString(oldDn), DnTool.toString(newDn));
    modifyAll(
        entry -> onOrganizationalUnitDnChange(entry, oldDn, newDn),
        entry -> true);
    List<LdaptiveAttribute<Dn>> attrList = List.of(
        AdConstants.GROUP_MEMBER,
        AdConstants.MEMBER_OF_GROUP);
    for (LdaptiveAttribute<Dn> attr : attrList) {
      modifyAll(
          entry -> onOrganizationalUnitDnChange(entry, attr, oldDn, newDn),
          attr::exists);
    }
  }

  private void onOrganizationalUnitDnChange(LdapEntry entry, Dn oldDn, Dn newDn) {
    log.debug("on ou dn change [{}] to [{}]: checking entry [{}]",
        DnTool.toString(oldDn), DnTool.toString(newDn), entry.getDn());
    Dn entryDn = new Dn(entry.getDn());
    Dn newEntryDn = DnTool.replaceAncestor(entryDn, oldDn, newDn);
    log.debug("on ou dn change old [{}] -> new [{}]", entry.getDn(), DnTool.toString(newEntryDn));
    entry.setDn(newEntryDn.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER));
    AdConstants.DN.setValue(entry, newEntryDn);
  }

  private void onOrganizationalUnitDnChange(
      LdapEntry entry, LdaptiveAttribute<Dn> attr, Dn oldDn, Dn newDn) {
    List<Dn> newValues = attr.getValues(entry)
        .map(dn -> DnTool.replaceAncestor(dn, oldDn, newDn))
        .toList();
    attr.setValues(entry, newValues);
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

}
