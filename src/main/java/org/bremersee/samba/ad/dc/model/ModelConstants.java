package org.bremersee.samba.ad.dc.model;

public class ModelConstants {

  public static final String DISCRIMINATOR = "_type";

  public static final String AD_ENTRY = "ad-entry";

  public static final String DNS_ENTRY = "dns-entry";

  public static final String DNS_ZONE = "dns-zone";

  public static final String COMPUTER = "computer";

  public static final String GROUP = "group";

  public static final String GROUP_MEMBER = "group-member";

  public static final String USER = "user";

  public static final String ORGANIZATIONAL_UNIT = "organizational-unit";



  public static final String SAM_ACCOUNT = "sam-account";

  private ModelConstants() {
    super();
  }
}
