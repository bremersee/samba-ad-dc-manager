package org.bremersee.samba.ad.dc.repository.cli;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.DomainInfo;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.repository.SambaToolDomain;
import org.bremersee.samba.ad.dc.repository.cli.parser.DomainInfoParser;
import org.bremersee.samba.ad.dc.repository.cli.parser.PasswordInformationParser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SambaToolDomainCli extends SambaToolCli implements SambaToolDomain {

  public SambaToolDomainCli(ApplicationProperties properties) {
    super(properties);
  }

  @Override
  protected String getSubCommand() {
    return "domain";
  }

  @Cacheable(value = "domainInfoCache", key = "#p0")
  @Override
  public DomainInfo getDomainInfo(String ipOrHostname) {
    List<String> commands = getCommands();
    commands.add("info");
    commands.add(quote(ipOrHostname));
    return executeAndGet(commands, DomainInfoParser.defaultParser())
        .orElseThrow(() -> ServiceException.notFoundWithErrorCode(
            DomainInfo.class.getSimpleName(),
            ipOrHostname,
            ErrorCode.EC_DOMAIN_INFO_NOT_FOUND));
  }

  @Cacheable(value = "passwordInformationCache")
  @Override
  public PasswordInformation getPasswordInformation() {
    List<String> commands = getCommands();
    commands.add("passwordsettings");
    commands.add("show");
    PasswordInformation raw = executeAndGet(
        commands,
        PasswordInformationParser.defaultParser());
    int minLength = raw.getMinimumPasswordLength();
    int maxLength = Math.max(getProperties().getDomain().getMaximumPasswordLength(), minLength);
    return PasswordInformation.builder().from(raw)
        .maximumPasswordLength(maxLength)
        .simplePasswordRegexTemplate(getProperties().getDomain().getSimplePasswordRegexTemplate())
        .complexPasswordRegexTemplate(getProperties().getDomain().getComplexPasswordRegexTemplate())
        .build();
  }

}
