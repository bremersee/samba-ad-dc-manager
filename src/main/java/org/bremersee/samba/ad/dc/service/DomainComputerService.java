package org.bremersee.samba.ad.dc.service;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import org.bremersee.samba.ad.dc.model.DomainComputer;
import org.bremersee.samba.ad.dc.model.TreeSearchScope;
import org.jspecify.annotations.Nullable;
import org.ldaptive.dn.Dn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DomainComputerService {

  @NotNull
  Page<DomainComputer> getComputers(
      @NotNull Pageable pageable,
      @Nullable String query,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  Optional<DomainComputer> getComputer(
      @NotEmpty String name,
      @Nullable Dn ou,
      @Nullable TreeSearchScope searchScope);

  @NotNull
  DomainComputer updateComputer(@NotNull DomainComputer domainComputer, @Nullable Dn newOu);

  boolean deleteComputer(@NotEmpty String name);

}
