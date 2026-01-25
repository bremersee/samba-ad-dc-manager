package org.bremersee.samba.ad.dc.controller.ui.shared;

import java.util.Optional;
import org.bremersee.samba.ad.dc.controller.AbstractController;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.ldaptive.dn.Dn;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The organizational unit component.
 */
public interface OrganizationalUnitComponent extends LoggerProvider {

  /**
   * Add organizational unit to model.
   *
   * @param ou the organizational unit
   * @return the organizational unit
   */
  @ModelAttribute(AbstractController.OU)
  default String addOrganizationalUnit(
      @RequestParam(name = AbstractController.OU, required = false) Dn ou) {
    String currentOu = Optional.ofNullable(ou).map(Dn::format).orElse("");
    getLogger().debug("Adding 'ou={}' to model.", currentOu);
    return currentOu;
  }

  /**
   * Add search scope to model.
   *
   * @param scope the search scope
   * @return the search scope
   */
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
