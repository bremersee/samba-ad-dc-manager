package org.bremersee.samba.ad.dc.newmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.springframework.lang.Nullable;

@Schema(description = "DNS zone.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableDnsZone.class)
@JsonDeserialize(as = ImmutableDnsZone.class)
public interface DnsZone extends AdEntry {

  @Schema(description = "The zone name.", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "name", required = true)
  String getName();

  @Schema(description = "The zone type.")
  @Nullable
  String getZoneType();

  @Schema(description = "Specifies whether this zone is a reverse zone or not.")
  @Nullable
  Boolean getReverseZone();

  @Nullable
  String getAllowUpdate();

  @Nullable
  Boolean getPaused();

  @Nullable
  Boolean getShutdown();

  @Nullable
  Boolean getAutoCreated();

  @Nullable
  Boolean getUseDatabase();

  @Nullable
  String getDataFile();

  @Nullable
  Boolean getUseWins();

  @Nullable
  Boolean getUseNbstat();

  @Nullable
  Boolean getAging();

  @Nullable
  String getFqdn();

  @Nullable
  Boolean getQueuedForBackgroundLoad();

  @Nullable
  Boolean getBackgroundLoadInProgress();

  @Nullable
  Boolean getReadOnlyZone();

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
  class Builder extends ImmutableDnsZone.Builder {

  }

}
