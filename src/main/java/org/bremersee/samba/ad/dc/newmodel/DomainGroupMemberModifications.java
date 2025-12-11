package org.bremersee.samba.ad.dc.newmodel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

@Schema(description = "The domain group member modifications.")
@Value.Style(
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    jdk9Collections = true,
    get = {"get*", "is*"},
    withUnaryOperator = "with*")
@Value.Immutable
@Serial.Version(1L)
@JsonSerialize(as = ImmutableDomainGroupMemberModifications.class)
@JsonDeserialize(as = ImmutableDomainGroupMemberModifications.class)
public interface DomainGroupMemberModifications {

  @Value.Default
  default Set<String> getMembersToAdd() {
    return Set.of();
  }

  @Value.Default
  default Set<String> getMembersToRemove() {
    return Set.of();
  }

}
