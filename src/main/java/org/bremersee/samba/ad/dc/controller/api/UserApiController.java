package org.bremersee.samba.ad.dc.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Optional;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.AvatarDefault;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.service.DomainUserService;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.NameValue;
import org.ldaptive.dn.RDn;
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
public class UserApiController extends ApiController {

  private final DomainUserService domainUserService;

  public UserApiController(
      SortMapper sortMapper,
      DomainUserService domainUserService) {
    super(sortMapper);
    this.domainUserService = domainUserService;
  }

  @Operation(
      description = "Get user.",
      security = {@SecurityRequirement(name = "bearer-jwt"),
          @SecurityRequirement(name = "basicAuth")}
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK."),
          @ApiResponse(responseCode = "401", description = "Unauthorized."),
          @ApiResponse(responseCode = "403", description = "Forbidden."),
          @ApiResponse(responseCode = "404", description = "Not found.")
      }
  )
  @GetMapping(path = "/{name}")
  public ResponseEntity<DomainUser> getUser(
      @Parameter(name = "name", description = "The name of the user.")
      @PathVariable("name") String name,

      @Parameter(name = OU,
          description = "The search base (organizational unit) like 'CN=Users'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = OU, required = false)
      Dn ou,

      @Parameter(name = SCOPE, description = "The search scope (one-level|subtree).",
          schema = @Schema(type = "string"))
      @RequestParam(name = SCOPE, required = false)
      TreeSearchScope scope) {

    return ResponseEntity.of(domainUserService.getUser(name, ou, scope));
  }

  @Operation(description = "Get user avatar.")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK."),
          @ApiResponse(responseCode = "404", description = "Not found.")
      }
  )
  @GetMapping(
      value = "/{name}/avatar",
      produces = {MediaType.IMAGE_JPEG_VALUE})
  public ResponseEntity<byte[]> getUserAvatar(
      @PathVariable String name,
      @RequestParam(name = "d", defaultValue = "NOT_FOUND") AvatarDefault avatarDefault,
      @RequestParam(name = "s", defaultValue = "80") Integer size) {

    String filename = Optional.of(name)
        .filter(DnTool::isValidDn)
        .map(Dn::new)
        .map(Dn::getRDn)
        .map(RDn::getNameValue)
        .map(NameValue::getStringValue)
        .orElse(name);
    return domainUserService.getUserAvatar(name, null, null, avatarDefault, size)
        .map(avatar -> ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + ".jpg\"")
            .body(avatar))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

}
