package org.bremersee.samba.ad.dc.controller.ui.components;

import java.util.List;
import org.bremersee.samba.ad.dc.ou.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface OrganisationalUnitsComponent {

  OrganizationalUnitService getOrganizationalUnitService();

  @ModelAttribute("ous")
  default List<OrganizationalUnit> addOrganisationalUnits() {
    return getOrganizationalUnitService().getOrganizationalUnitsWithSystemOus()
        .toList();
  }

}
