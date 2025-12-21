package org.bremersee.samba.ad.dc.repository.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;

public class DnsEntryDeleteValidator extends DnsEntryValidator {

  @Override
  String getExpectedResponse() {
    return "Record deleted successfully";
  }

  @Override
  String getAction() {
    return "Deleting";
  }

  @Override
  String getErrorCode() {
    return ErrorCode.EC_DELETING_DNS_ENTRY_FAILED;
  }

}
