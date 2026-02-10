package org.bremersee.samba.ad.dc.controller.api;

import static org.springframework.util.ObjectUtils.isEmpty;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.io.IOException;
import java.io.InputStream;
import org.bremersee.exception.ServiceException;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.model.DomainUser;
import org.bremersee.samba.ad.dc.model.DomainUserPage;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.model.event.InvitationEvent;
import org.bremersee.samba.ad.dc.service.DomainService;
import org.bremersee.samba.ad.dc.service.DomainUserService;
import org.ldaptive.dn.Dn;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
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
@RequestMapping(path = "/api/users")
public class UserApiController extends UserAvatarApiController {

  private final ApplicationProperties properties;

  private final ApplicationEventPublisher eventPublisher;

  private final DomainService domainService;

  public UserApiController(
      ApplicationProperties properties,
      ApplicationEventPublisher eventPublisher,
      DomainService domainService,
      DomainUserService domainUserService) {
    super(domainUserService);
    this.properties = properties;
    this.eventPublisher = eventPublisher;
    this.domainService = domainService;
  }

  @PageableAsQueryParam
  @Operation(
      description = "Get user page.",
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
  public ResponseEntity<DomainUserPage> getUsers(

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

    Page<DomainUser> userPage = domainUserService
        .getUsers(pageable, query, ou, scope);
    return ResponseEntity.ok(new DomainUserPage(userPage));
  }

  @Operation(
      description = "Add user.",
      security = {
          @SecurityRequirement(name = "bearer-jwt"),
          @SecurityRequirement(name = "basic-auth")}
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
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DomainUser> addUser(
      @RequestBody DomainUser user,

      @Parameter(name = "pwd", description = "The password of the user.")
      @RequestParam(name = "pwd", required = false)
      String clearPassword,

      @Parameter(name = OU,
          description = "Add user to organizational unit (like 'CN=Users').",
          schema = @Schema(type = "string"))
      @RequestParam(name = OU, required = false)
      Dn ou,

      @Parameter(
          name = "username-as-cn",
          description = "Specifies whether the username should be used as common name or not.")
      @RequestParam(name = "username-as-cn", required = false)
      Boolean useUsernameAsCn,

      @Parameter(
          name = "invitation-email",
          description = "Specifies whether an invitation email should be sent or not.",
          schema = @Schema(type = "boolean", defaultValue = "false"))
      @RequestParam(name = "invitation-email", defaultValue = "false")
      boolean sendInvitationEmail) {

    if (sendInvitationEmail && isEmpty(user.getEmail())) {
      throw ServiceException
          .badRequest("User has no email address. Sending invitation email is not possible.");
    }
    DomainUser addedUser = domainUserService.addUser(user, clearPassword, ou, useUsernameAsCn);
    if (sendInvitationEmail) {
      String baseUri = getBaseUri(properties.getEmail().getBaseUri());
      InvitationEvent event = new InvitationEvent(addedUser, baseUri);
      eventPublisher.publishEvent(event);
    }
    return ResponseEntity.ok(addedUser);
  }

  @Operation(
      description = "Get user.",
      security = {
          @SecurityRequirement(name = "bearer-jwt"),
          @SecurityRequirement(name = "basic-auth")}
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
  public ResponseEntity<DomainUser> getUser(
      @Parameter(name = "name", description = "The name of the user.", required = true)
      @PathVariable("name") String samAccountName,

      @Parameter(name = OU,
          description = "The search base (organizational unit) like 'CN=Users'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = OU, required = false)
      Dn ou,

      @Parameter(name = SCOPE, description = "The search scope (one-level|subtree).",
          schema = @Schema(type = "string"))
      @RequestParam(name = SCOPE, required = false)
      TreeSearchScope scope) {

    return ResponseEntity.of(domainUserService.getUser(samAccountName, ou, scope));
  }

  @Operation(
      description = "Update user.",
      security = {
          @SecurityRequirement(name = "bearer-jwt"),
          @SecurityRequirement(name = "basic-auth")}
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
  public ResponseEntity<DomainUser> updateUser(
      @Parameter(name = "name", description = "The name of the user.", required = true)
      @PathVariable("name") String samAccountName,

      @RequestBody DomainUser user,

      @Parameter(name = "move-to",
          description = "The new organizational unit of the user like 'CN=Users,CN=Marketing'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = "move-to", required = false)
      Dn newOu) {

    return ResponseEntity.ok(domainUserService.updateUser(samAccountName, user, newOu));
  }

  @Operation(
      description = "Delete user.",
      security = {
          @SecurityRequirement(name = "bearer-jwt"),
          @SecurityRequirement(name = "basic-auth")}
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
  public ResponseEntity<Boolean> deleteUser(
      @Parameter(name = "name", description = "The name of the user.", required = true)
      @PathVariable("name") String samAccountName) {

    return ResponseEntity.ok(domainUserService.deleteUser(samAccountName));
  }

  @Operation(
      description = "Update user password.",
      security = {
          @SecurityRequirement(name = "bearer-jwt"),
          @SecurityRequirement(name = "basic-auth")}
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
      path = "/{name}/pwd",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<Void> updateUserPassword(
      @Parameter(name = "name", description = "The name of the user.", required = true)
      @PathVariable("name") String samAccountName,

      @Schema(
          description = "The clear password of the user. If it is empty or null, a random "
              + "password will be generated.",
          requiredMode = RequiredMode.NOT_REQUIRED,
          example = "voF7J!0$kPsfk$fz")
      @RequestBody(required = false) String clearPassword,

      @Parameter(
          name = "invitation-email",
          description = "Specifies whether an invitation email should be sent or not.",
          schema = @Schema(type = "boolean", defaultValue = "false"))
      @RequestParam(name = "invitation-email", defaultValue = "false")
      boolean sendInvitationEmail) {

    String newPassword;
    if (isEmpty(clearPassword)) {
      newPassword = domainService.createRandomPassword();
    } else {
      newPassword = clearPassword;
    }
    domainUserService.updateUserPassword(samAccountName, newPassword);
    if (sendInvitationEmail) {
      domainUserService.getUser(samAccountName, null, null)
          .ifPresentOrElse(
              user -> {
                if (isEmpty(user.getEmail())) {
                  throw ServiceException.badRequest(
                      "User has no email address. Sending invitation email is not possible.");
                }
                String baseUri = getBaseUri(properties.getEmail().getBaseUri());
                InvitationEvent event = new InvitationEvent(user, baseUri);
                eventPublisher.publishEvent(event);
              },
              () -> {
                throw ServiceException.notFoundWithErrorCode(
                    DomainUser.class.getSimpleName(),
                    samAccountName,
                    ErrorCode.EC_SAM_ACCOUNT_NOT_FOUND);
              });
    }
    return ResponseEntity.ok().build();
  }

  @Operation(
      description = "Update user avatar.",
      security = {
          @SecurityRequirement(name = "bearer-jwt"),
          @SecurityRequirement(name = "basic-auth")}
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
      path = "/{name}/avatar",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<Void> updateUserAvatar(
      @Parameter(name = "name", description = "The name of the user.", required = true)
      @PathVariable("name") String samAccountName,

      @Schema(description = "The avatar as jpeg.", type = "string", format = "binary")
      @RequestBody Resource avatar) {

    try (InputStream in = avatar.getInputStream()) {
      domainUserService.updateUserAvatar(samAccountName, in);

    } catch (IOException e) {
      throw ServiceException.internalServerError("Uploading avatar failed.", e);
    }
    return ResponseEntity.ok().build();
  }

  @Operation(
      description = "Remove user avatar.",
      security = {
          @SecurityRequirement(name = "bearer-jwt"),
          @SecurityRequirement(name = "basic-auth")}
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
  @DeleteMapping(path = "/{name}/avatar", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> removeUserAvatar(
      @Parameter(name = "name", description = "The name of the user.", required = true)
      @PathVariable("name") String samAccountName) {

    domainUserService.removeUserAvatar(samAccountName);
    return ResponseEntity.ok().build();
  }

  @Operation(
      description = "Checks whether the avatar of the user is present in the active directory.",
      security = {
          @SecurityRequirement(name = "bearer-jwt"),
          @SecurityRequirement(name = "basic-auth")}
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
  @GetMapping(path = "/{name}/avatar/existence", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Boolean> existsAvatarInActiveDirectory(
      @Parameter(name = "name", description = "The name of the user.", required = true)
      @PathVariable("name") String samAccountName,

      @Parameter(name = OU,
          description = "The search base (organizational unit) like 'CN=Users'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = OU, required = false)
      Dn ou,

      @Parameter(name = SCOPE, description = "The search scope (one-level|subtree).",
          schema = @Schema(type = "string"))
      @RequestParam(name = SCOPE, required = false)
      TreeSearchScope scope) {

    return ResponseEntity
        .ok(domainUserService.existsAvatarInActiveDirectory(samAccountName, ou, scope));
  }

}
