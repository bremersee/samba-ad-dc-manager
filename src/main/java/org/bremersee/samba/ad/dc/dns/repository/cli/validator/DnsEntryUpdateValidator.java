package org.bremersee.samba.ad.dc.dns.repository.cli.validator;

import org.bremersee.samba.ad.dc.ErrorCode;

public class DnsEntryUpdateValidator extends DnsEntryValidator {

  @Override
  String getExpectedResponse() {
    return "Record updated successfully";
  }

  @Override
  String getAction() {
    return "Updating";
  }

  @Override
  String getErrorCode() {
    return ErrorCode.EC_UPDATING_DNS_ENTRY_FAILED;
  }

}
