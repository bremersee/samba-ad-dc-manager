package org.bremersee.samba.ad.dc.ou.controller.ui.shared;

import java.util.List;
import org.bremersee.samba.ad.dc.ou.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.ou.service.OrganizationalUnitService;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface OrganisationalUnitsComponent {

  OrganizationalUnitService getOrganizationalUnitService();

  @ModelAttribute("ous")
  default List<OrganizationalUnit> addOrganisationalUnits() {
    return getOrganizationalUnitService().getOrganizationalUnitsWithSystemOus()
        .toList();
  }

}
