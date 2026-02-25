package org.bremersee.samba.ad.dc.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.bremersee.exception.ServiceException;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.samba.ad.dc.misc.DnTool;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.model.DomainComputerPage;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.service.DomainComputerService;
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
@RequestMapping(path = "/api/computers")
public class ComputerApiController extends ApiController {

  private final DomainComputerService domainComputerService;

  public ComputerApiController(
      DomainComputerService domainComputerService) {
    this.domainComputerService = domainComputerService;
  }

  @PageableAsQueryParam
  @Operation(
      description = "Get computer page.",
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
  public ResponseEntity<DomainComputerPage> getComputers(

      @Parameter(hidden = true)
      @PageableDefault(size = SIZE_DEFAULT_INT, sort = COMPUTER_SORT) Pageable pageable,

      @Parameter(name = QUERY, description = "A search term.")
      @RequestParam(name = QUERY, required = false)
      String query,

      @Parameter(name = OU,
          description = "The search base (organizational unit) like 'CN=Computers'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = OU, required = false)
      Dn ou,

      @Parameter(name = SCOPE, description = "The search scope (one-level|subtree).",
          schema = @Schema(implementation = TreeSearchScope.class, example = "one-level"))
      @RequestParam(name = SCOPE, required = false)
      TreeSearchScope scope) {

    Page<DomainComputer> computerPage = domainComputerService
        .getComputers(pageable, query, ou, scope);
    return ResponseEntity.ok(new DomainComputerPage(computerPage));
  }

  @Operation(
      description = "Get computer.",
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
  @GetMapping(path = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DomainComputer> getComputer(

      @Parameter(name = "name", description = "The name of the computer.", required = true)
      @PathVariable("name") String samAccountName,

      @Parameter(name = OU,
          description = "The search base (organizational unit) like 'CN=Computers'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = OU, required = false)
      Dn ou,

      @Parameter(name = SCOPE, description = "The search scope (one-level|subtree).",
          schema = @Schema(implementation = TreeSearchScope.class, example = "one-level"))
      @RequestParam(name = SCOPE, required = false)
      TreeSearchScope scope) {

    return ResponseEntity.of(domainComputerService.getComputer(samAccountName, ou, scope));
  }

  @Operation(
      description = "Update computer.",
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
          @ApiResponse(responseCode = "409", description = "Already exists", content = {
              @Content(schema = @Schema(implementation = RestApiException.class))
          }),
          @ApiResponse(responseCode = "500", description = "Internal server error", content = {
              @Content(schema = @Schema(implementation = RestApiException.class))
          })
      }
  )
  @PutMapping(
      path = "/{name}",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DomainComputer> updateComputer(

      @Parameter(name = "name", description = "The name of the computer.", required = true)
      @PathVariable("name") String samAccountName,

      @Parameter(name = "move-to",
          description = "The new organizational unit of the computer like 'CN=Servers'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = "move-to", required = false)
      Dn newOu,

      @RequestBody DomainComputer computer) {

    if (!DnTool.isValidDn(samAccountName)
        && !samAccountName.equalsIgnoreCase(computer.getSamAccountName())) {
      throw ServiceException.badRequest("Renaming of a computer is not supported.");
    }
    return ResponseEntity.ok(domainComputerService.updateComputer(computer, newOu));
  }

  @Operation(
      description = "Delete computer.",
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
  @DeleteMapping(path = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Boolean> deleteComputer(

      @Parameter(name = "name", description = "The name of the computer.", required = true)
      @PathVariable("name") String samAccountName) {

    return ResponseEntity.ok(domainComputerService.deleteComputer(samAccountName));
  }

}
