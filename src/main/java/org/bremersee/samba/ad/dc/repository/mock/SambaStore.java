package org.bremersee.samba.ad.dc.repository.mock;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import org.bremersee.ldaptive.LdaptiveAttribute;
import org.bremersee.ldaptive.LdaptiveException;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.DefaultDnTool;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.model.Sid;
import org.bremersee.samba.ad.dc.repository.AdConstants;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Profile({"test", "mock"})
class SambaStore {

  static final String DOMAIN_SID = Sid.DEFAULT_SID_PREFIX + "1111111111-111111111-1111111111";

  @Getter(AccessLevel.PACKAGE)
  private final DnTool dnTool;

  private final AtomicInteger sidPostfix = new AtomicInteger(Sid.MAX_SYSTEM_SID_SUFFIX + 1);

  private DomainInfo domainInfo;

  private PasswordInformation passwordInformation;

  @Getter
  private final LdapNode root;

  SambaStore(ApplicationProperties properties) {
    this.dnTool = new DefaultDnTool(properties);
    this.root = new LdapNode(properties.getBaseDn());
    AdConstants.OBJECT_CLASS.setValues(root, List.of(
        "domain", "domainDNS", "top"
    ));
    AdConstants.IS_CRITICAL_SYSTEM_OBJECT.setValue(this.root, true);
    AdConstants.OBJECT_SID.setValue(this.root, Sid.builder()
        .value(DOMAIN_SID)
        .build());
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

  boolean isRfc2307Enabled() {
    return true;
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
    if (!dnTool.isValidDnWithBaseDn(dn)) {
      return Optional.empty();
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

  synchronized void add(LdapEntry entry) {
    Assert.notNull(entry, "Ldap entry must not be null.");
    Assert.isTrue(dnTool.isValidDnWithBaseDn(entry.getDn()), "Dn is invalid.");
    findByDn(new Dn(entry.getDn()).getParent().format()).ifPresentOrElse(
        parentNode -> new LdapNode(entry, parentNode),
        () -> {
          throw LdaptiveException.builder()
              .reason("Parent not found.")
              .errorCode(ErrorCode.EC_OU_NOT_FOUND)
              .build();
        });
  }

  synchronized void move(String dn, String newParentDn) {
    if (!DnTool.isValidDn(dn) || !DnTool.isValidDn(newParentDn)) {
      return;
    }
    Dn validatedDn = dnTool.addBaseDn(dn);
    Dn newValidatedParentDn = dnTool.addBaseDn(newParentDn);
    if (DnTool.isSameDn(newValidatedParentDn, validatedDn.getParent())) {
      return;
    }
    LdapNode newParent = findByDn(newValidatedParentDn.format())
        .orElseThrow(() -> LdaptiveException.builder()
            .reason("New parent not found.")
            .errorCode(ErrorCode.EC_OU_NOT_FOUND)
            .build());
    findByDn(validatedDn.format()).ifPresentOrElse(
        node -> {
          node.getParent().removeChild(node);
          newParent.addChild(node);
          Dn newDn = new Dn(validatedDn.getRDn());
          newDn.add(newValidatedParentDn);
          node.setDn(newDn.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER));
          AdConstants.DN.setValue(node, newDn);
          List<LdaptiveAttribute<Dn>> attrList = List.of(
              AdConstants.GROUP_MEMBER,
              AdConstants.MEMBER_OF_GROUP);
          for (LdaptiveAttribute<Dn> attr : attrList) {
            modifyAll(
                entry -> {
                  List<Dn> list = attr.getValues(entry)
                      .map(e -> {
                        if (DnTool.isSameDn(validatedDn, e)) {
                          return newDn;
                        }
                        return e;
                      })
                      .toList();
                  attr.setValues(entry, list);
                },
                attr::exists);
          }
        },
        () -> {
          throw LdaptiveException.builder()
              .reason("Entry not found.")
              .errorCode(ErrorCode.EC_ILLEGAL_DN)
              .build();
        });
  }

  synchronized void remove(String dn) {
    findByDn(dn).ifPresent(node -> {
      node.getParent().getChildren().remove(node);
      List<LdaptiveAttribute<Dn>> attrList = List.of(
          AdConstants.GROUP_MEMBER,
          AdConstants.MEMBER_OF_GROUP);
      for (LdaptiveAttribute<Dn> attr : attrList) {
        modifyAll(
            entry -> {
              List<Dn> list = attr.getValues(entry)
                  .filter(e -> !DnTool.isSameDn(e, dn))
                  .toList();
              attr.setValues(entry, list);
            },
            attr::exists);
      }
    });
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
