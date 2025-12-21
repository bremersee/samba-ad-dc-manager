package org.bremersee.samba.ad.dc.controller;

public abstract class AbstractController {

  public static final String PAGE = "page";

  public static final int PAGE_DEFAULT_INT = 0;

  public static final String PAGE_DEFAULT = "" + PAGE_DEFAULT_INT;

  public static final String SIZE = "size";

  public static final int SIZE_DEFAULT_INT = 20;

  public static final String SIZE_DEFAULT = "" + SIZE_DEFAULT_INT;

  public static final String SORT = "sort";

  public static final String COMPUTER_SORT = "name";

  public static final String DHCP_LEASE_SORT = "ip";

  public static final String DNS_ENTRY_SORT = "name;type;value";

  public static final String GROUP_SORT = "samAccountName";

  public static final String OU_SORT = "nameTree";

  public static final String USER_SORT = "lastName;firstName;samAccountName";

  public static final String QUERY = "q";

  public static final String QUERY_DEFAULT = "";

  public static final String OU = "ou";

  public static final String SCOPE = "scope";

  public static final String ZONE_NAME = "zone-name";

  public static final String ZONE_TYPE = "zone-type";

  public static final String ZONE_TYPE_DEFAULT = "primary";

  public static final String DNS_ENTRY_NAME = "name";

  public static final String DNS_ENTRY_TYPE = "type";

  public static final String DNS_ENTRY_VALUE = "value";

  protected AbstractController() {
  }

}
