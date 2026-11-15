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
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import org.bremersee.exception.model.RestApiException;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The user avatar api controller.
 */
@Profile("!api")
@RestController
@RequestMapping(path = "/api/users")
@Tag(name = "ad-user-avatar-manager", description = "RESTful api for user avatars.")
@Validated
public class UserAvatarApiController extends ApiController {

  /**
   * The Domain user service.
   */
  protected final DomainUserService domainUserService;

  /**
   * Instantiates a new user avatar api controller.
   *
   * @param domainUserService the domain user service
   */
  public UserAvatarApiController(DomainUserService domainUserService) {
    this.domainUserService = domainUserService;
  }

  /**
   * Gets user avatar.
   *
   * @param samAccountName the sam account name
   * @param avatarDefault the avatar default
   * @param size the size
   * @return the user avatar
   */
  @Operation(description = "Get user avatar.")
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
  @GetMapping(
      value = "/{name}/avatar",
      produces = {MediaType.IMAGE_JPEG_VALUE})
  public ResponseEntity<byte[]> getUserAvatar(
      @Parameter(name = "name", description = "The name of the user.", required = true)
      @PathVariable(name = "name") String samAccountName,

      @Parameter(
          name = "d",
          description = "Specifies the default avatar.",
          schema = @Schema(
              implementation = AvatarDefault.class,
              defaultValue = "404",
              example = "mp"))
      @RequestParam(name = "d", defaultValue = "404") AvatarDefault avatarDefault,

      @Parameter(
          name = "s",
          description = "Specifies the size of the avatar.",
          schema = @Schema(
              type = "integer",
              format = "int32",
              defaultValue = "80",
              example = "120"))
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
