package org.bremersee.samba.ad.dc.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.exception.ServiceException;
import org.bremersee.ldaptive.LdaptiveOperations;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.Sid;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.repository.mapper.DomainGroupLdapMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.dn.Dn;

@ExtendWith(SoftAssertionsExtension.class)
class DomainGroupRepositoryImplTest {

  private LdaptiveOperations ldapOperations;

  private DomainRepository domainRepository;

  private DomainGroupLdapMapper domainGroupLdapMapper;

  private DomainGroupRepositoryImpl target;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    ApplicationProperties properties = new ApplicationProperties();
    properties.getGroup().setDefaultOu("CN=Users");
    properties.setBaseDn("dc=samdom,dc=example,dc=org");
    ldapOperations = mock(LdaptiveOperations.class);
    domainRepository = mock(DomainRepository.class);
    lenient()
        .doReturn(true)
        .when(domainRepository)
        .isRfc2307Enabled();
    domainGroupLdapMapper = new DomainGroupLdapMapper(domainRepository);
    target = spy(new DomainGroupRepositoryImpl(
        properties,
        ldapOperations,
        domainRepository,
        domainGroupLdapMapper));
  }

  @Test
  void getDefaultOu() {
    Dn expected = new Dn("CN=Users");
    Dn actual = target.getDefaultOu();
    assertThat(actual)
        .isEqualTo(expected);
  }

  @Test
  void getObjectClassValue() {
    assertThat(target.getObjectClassValue())
        .isEqualTo(AdConstants.OBJECT_CLASS_GROUP);
  }

  @Test
  void getBinaryAttributes() {
    assertThat(target.getBinaryAttributes())
        .containsExactlyInAnyOrder(domainGroupLdapMapper.getBinaryAttributeNames());
  }

  @Test
  void getReturnAttributes() {
    assertThat(target.getReturnAttributes())
        .containsExactlyInAnyOrder(domainGroupLdapMapper.getMappedAttributeNames());
  }

  @Test
  void findAll(SoftAssertions softly) {
    List<DomainGroup> expected = List.of(DomainGroup.builder()
        .distinguishedName("CN=group,CN=Users,DC=samdom,DC=example,DC=org")
        .samAccountName("group")
        .build());
    doAnswer(invocationOnMock -> expected.stream())
        .when(ldapOperations)
        .findAll(any(), any());
    List<DomainGroup> actual = target
        .findAll("group", new Dn("CN=Users"), TreeSearchScope.ONELEVEL).toList();
    softly
        .assertThat(actual)
        .containsExactlyInAnyOrderElementsOf(expected);
    actual = target.findAll("", null, null).toList();
    softly
        .assertThat(actual)
        .containsExactlyInAnyOrderElementsOf(expected);
  }

  @Test
  void findOne(SoftAssertions softly) {
    DomainGroup expected = DomainGroup.builder()
        .distinguishedName("CN=group,CN=Users,DC=samdom,DC=example,DC=org")
        .samAccountName("group")
        .build();
    doReturn(Optional.of(expected))
        .when(ldapOperations)
        .findOne(any(), any());
    Optional<DomainGroup> actual = target.findOne("group", null, null);
    softly
        .assertThat(actual)
        .hasValue(expected);
    target.findOne("group", new Dn("CN=Users"), TreeSearchScope.ONELEVEL);
    softly
        .assertThat(actual)
        .hasValue(expected);
  }

  @Test
  void findOneByPrimaryGroupId() {
    String domainSid = Sid.DEFAULT_SID_PREFIX + "1111111111-111111111-1111111111";
    doReturn(domainSid)
        .when(domainRepository)
        .getDomainSid();
    DomainGroup expected = DomainGroup.builder()
        .distinguishedName("CN=group,CN=Users,DC=samdom,DC=example,DC=org")
        .samAccountName("group")
        .sid(Sid.builder()
            .value(domainSid + "-1001")
            .build())
        .build();
    doReturn(Optional.of(expected))
        .when(ldapOperations)
        .findOne(any(), any());
    Optional<DomainGroup> actual = target.findOneByPrimaryGroupId(1001);
    assertThat(actual)
        .hasValue(expected);
    target.findOne("group", new Dn("CN=Users"), TreeSearchScope.ONELEVEL);
  }

  @Test
  void findOneByGidNumber() {
    DomainGroup expected = DomainGroup.builder()
        .distinguishedName("CN=group,CN=Users,DC=samdom,DC=example,DC=org")
        .samAccountName("group")
        .gidNumber(10001)
        .build();
    doReturn(Optional.of(expected))
        .when(ldapOperations)
        .findOne(any(), any());
    Optional<DomainGroup> actual = target.findOneByGidNumber(10001);
    assertThat(actual)
        .hasValue(expected);
  }

  @Test
  void existsByGidNumber() {
    LdapEntry ldapEntry = new LdapEntry();
    ldapEntry.setDn("CN=group,CN=Users,DC=samdom,DC=example,DC=org");
    AdConstants.DN.setValue(ldapEntry, new Dn("CN=group,CN=Users,DC=samdom,DC=example,DC=org"));
    AdConstants.GID_NUMBER.setValue(ldapEntry, 10001);
    doReturn(Optional.of(ldapEntry))
        .when(ldapOperations)
        .findOne(any());
    boolean actual = target.existsByGidNumber(10001);
    assertThat(actual)
        .isTrue();
  }

  @Test
  void add() {
    DomainGroup domainGroup = DomainGroup.builder()
        .samAccountName("group")
        .gidNumber(10001)
        .build();
    String newParentDn = "CN=Users,DC=samdom,DC=example,DC=org";
    doAnswer(
        invocationOnMock -> {
          SearchRequest searchRequest = invocationOnMock.getArgument(0);
          if (DnTool.isSameDn(newParentDn, searchRequest.getBaseDn())) {
            LdapEntry entry = new LdapEntry();
            entry.setDn(newParentDn);
            AdConstants.DN.setValue(entry, new Dn(newParentDn));
            return Optional.of(entry);
          }
          return Optional.empty();
        })
        .when(ldapOperations)
        .findOne(any());
    DomainGroup expected = DomainGroup.builder()
        .from(domainGroup)
        .build();
    doReturn(expected)
        .when(ldapOperations)
        .save(any(), any());
    DomainGroup actual = target.add(domainGroup, new Dn("CN=Users"));
    assertThat(actual)
        .isEqualTo(expected);
  }

  @Test
  void addWithInvalidParentDn() {
    DomainGroup domainGroup = DomainGroup.builder()
        .samAccountName("group")
        .gidNumber(10001)
        .build();
    doReturn(Optional.empty())
        .when(ldapOperations)
        .findOne(any());
    Dn ou = new Dn("CN=Users");
    assertThatExceptionOfType(ServiceException.class)
        .isThrownBy(() -> target.add(domainGroup, ou));
  }

  @Test
  void addWithExistingSamAccountName() {
    DomainGroup domainGroup = DomainGroup.builder()
        .samAccountName("group")
        .gidNumber(10001)
        .build();
    String newParentDn = "CN=Users,DC=samdom,DC=example,DC=org";
    LdapEntry entry = new LdapEntry();
    entry.setDn("CN=group" + newParentDn);
    AdConstants.DN.setValue(entry, new Dn("CN=group" + newParentDn));
    doReturn(Optional.of(entry))
        .when(ldapOperations)
        .findOne(any());
    Dn ou = new Dn("CN=Users");
    assertThatExceptionOfType(ServiceException.class)
        .isThrownBy(() -> target.add(domainGroup, ou));
  }

  @Test
  void addWithExistingGidNumber() {
    DomainGroup domainGroup = DomainGroup.builder()
        .samAccountName("group")
        .gidNumber(10001)
        .build();
    String newParentDn = "CN=Users,DC=samdom,DC=example,DC=org";
    doAnswer(
        invocationOnMock -> {
          SearchRequest searchRequest = invocationOnMock.getArgument(0);
          if (DnTool.isSameDn(newParentDn, searchRequest.getBaseDn())) {
            LdapEntry entry = new LdapEntry();
            entry.setDn(newParentDn);
            AdConstants.DN.setValue(entry, new Dn(newParentDn));
            return Optional.of(entry);
          }
          return Optional.empty();
        })
        .when(ldapOperations)
        .findOne(any());
    doReturn(true)
        .when(target)
        .existsByGidNumber(10001);
    Dn ou = new Dn("CN=Users");
    assertThatExceptionOfType(ServiceException.class)
        .isThrownBy(() -> target.add(domainGroup, ou));
  }

  @Test
  void update() {
    DomainGroup existing = DomainGroup.builder()
        .distinguishedName("CN=group,CN=Groups,DC=samdom,DC=example,DC=org")
        .samAccountName("group")
        .gidNumber(10001)
        .build();
    doReturn(Optional.of(existing))
        .when(target)
        .findOne("group", null, null);
    String newParentDn = "CN=Users,DC=samdom,DC=example,DC=org";
    doAnswer(
        invocationOnMock -> {
          SearchRequest searchRequest = invocationOnMock.getArgument(0);
          if (DnTool.isSameDn(newParentDn, searchRequest.getBaseDn())) {
            LdapEntry entry = new LdapEntry();
            entry.setDn(newParentDn);
            AdConstants.DN.setValue(entry, new Dn(newParentDn));
            return Optional.of(entry);
          }
          return Optional.empty();
        })
        .when(ldapOperations)
        .findOne(any());

    DomainGroup group = DomainGroup.builder()
        .from(existing)
        .distinguishedName("CN=group,CN=Groups,DC=samdom,DC=example,DC=org")
        .samAccountName("newgroup")
        .gidNumber(10002)
        .build();

    DomainGroup expected = DomainGroup.builder()
        .from(group)
        .distinguishedName("CN=group,CN=Users,DC=samdom,DC=example,DC=org")
        .build();
    doReturn(expected)
        .when(ldapOperations)
        .save(any(), any());

    Dn ou = new Dn("CN=Users");
    DomainGroup actual = target.update("group", group, ou);

    assertThat(actual)
        .isEqualTo(expected);
  }

  @Test
  void delete() {
    DomainGroup existing = DomainGroup.builder()
        .distinguishedName("CN=group,CN=Groups,DC=samdom,DC=example,DC=org")
        .samAccountName("group")
        .gidNumber(10001)
        .build();
    doReturn(Optional.of(existing))
        .when(target)
        .findOne("group", null, null);

    boolean actual = target.delete("group");
    assertThat(actual)
        .isTrue();
    verify(ldapOperations)
        .delete(any());
  }

  @Test
  void save() {
    DomainGroup expected = DomainGroup.builder()
        .distinguishedName("CN=group,CN=Groups,DC=samdom,DC=example,DC=org")
        .samAccountName("group")
        .gidNumber(10001)
        .build();
    doReturn(expected)
        .when(ldapOperations)
        .save(any(), any());
    DomainGroup actual = target.save(expected);
    assertThat(actual)
        .isEqualTo(expected);
  }
}