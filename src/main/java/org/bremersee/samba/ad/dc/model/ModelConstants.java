package org.bremersee.samba.ad.dc.model;

public class ModelConstants {

  public static final String DISCRIMINATOR = "_json_type";

  public static final String AD_ENTRY = "AdEntry";

  public static final String DNS_ENTRY = "DnsEntry"; //.class.getSimpleName(); // " dns-entry";

  public static final String DNS_ZONE = "DnsZone"; //.class.getSimpleName(); // "dns-zone";

  public static final String COMPUTER = "DomainComputer"; //.class.getSimpleName(); // "computer";

  public static final String GROUP = "DomainGroup"; //.class.getSimpleName(); // "group";

  public static final String GROUP_MEMBER = "DomainGroupMember"; //.class.getSimpleName(); // "group-member";

  public static final String USER = "DomainUser"; //.class.getSimpleName(); // "user";

  public static final String ORGANIZATIONAL_UNIT = "OrganizationalUnit"; //.class.getSimpleName(); // "organizational-unit";

  public static final String SAM_ACCOUNT = "SamAccount"; //.class.getSimpleName(); // "sam-account";

  private ModelConstants() {
    super();
  }
}
