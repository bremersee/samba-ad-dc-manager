package org.bremersee.samba.ad.dc.controller.ui.shared;

import java.util.List;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * The organizational units component.
 */
public interface OrganizationalUnitsComponent {

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
  default List<OrganizationalUnit> addOrganizationalUnits() {
    return getOrganizationalUnitService().getOrganizationalUnitsWithSystemOus()
        .toList();
  }

}
