package org.bremersee.samba.ad.dc.controller.ui.components;

import java.util.Optional;
import org.bremersee.samba.ad.dc.common.controller.ui.UiControllerConstants;
import org.bremersee.samba.ad.dc.common.controller.ui.shared.logger.LoggerProvider;
import org.bremersee.samba.ad.dc.common.model.TreeSearchScope;
import org.ldaptive.dn.Dn;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

public interface OrganizationalUnitComponent extends UiControllerConstants, LoggerProvider {

  @ModelAttribute(OU)
  default String addOrganisationalUnit(@RequestParam(name = OU, required = false) Dn ou) {
    String currentOu = Optional.ofNullable(ou).map(Dn::format).orElse("");
    getLogger().debug("Adding 'ou={}' to model.", currentOu);
    return currentOu;
  }

  @ModelAttribute(SCOPE)
  default String addSearchScope(
      @RequestParam(name = SCOPE, required = false) TreeSearchScope scope) {
    String searchScope = Optional.ofNullable(scope)
        .map(TreeSearchScope::getParameterValue)
        .orElse("");
    getLogger().debug("Adding 'scope={}' to model.", searchScope);
    return searchScope;
  }

}
