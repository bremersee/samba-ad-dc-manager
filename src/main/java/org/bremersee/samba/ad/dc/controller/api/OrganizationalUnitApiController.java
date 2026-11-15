/*
 * Copyright 2025-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.samba.ad.dc.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The organizational unit api controller.
 */
@Profile("api")
@RestController
@RequestMapping(path = "/api/organizational-units")
@Tag(name = "ad-organizational-unit-manager", description = "RESTful api for organizational units.")
@Validated
public class OrganizationalUnitApiController extends ApiController {

  private final OrganizationalUnitService service;

  /**
   * Instantiates a new organizational unit api controller.
   *
   * @param service the service
   */
  public OrganizationalUnitApiController(OrganizationalUnitService service) {
    this.service = service;
  }

  /**
   * Gets organizational units.
   *
   * @param pageable the pageable
   * @param query the query
   * @return the organizational units
   */
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

  /**
   * Add organizational unit response entity.
   *
   * @param ou the ou
   * @param parentOu the parent ou
   * @return the response entity
   */
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

  /**
   * Gets organizational unit.
   *
   * @param ou the ou
   * @return the organizational unit
   */
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

  /**
   * Delete organizational unit response entity.
   *
   * @param ou the ou
   * @return the response entity
   */
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

  /**
   * Update organizational unit response entity.
   *
   * @param ou the ou
   * @param organizationUnit the organization unit
   * @param parentOu the parent ou
   * @return the response entity
   */
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
