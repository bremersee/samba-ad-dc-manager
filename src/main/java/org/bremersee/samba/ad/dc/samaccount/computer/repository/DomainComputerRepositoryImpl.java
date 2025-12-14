package org.bremersee.samba.ad.dc.samaccount.computer.repository;

import static java.util.Objects.isNull;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.common.DnTool;
import org.bremersee.samba.ad.dc.common.converter.TreeSearchScopeConverter;
import org.bremersee.samba.ad.dc.common.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.common.repository.AdConstants;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.samaccount.common.repository.SamAccountRepository;
import org.bremersee.samba.ad.dc.samaccount.computer.model.DomainComputer;
import org.bremersee.samba.ad.dc.samaccount.computer.repository.mapper.DomainComputerLdapMapper;
import org.bremersee.samba.ad.dc.samaccount.user.model.DomainUser;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.NameValue;
import org.ldaptive.dn.RDn;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.OrFilter;
import org.ldaptive.filter.SubstringFilter;
import org.springframework.stereotype.Component;

@Component("domainComputerRepository")
@Slf4j
public class DomainComputerRepositoryImpl extends SamAccountRepository
    implements DomainComputerRepository {

  private final LdaptiveEntryMapper<DomainComputer> domainComputerLdapMapper;

  private final SambaToolComputer domainComputerTool;

  DomainComputerRepositoryImpl(
      DomainControllerProperties properties,
      SambaToolComputer domainComputerTool,
      LdaptiveTemplate ldapTemplate) {
    super(properties, ldapTemplate);
    this.domainComputerLdapMapper = new DomainComputerLdapMapper();
    this.domainComputerTool = domainComputerTool;
  }

  @Override
  protected Dn getDefaultOu() {
    return new Dn(getProperties().getComputer().getDefaultOu());
  }

  @Override
  protected String getObjectClassValue() {
    return AdConstants.OBJECT_CLASS_COMPUTER;
  }

  @Override
  protected String[] getBinaryAttributes() {
    return domainComputerLdapMapper.getBinaryAttributeNames();
  }

  @Override
  protected String[] getReturnAttributes() {
    return domainComputerLdapMapper.getMappedAttributeNames();
  }

  Filter getFindAllFilter(String query) {
    //noinspection DuplicatedCode
    Filter objectClassFilter = objectClassFilter();
    if (isNull(query) || query.length() < getProperties().getComputer().getMinQueryLength()) {
      return objectClassFilter;
    }
    Filter orFilter = new OrFilter(
        new SubstringFilter(
            AdConstants.DESCRIPTION.getName(), null, null, query),
        new SubstringFilter(
            AdConstants.SAM_ACCOUNT_NAME.getName(), null, null, query),
        new SubstringFilter(
            AdConstants.NAME.getName(), null, null, query),
        new SubstringFilter(
            AdConstants.COMPUTER_NETWORK_ADDRESS.getName(), null, null, query),
        new SubstringFilter(
            AdConstants.COMPUTER_OPERATING_SYSTEM.getName(), null, null, query),
        new SubstringFilter(
            AdConstants.COMPUTER_OPERATING_SYSTEM_VERSION.getName(), null, null, query)
    );
    return new AndFilter(objectClassFilter, orFilter);
  }

  @Override
  public Stream<DomainComputer> findAll(String query, Dn ou, TreeSearchScope searchScope) {
    log.debug("findAll({}, {}, {})", query, ou, searchScope);
    SearchScope scope = TreeSearchScopeConverter.toSearchScope(searchScope);
    SearchRequest searchRequest = searchAllRequest(
        ou,
        getFindAllFilter(query),
        scope,
        getReturnAttributes());
    return getLdapTemplate()
        .findAll(searchRequest, domainComputerLdapMapper)
        .filter(getIgnoredObjectFilter(ou, scope));
  }

  @Override
  public Optional<DomainComputer> findOne(String name, Dn ou, TreeSearchScope searchScope) {
    log.debug("findOne({})", name);
    String samAccountName;
    if (!name.endsWith("$")) {
      samAccountName = name + "$";
    } else {
      samAccountName = name;
    }
    SearchScope scope = TreeSearchScopeConverter.toSearchScope(searchScope);
    SearchRequest searchRequest = searchOneRequest(samAccountName, ou, scope);
    log.debug("findOne, searchRequest = {}", searchRequest);
    return getLdapTemplate()
        .findOne(searchRequest, domainComputerLdapMapper)
        .filter(getIgnoredObjectFilter(ou, scope));
  }

  @Override
  public DomainComputer update(DomainComputer domainComputer, Dn newOu) {
    log.debug("update({}, {})", domainComputer.getSamAccountName(), newOu);
    DomainComputer existingDomainComputer = findOne(
        domainComputer.getSamAccountName(), null, null)
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            DomainComputer.class.getSimpleName(),
            domainComputer.getSamAccountName(),
            EC_SAM_ACCOUNT_NOT_FOUND));
    Dn oldDn = new Dn(existingDomainComputer.getDistinguishedName());
    Dn newDn = getNewDn(existingDomainComputer, domainComputer, newOu);
    if (!oldDn.isSame(newDn) && dnExistsWithAnyObjectClass(newDn.format())) {
      throw ServiceException.alreadyExistsWithErrorCode(
          DomainUser.class.getSimpleName(),
          getDnTool().removeBaseDn(newDn),
          EC_DN_ALREADY_EXISTS);
    }
    if (!oldDn.isSame(newDn)) {
      domainComputerTool.moveComputer(existingDomainComputer, newDn);
    }
    DomainComputer newComputer = DomainComputer.builder()
        .from(existingDomainComputer)
        .distinguishedName(newDn.format(DnTool.CASE_SENSITIVE_RDN_NORMALIZER))
        .description(domainComputer.getDescription())
        .build();
    return getLdapTemplate().save(newComputer, domainComputerLdapMapper);
  }

  @Override
  public boolean delete(String name) {
    log.debug("delete({})", name);
    return findOne(name, null, null)
        .map(domainComputer -> {
          domainComputerTool.deleteComputer(domainComputer);
          return findOne(name, null, null).isEmpty();
        })
        .orElse(false);
  }

  Dn getNewDn(DomainComputer oldComputer, DomainComputer newComputer, Dn newOu) {
    Dn oldDn = new Dn(oldComputer.getDistinguishedName());
    Dn newDn = new Dn(new RDn(new NameValue(
        oldDn.getRDn().getNameValue().getName(),
        newComputer.getSamAccountName())));
    Dn parentDn;
    if (DnTool.isValidDn(newOu)) {
      parentDn = getDnTool().addBaseDn(newOu);
    } else {
      parentDn = oldDn.getParent();
    }
    newDn.add(parentDn);
    return newDn;
  }

}
