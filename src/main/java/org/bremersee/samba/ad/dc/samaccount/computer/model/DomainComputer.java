package org.bremersee.samba.ad.dc.samaccount.computer.model;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.bremersee.samba.ad.dc.samaccount.common.model.SamAccount;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.springframework.lang.Nullable;

@Schema(description = "The domain computer.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableDomainComputer.class)
@JsonDeserialize(as = ImmutableDomainComputer.class)
public interface DomainComputer extends SamAccount {

  @Schema(description = "The name of the computer.")
  @JsonProperty(value = "name")
  @Nullable
  @Override
  String getName();

  @Schema(description = "The dns host name of the computer.")
  @Nullable
  String getDnsHostName();

  @Schema(description = "The network addresses of the computer.")
  @Value.Default
  default List<String> getNetworkAddresses() {
    return List.of();
  }

  @Schema(description = "The operating system of the computer.")
  @Nullable
  String getOperatingSystem();

  @Schema(description = "The version operating system.")
  @Nullable
  String getOperatingSystemVersion();

  @Schema(description = "The description of the computer.")
  @Nullable
  String getDescription();

  @Schema(description = "The service principal names of the computer.")
  @Value.Default
  default List<String> getServicePrincipalNames() {
    return List.of();
  }

  @Hidden
  @JsonIgnore
  @Value.Lazy
  default String getSamAccountNameWithoutTrailingDollarSign() {
    String tmpName = getSamAccountName();
    if (isNull(tmpName) || tmpName.isEmpty()) {
      return null;
    }
    return tmpName.substring(0, tmpName.length() - 1);
  }

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
  class Builder extends ImmutableDomainComputer.Builder {

  }

}
