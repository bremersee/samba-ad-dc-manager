package org.bremersee.samba.ad.dc.samaccount.computer.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.bremersee.comparator.model.SortOrder;
import org.bremersee.comparator.spring.mapper.SortMapper;
import org.bremersee.samba.ad.dc.common.controller.SortOrderConstants;
import org.bremersee.samba.ad.dc.common.controller.api.ApiController;
import org.bremersee.samba.ad.dc.common.model.TreeSearchScope;
import org.bremersee.samba.ad.dc.samaccount.computer.controller.ComputerControllerConstants;
import org.bremersee.samba.ad.dc.samaccount.computer.model.DomainComputer;
import org.bremersee.samba.ad.dc.samaccount.computer.model.DomainComputerPage;
import org.bremersee.samba.ad.dc.samaccount.computer.service.DomainComputerService;
import org.ldaptive.dn.Dn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/computers")
public class ComputerApiController extends ApiController {

  private final SortMapper sortMapper;

  private final DomainComputerService domainComputerService;

  public ComputerApiController(
      SortMapper sortMapper,
      DomainComputerService domainComputerService) {
    this.sortMapper = sortMapper;
    this.domainComputerService = domainComputerService;
  }

  // TODO @PageableAsQueryParam from sprindoc
  @Operation(
      summary = "Get page of computers.",
      description = "",
      security = {@SecurityRequirement(name = "bearer-jwt"),
          @SecurityRequirement(name = "basicAuth")}
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "OK."),
          @ApiResponse(responseCode = "401", description = "Unauthorized."),
          @ApiResponse(responseCode = "403", description = "Forbidden."),
      }
  )
  @GetMapping
  public ResponseEntity<DomainComputerPage> getComputers(
      @Parameter(description = "Zero-based page index (0..N).",
          name = PAGE,
          schema = @Schema(type = "integer", defaultValue = PAGE_DEFAULT))
      @RequestParam(name = PAGE, defaultValue = PAGE_DEFAULT) int page,

      @Parameter(description = "The size of the page to be returned.",
          name = SIZE,
          schema = @Schema(type = "integer", defaultValue = SIZE_DEFAULT))
      @RequestParam(name = SIZE, defaultValue = SIZE_DEFAULT) int size,

      @Parameter(description = "Sorting criteria in the format: property,(asc|desc). Default "
          + "sort order is ascending. Multiple sort criteria are supported.",
          name = SORT,
          array = @ArraySchema(schema = @Schema(type = "string")))
      @RequestParam(name = SORT, required = false) List<SortOrder> sortOrder,

      @Parameter(name = "query", description = "A search term.")
      @RequestParam(name = "query", required = false)
      String query,

      @Parameter(name = "ou",
          description = "The search base (organizational unit) like 'CN=Computers'.",
          schema = @Schema(type = "string"))
      @RequestParam(name = "ou", required = false)
      Dn ou,

      @Parameter(name = "scope", description = "The search scope (one-level|subtree).",
          schema = @Schema(type = "string"))
      @RequestParam(name = "scope", required = false)
      TreeSearchScope scope) {

    Sort sort = sortMapper.toSort(sortOrder, ComputerControllerConstants.COMPUTER_SORT);
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<DomainComputer> computerPage = domainComputerService
        .getComputers(pageable, query, ou, scope);
    return ResponseEntity.ok(new DomainComputerPage(computerPage));
  }
}
