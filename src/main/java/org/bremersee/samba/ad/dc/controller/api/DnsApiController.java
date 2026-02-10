package org.bremersee.samba.ad.dc.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.bremersee.exception.model.RestApiException;
import org.bremersee.samba.ad.dc.model.DhcpLease;
import org.bremersee.samba.ad.dc.model.DhcpLeasePage;
import org.bremersee.samba.ad.dc.model.DnsEntry;
import org.bremersee.samba.ad.dc.model.DnsEntryPage;
import org.bremersee.samba.ad.dc.model.DnsEntryType;
import org.bremersee.samba.ad.dc.model.DnsZone;
import org.bremersee.samba.ad.dc.model.DnsZoneType;
import org.bremersee.samba.ad.dc.service.DnsService;
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
@RequestMapping(path = "/api/dns")
public class DnsApiController extends ApiController {

  private final DnsService dnsService;

  public DnsApiController(DnsService dnsService) {
    this.dnsService = dnsService;
  }

  @Operation(
      description = "Get DNS zones.",
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
  @GetMapping(path = "/zones", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<String>> getDnsZoneNames(
      @Parameter(
          name = ZONE_TYPE,
          description = "Specifies the zone type.",
          schema = @Schema(
              implementation = DnsZoneType.class,
              defaultValue = "primary",
              example = "reverse"))
      @RequestParam(name = ZONE_TYPE, defaultValue = "primary") DnsZoneType zoneType) {

    return ResponseEntity.ok(dnsService.getDnsZoneNames(zoneType));
  }

  @Operation(
      description = "Add DNS zone.",
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
  @PutMapping(path = "/zones", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DnsZone> addDnsZone(
      @Parameter(name = ZONE_NAME, description = "The zone name.", required = true)
      @RequestParam(name = ZONE_NAME) String zoneName) {

    return ResponseEntity.ok(dnsService.createDnsZone(zoneName));
  }

  @Operation(
      description = "Get DNS zone.",
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
  @GetMapping(path = "/zones/{zone}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DnsZone> getDnsZone(
      @Parameter(name = "zone", description = "The name of the zone.", required = true)
      @PathVariable("zone") String zoneName) {

    return ResponseEntity.ok(dnsService.getDnsZone(zoneName));
  }

  @Operation(
      description = "Delete DNS zone.",
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
  @DeleteMapping(path = "/zones/{zone}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deleteDnsZone(
      @Parameter(name = "zone", description = "The name of the zone.", required = true)
      @PathVariable("zone") String zoneName) {

    dnsService.deleteDnsZone(zoneName);
    return ResponseEntity.ok().build();
  }

  @PageableAsQueryParam
  @Operation(
      description = "Get dns entries of a zone.",
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
  @GetMapping(path = "/zones/{zone}/entries", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DnsEntryPage> getDnsEntries(
      @Parameter(name = "zone", description = "The name of the zone.", required = true)
      @PathVariable("zone") String zoneName,

      @Parameter(hidden = true)
      @PageableDefault(
          size = SIZE_DEFAULT_INT,
          sort = {DNS_ENTRY_NAME, DNS_ENTRY_TYPE, DNS_ENTRY_VALUE})
      Pageable pageable,

      @Parameter(name = QUERY, description = "A search term.")
      @RequestParam(name = QUERY, required = false)
      String query) {

    return ResponseEntity.ok(new DnsEntryPage(dnsService.getDnsEntries(zoneName, pageable, query)));
  }

  @Operation(
      description = "Add DNS entry to a zone.",
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
      path = "/zones/{zone}/entries",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DnsEntry> addDnsEntry(
      @Parameter(name = "zone", description = "The name of the zone.", required = true)
      @PathVariable("zone") String zoneName,

      @RequestBody DnsEntry dnsEntry) {

    DnsEntry entry = dnsEntry.withZoneName(zoneName);
    dnsService.addDnsEntry(entry);
    return ResponseEntity.of(dnsService.findDnsEntry(entry));
  }

  @Operation(
      description = "Get DNS entry of a zone.",
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
  @GetMapping(
      path = "/zones/{zone}/entries/{name}/{type}/{value}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DnsEntry> getDnsEntry(
      @Parameter(name = "zone", description = "The name of the zone.", required = true)
      @PathVariable("zone") String zoneName,

      @Parameter(name = "name", description = "The name of the entry.", required = true)
      @PathVariable("name") String entryName,

      @Parameter(name = "type", description = "The type of the entry.", required = true)
      @PathVariable("type") DnsEntryType entryType,

      @Parameter(name = "value", description = "The value of the entry.", required = true)
      @PathVariable("value") String entryValue,

      @Parameter(
          name = "reverse",
          description = "Get reverse entry.",
          schema = @Schema(type = "boolean", defaultValue = "false", example = "false"))
      @RequestParam(name = "reverse", defaultValue = "false") boolean reverseEntry) {

    DnsEntry entry = DnsEntry.builder()
        .zoneName(zoneName)
        .name(entryName)
        .type(entryType)
        .value(entryValue)
        .build();
    if (reverseEntry) {
      return ResponseEntity.of(dnsService.findReverseDnsEntry(entry));
    }
    return ResponseEntity.of(dnsService.findDnsEntry(entry));
  }

  @Operation(
      description = "Update DNS entry of a zone.",
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
      path = "/zones/{zone}/entries/{name}/{type}/{value}",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<DnsEntry> updateDnsEntry(
      @Parameter(name = "zone", description = "The name of the zone.", required = true)
      @PathVariable("zone") String zoneName,

      @Parameter(name = "name", description = "The name of the entry.", required = true)
      @PathVariable("name") String entryName,

      @Parameter(name = "type", description = "The type of the entry.", required = true)
      @PathVariable("type") DnsEntryType entryType,

      @Parameter(name = "value", description = "The value of the entry.", required = true)
      @PathVariable("value") String entryValue,

      @RequestBody String newValue) {

    DnsEntry entry = DnsEntry.builder()
        .zoneName(zoneName)
        .name(entryName)
        .type(entryType)
        .value(entryValue)
        .build();
    dnsService.updateDnsEntry(entry, newValue);
    return ResponseEntity.of(dnsService.findDnsEntry(entry.withValue(newValue)));
  }

  @Operation(
      description = "Delete DNS entry of a zone.",
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
  @DeleteMapping(
      path = "/zones/{zone}/entries/{name}/{type}/{value}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deleteDnsEntry(
      @Parameter(name = "zone", description = "The name of the zone.", required = true)
      @PathVariable("zone") String zoneName,

      @Parameter(name = "name", description = "The name of the entry.", required = true)
      @PathVariable("name") String entryName,

      @Parameter(name = "type", description = "The type of the entry.", required = true)
      @PathVariable("type") DnsEntryType entryType,

      @Parameter(name = "value", description = "The value of the entry.", required = true)
      @PathVariable("value") String entryValue) {

    DnsEntry entry = DnsEntry.builder()
        .zoneName(zoneName)
        .name(entryName)
        .type(entryType)
        .value(entryValue)
        .build();
    dnsService.deleteDnsEntry(entry);
    return ResponseEntity.ok().build();
  }

  @PageableAsQueryParam
  @Operation(
      description = "Get dhcp leases.",
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
  @GetMapping(path = "/dhcp/leases", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DhcpLeasePage> getDhcpLeases(
      @Parameter(hidden = true)
      @PageableDefault(size = SIZE_DEFAULT_INT, sort = DHCP_LEASE_SORT) Pageable pageable,

      @Parameter(name = QUERY, description = "A search term.")
      @RequestParam(name = QUERY, required = false)
      String query) {

    Page<DhcpLease> page = dnsService.getDhcpLeases(pageable, query);
    return ResponseEntity.ok(new DhcpLeasePage(page));
  }

}
