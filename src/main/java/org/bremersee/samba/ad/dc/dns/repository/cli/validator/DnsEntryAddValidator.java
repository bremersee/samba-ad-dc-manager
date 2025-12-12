package org.bremersee.samba.ad.dc.dns.repository.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;

public class DnsEntryAddValidator extends DnsEntryValidator {

  @Override
  String getExpectedResponse() {
    return "Record added successfully";
  }

  @Override
  String getAction() {
    return "Adding";
  }

  @Override
  String getErrorCode() {
    return ErrorCode.EC_ADDING_DNS_ENTRY_FAILED;
  }

}
