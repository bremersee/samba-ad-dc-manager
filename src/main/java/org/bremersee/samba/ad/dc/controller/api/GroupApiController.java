package org.bremersee.samba.ad.dc.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bremersee.comparator.ComparatorBuilder;
import org.bremersee.exception.ServiceException;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.samba.ad.dc.mapper.GroupPatchMapper;
import org.bremersee.samba.ad.dc.model.DomainGroup;
import org.bremersee.samba.ad.dc.model.DomainGroupMember;
import org.bremersee.samba.ad.dc.model.DomainGroupMemberModifications;
import org.bremersee.samba.ad.dc.model.DomainGroupMemberPage;
import org.bremersee.samba.ad.dc.model.DomainGroupMemberType;
import org.bremersee.samba.ad.dc.model.DomainGroupPage;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.service.DomainGroupService;
import org.ldaptive.dn.Dn;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springdoc.core.converters.models.SortAsQueryParam;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("api")
@RestController
@RequestMapping(path = "/api/groups")
public class GroupApiController extends ApiController {

  private final DomainGroupService domainGroupService;

  public GroupApiController(DomainGroupService domainGroupService) {
    this.domainGroupService = domainGroupService;
  }

  @PageableAsQueryParam
  @Operation(
      description = "Get group page.",
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
      description = "Add group.",
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
  public ResponseEntity<DomainGroup> addGroup(
      @RequestBody DomainGroup group,

      @Parameter(name = OU,
          description = "Add group to organizational unit (like 'CN=Groups').",
          schema = @Schema(type = "string"))
      @RequestParam(name = OU, required = false)
      Dn ou) {

    DomainGroup groupToAdd = group
        .withDistinguishedName("")
        .withSid(null)
        .withCriticalSystemObject(false)
        .withPrimaryGroupId(null)
        .withMemberships(List.of())
        .withMembers(List.of());
    return ResponseEntity.ok(domainGroupService.addGroup(groupToAdd, ou));
  }

  @Operation(
      description = "Get group.",
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
  public ResponseEntity<DomainGroup> getGroup(
      @Parameter(name = "name", description = "The name of the group or the group ID.")
      @PathVariable("name") String samAccountName,

      @Parameter(
          name = "by-group-id",
          description = "The given name is the group ID.",
          schema = @Schema(type = "boolean", defaultValue = "false"))
      @RequestParam(value = "by-group-id", defaultValue = "false") boolean nameIsGroupId,

      @Parameter(name = OU,
          description = "The search base (organizational unit) like 'CN=Groups'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = OU, required = false)
      Dn ou,

      @Parameter(name = SCOPE, description = "The search scope (one-level|subtree).",
          schema = @Schema(type = "string"))
      @RequestParam(name = SCOPE, required = false)
      TreeSearchScope scope) {

    Optional<DomainGroup> domainGroup;
    if (nameIsGroupId) {
      domainGroup = domainGroupService.getGroupByPrimaryGroupId(getGroupId(samAccountName));
    } else {
      domainGroup = domainGroupService.getGroup(samAccountName, ou, scope);
    }
    return ResponseEntity.of(domainGroup);
  }

  private int getGroupId(String name) {
    try {
      return Integer.parseInt(name);
    } catch (RuntimeException ignored) {
      throw ServiceException
          .badRequest(String.format("Value '%s' is not a valid group ID.", name));
    }
  }

  @Operation(
      description = "Update group.",
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
  @PatchMapping(
      path = "/{name}",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DomainGroup> updateGroup(
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
      TreeSearchScope scope,

      @RequestBody DomainGroup group,

      @Parameter(name = "move-to",
          description = "The new organizational unit like 'CN=Contact Groups,CN=Telephone Book'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = "move-to", required = false)
      Dn newOu) {

    Optional<DomainGroup> updated = domainGroupService.getGroup(samAccountName, ou, scope)
        .map(existing -> GroupPatchMapper.INSTANCE.patch(group, existing))
        .map(patched -> domainGroupService.updateGroup(samAccountName, patched, newOu));
    return ResponseEntity.of(updated);
  }

  @Operation(
      description = "Delete group.",
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
  public ResponseEntity<Boolean> deleteGroup(
      @Parameter(name = "name", description = "The name of the group.")
      @PathVariable("name") String samAccountName) {

    return ResponseEntity.ok(domainGroupService.deleteGroup(samAccountName));
  }

  @SortAsQueryParam
  @Operation(
      description = "Get memberships of group.",
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
  @GetMapping(path = "/{name}/memberships", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<DomainGroup>> getMemberships(
      @Parameter(name = "name", description = "The name of the group or the group ID.")
      @PathVariable("name") String samAccountName,

      @Parameter(
          name = "resolved",
          description = "Specifies whether the memberships should be resoled or not.",
          schema = @Schema(type = "boolean", defaultValue = "false"))
      @RequestParam(value = "resolved", defaultValue = "false") boolean resolveMemberships,

      @Parameter(name = OU,
          description = "The search base (organizational unit) like 'CN=Groups'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = OU, required = false)
      Dn ou,

      @Parameter(name = SCOPE, description = "The search scope (one-level|subtree).",
          schema = @Schema(type = "string"))
      @RequestParam(name = SCOPE, required = false)
      TreeSearchScope scope,

      @Schema(hidden = true)
      @SortDefault(sort = GROUP_SORT) Sort sort) {

    Stream<DomainGroup> groupStream;
    if (resolveMemberships) {
      groupStream = domainGroupService.resolveMemberships(samAccountName, ou, scope);
    } else {
      groupStream = domainGroupService.getMemberships(samAccountName, ou, scope);
    }
    List<DomainGroup> groups = groupStream
        .sorted(ComparatorBuilder.newInstance()
            .addAll(getSortMapper().fromSort(sort))
            .build())
        .toList();
    return ResponseEntity.ok(groups);
  }

  @PageableAsQueryParam
  @Operation(
      description = "Get group members.",
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
  @GetMapping(path = "/{name}/members", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DomainGroupMemberPage> getMemberSelection(
      @Parameter(name = "name", description = "The name of the group or the group ID.")
      @PathVariable("name") String samAccountName,

      @Parameter(name = OU,
          description = "The search base (organizational unit) like 'CN=Users'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = OU, required = false)
      Dn ou,

      @Parameter(name = SCOPE, description = "The search scope (one-level|subtree).",
          schema = @Schema(type = "string"))
      @RequestParam(name = SCOPE, required = false)
      TreeSearchScope scope,

      @Parameter(name = "member-type", description = "The type of a group member.")
      @RequestParam(name = "member-type", required = false)
      List<DomainGroupMemberType> memberTypes,

      @Parameter(
          name = "with-primary-members",
          description = "Specifies whether primary members should be returned or not.",
          schema = @Schema(type = "boolean", defaultValue = "true"))
      @RequestParam(value = "with-primary-members", defaultValue = "true")
      boolean withPrimaryMembers,

      @Parameter(hidden = true)
      @PageableDefault(size = SIZE_DEFAULT_INT, sort = "displayName") Pageable pageable,

      @Parameter(name = QUERY, description = "A search term.")
      @RequestParam(name = QUERY, required = false)
      String query) {

    Set<DomainGroupMemberType> memberTypeSet = Stream.ofNullable(memberTypes)
        .flatMap(Collection::stream)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Page<DomainGroupMember> memberPage = domainGroupService.getMemberSelection(
        pageable, query, memberTypeSet, withPrimaryMembers, samAccountName, ou, scope);
    return ResponseEntity.ok(new DomainGroupMemberPage(memberPage));
  }

  @Operation(
      description = "Modify group members.",
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
  @PatchMapping(
      path = "/{name}/members",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DomainGroup> modifyMembers(
      @Parameter(name = "name", description = "The name of the group or the group ID.")
      @PathVariable("name") String samAccountName,

      @Parameter(name = OU,
          description = "The search base (organizational unit) like 'CN=Users'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = OU, required = false)
      Dn ou,

      @Parameter(name = SCOPE, description = "The search scope (one-level|subtree).",
          schema = @Schema(type = "string"))
      @RequestParam(name = SCOPE, required = false)
      TreeSearchScope scope,

      @RequestBody DomainGroupMemberModifications modifications) {

    return ResponseEntity.ok(domainGroupService.modifyMembers(
        samAccountName,
        ou,
        scope,
        modifications.getMembersToAdd(),
        modifications.getMembersToRemove()));
  }

}
