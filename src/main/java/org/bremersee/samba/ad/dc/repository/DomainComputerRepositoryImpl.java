package org.bremersee.samba.ad.dc.repository;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveEntryMapper;
import org.bremersee.ldaptive.LdaptiveTemplate;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.converter.TreeSearchScopeConverter;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.repository.mapper.DomainComputerLdapMapper;
import org.bremersee.samba.ad.dc.repository.tools.DomainComputerTool;
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
public class DomainComputerRepositoryImpl extends AbstractSamAccountRepository
    implements DomainComputerRepository {

  private final LdaptiveEntryMapper<DomainComputer> domainComputerLdapMapper;

  private final DomainComputerTool domainComputerTool;

  DomainComputerRepositoryImpl(
      DomainControllerProperties properties,
      DomainComputerTool domainComputerTool,
      LdaptiveTemplate ldapTemplate) {
    super(properties, ldapTemplate);
    this.domainComputerLdapMapper = new DomainComputerLdapMapper();
    this.domainComputerTool = domainComputerTool;
  }

  @Override
  Dn getDefaultOu() {
    return getProperties().getComputer().getDefaultOu();
  }

  @Override
  String getObjectClassValue() {
    return AdConstants.OBJECT_CLASS_COMPUTER;
  }

  @Override
  String[] getBinaryAttributes() {
    return domainComputerLdapMapper.getBinaryAttributeNames();
  }

  @Override
  String[] getReturnAttributes() {
    return domainComputerLdapMapper.getMappedAttributeNames();
  }

  private Filter getFindAllFilter(String query) {
    //noinspection DuplicatedCode
    Filter objectClassFilter = objectClassFilter();
    if (isNull(query) || query.length() <= 2) {
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
    log.debug("findAll, searchRequest = {}", searchRequest);
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
          getProperties().removeBaseDn(newDn),
          EC_DN_ALREADY_EXISTS);
    }
    if (!oldDn.isSame(newDn)) {
      return getLdapTemplate()
          .save(domainComputerTool.moveComputer(domainComputer, newOu), domainComputerLdapMapper);
    }
    return getLdapTemplate().save(domainComputer, domainComputerLdapMapper);
  }

  @Override
  public boolean delete(String name) {
    log.debug("delete({})", name);
    if (findOne(name, null, null).isEmpty()) {
      return false;
    }
    domainComputerTool.deleteComputer(name);
    return findOne(name, null, null).isEmpty();
  }

  Dn getNewDn(DomainComputer oldDomainComputer, DomainComputer newDomainComputer, Dn newOu) {
    if (isEmpty(newOu) || newOu.isEmpty()) {
      return new Dn(oldDomainComputer.getDistinguishedName());
    }
    String rdnName = new Dn(oldDomainComputer.getDistinguishedName())
        .getRDn().getNameValue().getName();
    String rdnValue = newDomainComputer.getName();
    Dn newDn = new Dn(new RDn(new NameValue(rdnName, rdnValue)));
    newDn.add(getProperties().getBaseDn(validateOu(newOu)));
    return newDn;
  }

}
