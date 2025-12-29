package org.bremersee.samba.ad.dc.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.springframework.lang.Nullable;

@Schema(description = "The password information of an active directory.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableDomainInfo.class)
@JsonDeserialize(as = ImmutableDomainInfo.class)
public interface DomainInfo extends Serializable {

  @Nullable
  String getForest();

  @Nullable
  String getDomain();

  @Nullable
  String getNetbiosDomain();

  @Nullable
  String getDomainControllerName();

  @Nullable
  String getDomainControllerNetbiosName();

  @Nullable
  String getServerSite();

  @Nullable
  String getClientSite();

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
  class Builder extends ImmutableDomainInfo.Builder {

  }

}
