package org.bremersee.samba.ad.dc.repository.tools.cli;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;
import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.repository.tools.DomainTool;
import org.bremersee.samba.ad.dc.repository.tools.cli.parser.PasswordInformationParser;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DomainToolExecutor extends SambaToolExecutor implements DomainTool {

  private final PasswordInformationParser passwordInformationParser;

  public DomainToolExecutor(DomainControllerProperties properties) {
    super(properties);
    this.passwordInformationParser = PasswordInformationParser.defaultParser();
  }

  @Override
  String getSubCommand() {
    return "domain";
  }

  @Override
  public PasswordInformation getPasswordInformation() {
    log.debug("getPasswordInformation()");
    List<String> commands = getCommands();
    commands.add("passwordsettings");
    commands.add("show");
    PasswordInformation raw = executeAndGet(
        commands,
        passwordInformationParser);
    int minLength = raw.getMinimumPasswordLength();
    int maxLength = Math.max(getProperties().getMaximumPasswordLength(), minLength);
    return PasswordInformation.builder().from(raw)
        .maximumPasswordLength(maxLength)
        .simplePasswordRegexTemplate(getProperties().getSimplePasswordRegexTemplate())
        .complexPasswordRegexTemplate(getProperties().getComplexPasswordRegexTemplate())
        .build();
  }

}
