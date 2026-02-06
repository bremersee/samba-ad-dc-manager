package org.bremersee.samba.ad.dc.controller.api;

import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.service.DnsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/dns")
public class DnsApiController extends ApiController {

  private final DnsService dnsService;

  public DnsApiController(DnsService dnsService) {
    this.dnsService = dnsService;
  }

  @GetMapping(path = "/{zone}")
  public ResponseEntity<DnsZone> getDnsZone(@PathVariable("zone") String zoneName) {
    return ResponseEntity.ok(dnsService.getDnsZone(zoneName));
  }

}
