package org.bremersee.samba.ad.dc.controller.api;

import org.bremersee.samba.ad.dc.model.PasswordInformation;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/domain")
public class DomainApiController {

  private final DomainService domainService;

  public DomainApiController(DomainService domainService) {
    this.domainService = domainService;
  }

  @GetMapping(path = "/password-information")
  public ResponseEntity<PasswordInformation> getPasswordInformation() {
    return ResponseEntity.ok(domainService.getPasswordInformation());
  }
}
