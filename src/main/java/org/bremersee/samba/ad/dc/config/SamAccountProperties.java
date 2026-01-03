package org.bremersee.samba.ad.dc.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;

@Getter
@Setter
@EqualsAndHashCode
@ToString
abstract class SamAccountProperties implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @NotNull
  private String defaultOu;

  @NotNull
  private TreeSearchScope defaultSearchScope = TreeSearchScope.ONELEVEL;

  @Min(1)
  private int minQueryLength = 2;

  private String newSamAccountNameRegex = "^(?![._-])[a-zA-Z0-9._-]{3,75}$";

  SamAccountProperties() {
    super();
  }
}
