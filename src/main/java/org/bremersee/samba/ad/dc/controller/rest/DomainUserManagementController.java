package org.bremersee.samba.ad.dc.controller.rest;

import org.bremersee.samba.ad.dc.samaccount.user.model.AvatarDefault;
import org.bremersee.samba.ad.dc.samaccount.user.service.DomainUserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/users")
public class DomainUserManagementController {

  private final DomainUserService domainUserService;

  public DomainUserManagementController(DomainUserService domainUserService) {
    this.domainUserService = domainUserService;
  }

  @GetMapping(
      value = "/{userName}/avatar",
      produces = {MediaType.IMAGE_JPEG_VALUE})
  public ResponseEntity<byte[]> getUserAvatar(
      @PathVariable("userName") String userName,
      @RequestParam(name = "d", defaultValue = "NOT_FOUND") AvatarDefault avatarDefault,
      @RequestParam(name = "s", defaultValue = "80") Integer size) {

    return domainUserService.getUserAvatar(userName, null, null, avatarDefault, size)
        .map(avatar -> ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + userName + ".jpg\"")
            .body(avatar))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

}
