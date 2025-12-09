package org.bremersee.samba.ad.dc.repository.tools.cli.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bremersee.samba.ad.dc.ErrorCode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DnsEntryUpdateValidator extends DnsEntryValidator {

  private static DnsEntryUpdateValidator instance;

  public static DnsEntryUpdateValidator getInstance() {
    if (instance == null) {
      instance = new DnsEntryUpdateValidator();
    }
    return instance;
  }

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
