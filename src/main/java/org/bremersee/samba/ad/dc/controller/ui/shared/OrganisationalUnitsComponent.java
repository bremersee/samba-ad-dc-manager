package org.bremersee.samba.ad.dc.controller.ui.shared;

import java.util.List;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * The rganizational units component.
 */
public interface OrganisationalUnitsComponent {

  /**
   * Gets organizational unit service.
   *
   * @return the organizational unit service
   */
  OrganizationalUnitService getOrganizationalUnitService();

  /**
   * Add organizational units to model.
   *
   * @return the list
   */
  @ModelAttribute("ous")
  default List<OrganizationalUnit> addOrganisationalUnits() {
    return getOrganizationalUnitService().getOrganizationalUnitsWithSystemOus()
        .toList();
  }

}
