package org.bremersee.samba.ad.dc.controller.ui.shared;

import java.util.Optional;
import org.bremersee.samba.ad.dc.controller.AbstractController;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.ldaptive.dn.Dn;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

public interface OrganizationalUnitComponent extends LoggerProvider {

  @ModelAttribute(AbstractController.OU)
  default String addOrganisationalUnit(
      @RequestParam(name = AbstractController.OU, required = false) Dn ou) {
    String currentOu = Optional.ofNullable(ou).map(Dn::format).orElse("");
    getLogger().debug("Adding 'ou={}' to model.", currentOu);
    return currentOu;
  }

  @ModelAttribute(AbstractController.SCOPE)
  default String addSearchScope(
      @RequestParam(name = AbstractController.SCOPE, required = false) TreeSearchScope scope) {
    String searchScope = Optional.ofNullable(scope)
        .map(TreeSearchScope::getParameterValue)
        .orElse("");
    getLogger().debug("Adding 'scope={}' to model.", searchScope);
    return searchScope;
  }

}
