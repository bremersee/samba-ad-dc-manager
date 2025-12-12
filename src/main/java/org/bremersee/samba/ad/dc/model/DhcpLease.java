package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.OffsetDateTime;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.springframework.lang.Nullable;

/**
 * The dhcp lease of the dhcp server.
 *
 * @author Christian Bremer
 */
@Schema(description = "The dhcp lease of the dhcp server.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableDhcpLease.class)
@JsonDeserialize(as = ImmutableDhcpLease.class)
public interface DhcpLease {

  /**
   * Gets mac.
   *
   * @return the mac
   */
  @Schema(description = "The mac of the client.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "mac", required = true)
  String getMac();

  /**
   * Gets ip.
   *
   * @return the ip
   */
  @Schema(description = "The ip of the client.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "ip", required = true)
  String getIp();

  /**
   * Gets hostname.
   *
   * @return the hostname
   */
  @Schema(description = "The host name of the client.", defaultValue = "-NA-")
  @JsonProperty(value = "hostname", defaultValue = "-NA")
  @Value.Default
  default String getHostname() {
    return "-NA-";
  }

  /**
   * Gets the beginning.
   *
   * @return the beginning
   */
  @Schema(description = "The start time of the lease.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "begin", required = true)
  OffsetDateTime getBegin();

  /**
   * Gets end.
   *
   * @return the end
   */
  @Schema(description = "The end time of the lease.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "end", required = true)
  OffsetDateTime getEnd();

  /**
   * Gets manufacturer.
   *
   * @return the manufacturer
   */
  @Schema(description = "The manufacturer of the client.")
  @Nullable
  String getManufacturer();

  /**
   * Gets the immutable builder.
   *
   * @return the builder
   */
  static Builder builder() {
    return new Builder();
  }

  /**
   * The immutable builder.
   */
  class Builder extends ImmutableDhcpLease.Builder {

  }

}
