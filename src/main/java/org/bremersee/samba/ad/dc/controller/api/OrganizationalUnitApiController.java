package org.bremersee.samba.ad.dc.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.OrganizationalUnit;
import org.bremersee.samba.ad.dc.model.OrganizationalUnitPage;
import org.bremersee.samba.ad.dc.service.OrganizationalUnitService;
import org.ldaptive.dn.Dn;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("api")
@RestController
@RequestMapping(path = "/api/organizational-units")
public class OrganizationalUnitApiController extends ApiController {

  private final OrganizationalUnitService service;

  public OrganizationalUnitApiController(OrganizationalUnitService service) {
    this.service = service;
  }

  @PageableAsQueryParam
  @Operation(
      description = "Get organizational units.",
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
  public ResponseEntity<OrganizationalUnitPage> getOrganizationalUnits(

      @Parameter(hidden = true)
      @PageableDefault(size = SIZE_DEFAULT_INT, sort = OU_SORT) Pageable pageable,

      @Parameter(name = QUERY, description = "A search term.")
      @RequestParam(name = QUERY, required = false)
      String query) {

    Page<OrganizationalUnit> page = service.getOrganizationalUnits(pageable, query);
    return ResponseEntity.ok(new OrganizationalUnitPage(page));
  }

  @Operation(
      description = "Add organizational unit.",
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
  @PutMapping(
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<OrganizationalUnit> addOrganizationalUnit(

      @RequestBody OrganizationalUnit ou,

      @Parameter(name = "parent-ou",
          description = "The distinguished name of the parent organizational unit.",
          schema = @Schema(type = "string", example = "CN=Contacts"))
      @RequestParam(name = "parent-ou", required = false) Dn parentOu) {

    return ResponseEntity.ok(service.add(ou, parentOu));
  }

  @Operation(
      description = "Get organizational unit.",
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
  public ResponseEntity<OrganizationalUnit> getOrganizationalUnit(

      @Parameter(name = "dn",
          description = "The distinguished name of the organizational unit.",
          required = true,
          schema = @Schema(type = "string", example = "CN=Users"))
      @PathVariable(name = "dn") Dn ou) {

    return ResponseEntity.of(service.getOrganizationalUnit(ou));
  }

  @Operation(
      description = "Delete organizational unit.",
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
  @DeleteMapping(path = "/{dn}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Boolean> deleteOrganizationalUnit(

      @Parameter(name = "dn",
          description = "The distinguished name of the organizational unit.",
          required = true,
          schema = @Schema(type = "string", example = "CN=Contacts"))
      @PathVariable(name = "dn") Dn ou) {

    return ResponseEntity.ok(service.delete(ou));
  }

  @Operation(
      description = "Update organizational unit.",
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
  @PutMapping(
      path = "/{dn}",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<OrganizationalUnit> updateOrganizationalUnit(

      @Parameter(name = "dn",
          description = "The distinguished name of the organizational unit.",
          required = true,
          schema = @Schema(type = "string", example = "CN=Contacts"))
      @PathVariable(name = "dn") Dn ou,

      @RequestBody OrganizationalUnit organizationUnit,

      @Parameter(name = "parent-ou",
          description = "The distinguished name of the new parent organizational unit.",
          schema = @Schema(type = "string", example = "CN=Telephone Book"))
      @RequestParam(name = "parent-ou", required = false) Dn parentOu) {

    OrganizationalUnit unit = organizationUnit
        .withDistinguishedName(DnTool.toString(ou));
    return ResponseEntity.ok(service.update(unit, parentOu));
  }

}
