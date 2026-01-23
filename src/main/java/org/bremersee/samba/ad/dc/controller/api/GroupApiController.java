package org.bremersee.samba.ad.dc.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainGroupPage;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.service.DomainGroupService;
import org.ldaptive.dn.Dn;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/groups")
public class GroupApiController extends ApiController {

  private final DomainGroupService domainGroupService;

  public GroupApiController(
      DomainGroupService domainGroupService) {
    this.domainGroupService = domainGroupService;
  }

  @PageableAsQueryParam
  @Operation(
      description = "Get group page.",
      security = {@SecurityRequirement(name = "bearer-jwt"),
          @SecurityRequirement(name = "basicAuth")}
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK."),
          @ApiResponse(responseCode = "401", description = "Unauthorized."),
          @ApiResponse(responseCode = "403", description = "Forbidden.")
      }
  )
  @GetMapping
  public ResponseEntity<DomainGroupPage> getGroups(

      @Parameter(hidden = true)
      @PageableDefault(size = SIZE_DEFAULT_INT, sort = GROUP_SORT) Pageable pageable,

      @Parameter(name = QUERY, description = "A search term.")
      @RequestParam(name = QUERY, required = false)
      String query,

      @Parameter(name = OU,
          description = "The search base (organizational unit) like 'CN=Users'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = OU, required = false)
      Dn ou,

      @Parameter(name = SCOPE, description = "The search scope (one-level|subtree).",
          schema = @Schema(type = "string"))
      @RequestParam(name = SCOPE, required = false)
      TreeSearchScope scope) {

    Page<DomainGroup> groupPage = domainGroupService
        .getGroups(pageable, query, ou, scope);
    return ResponseEntity.ok(new DomainGroupPage(groupPage));
  }

  @Operation(
      description = "Get group.",
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
  public ResponseEntity<DomainGroup> getGroup(
      @Parameter(name = "name", description = "The name of the group.")
      @PathVariable("name") String samAccountName,

      @Parameter(name = OU,
          description = "The search base (organizational unit) like 'CN=Groups'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = OU, required = false)
      Dn ou,

      @Parameter(name = SCOPE, description = "The search scope (one-level|subtree).",
          schema = @Schema(type = "string"))
      @RequestParam(name = SCOPE, required = false)
      TreeSearchScope scope) {
    return ResponseEntity.of(domainGroupService.getGroup(samAccountName, ou, scope));
  }
}
