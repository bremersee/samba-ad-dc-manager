package org.bremersee.samba.ad.dc.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.samba.ad.dc.model.AdEntryPage;
import org.bremersee.samba.ad.dc.model.ModelConstants;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.ldaptive.dn.Dn;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("api")
@RestController
@RequestMapping(path = "/api/active-directory")
public class AdEntryApiController extends ApiController {

  private final OrganizationalUnitService service;

  public AdEntryApiController(OrganizationalUnitService service) {
    this.service = service;
  }

  @Operation(
      description = "Get root (base dn) as organizational unit.",
      security = {
          @SecurityRequirement(name = "bearer-jwt"),
          @SecurityRequirement(name = "basic-auth")
      }
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK"),
          @ApiResponse(responseCode = "400", description = "Bad request", content = {
              @Content(schema = @Schema(implementation = RestApiException.class))
          }),
          @ApiResponse(responseCode = "401", description = "Unauthorized", content = {
              @Content(schema = @Schema(implementation = RestApiException.class))
          }),
          @ApiResponse(responseCode = "403", description = "Forbidden", content = {
              @Content(schema = @Schema(implementation = RestApiException.class))
          }),
          @ApiResponse(responseCode = "500", description = "Internal server error", content = {
              @Content(schema = @Schema(implementation = RestApiException.class))
          })
      }
  )
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<OrganizationalUnit> getRoot() {

    return ResponseEntity.ok(service.getBase());
  }

  @PageableAsQueryParam
  @Operation(
      description = "Get children of an organizational unit.",
      security = {
          @SecurityRequirement(name = "bearer-jwt"),
          @SecurityRequirement(name = "basic-auth")
      }
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK"),
          @ApiResponse(responseCode = "400", description = "Bad request", content = {
              @Content(schema = @Schema(implementation = RestApiException.class))
          }),
          @ApiResponse(responseCode = "401", description = "Unauthorized", content = {
              @Content(schema = @Schema(implementation = RestApiException.class))
          }),
          @ApiResponse(responseCode = "403", description = "Forbidden", content = {
              @Content(schema = @Schema(implementation = RestApiException.class))
          }),
          @ApiResponse(responseCode = "404", description = "Not found", content = {
              @Content(schema = @Schema(implementation = RestApiException.class))
          }),
          @ApiResponse(responseCode = "500", description = "Internal server error", content = {
              @Content(schema = @Schema(implementation = RestApiException.class))
          })
      }
  )
  @GetMapping(path = "/{dn}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AdEntryPage> getChildren(

      @Parameter(name = "dn",
          description = "The distinguished name of the organizational unit.",
          required = true,
          schema = @Schema(type = "string", example = "CN=Users"))
      @PathVariable(name = "dn") Dn ou,

      @Parameter(hidden = true)
      @PageableDefault(
          size = SIZE_DEFAULT_INT,
          sort = {ModelConstants.DISCRIMINATOR, DN_SORT})
      Pageable pageable) {

    return ResponseEntity.ok(new AdEntryPage(service.getChildren(ou, pageable)));
  }

}
