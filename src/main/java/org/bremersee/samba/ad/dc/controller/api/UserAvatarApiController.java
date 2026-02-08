package org.bremersee.samba.ad.dc.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Optional;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.AvatarDefault;
import org.bremersee.samba.ad.dc.service.DomainUserService;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.NameValue;
import org.ldaptive.dn.RDn;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("!api")
@RestController
public class UserAvatarApiController extends ApiController {

  protected final DomainUserService domainUserService;

  public UserAvatarApiController(DomainUserService domainUserService) {
    this.domainUserService = domainUserService;
  }

  @Operation(description = "Get user avatar.")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK."),
          @ApiResponse(responseCode = "404", description = "Not found.")
      }
  )
  @GetMapping(
      value = "/api/users/{name}/avatar",
      produces = {MediaType.IMAGE_JPEG_VALUE})
  public ResponseEntity<byte[]> getUserAvatar(
      @PathVariable(name = "name") String samAccountName,
      @RequestParam(name = "d", defaultValue = "NOT_FOUND") AvatarDefault avatarDefault,
      @RequestParam(name = "s", defaultValue = "80") Integer size) {

    String filename = Optional.of(samAccountName)
        .filter(DnTool::isValidDn)
        .map(Dn::new)
        .map(Dn::getRDn)
        .map(RDn::getNameValue)
        .map(NameValue::getStringValue)
        .orElse(samAccountName);
    return domainUserService.getUserAvatar(samAccountName, null, null, avatarDefault, size)
        .map(avatar -> ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + ".jpg\"")
            .body(avatar))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

}
