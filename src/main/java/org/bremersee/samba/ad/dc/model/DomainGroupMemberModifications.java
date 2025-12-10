package org.bremersee.samba.ad.dc.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;

@Data
public class DomainGroupMemberModifications {

  private Set<String> membersToAdd = new HashSet<>();

  private Set<String> membersToRemove = new HashSet<>();

}
