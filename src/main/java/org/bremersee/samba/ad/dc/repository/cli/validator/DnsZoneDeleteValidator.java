package org.bremersee.samba.ad.dc.repository.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;

public class DnsZoneDeleteValidator extends DnsEntryValidator {

  private final String zoneName;

  public DnsZoneDeleteValidator(String zoneName) {
    this.zoneName = zoneName;
  }

  @Override
  String getExpectedResponse() {
    return String.format("Zone %s deleted successfully", zoneName);
  }

  @Override
  String getAction() {
    return "Deleting";
  }

  @Override
  String getErrorCode() {
    return ErrorCode.EC_DELETING_DNS_ZONE_FAILED;
  }

}
