package org.bremersee.samba.ad.dc.repository.tools.cli.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bremersee.samba.ad.dc.ErrorCode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DnsEntryAddValidator extends DnsEntryValidator {

  private static DnsEntryAddValidator instance;

  public static DnsEntryAddValidator getInstance() {
    if (instance == null) {
      instance = new DnsEntryAddValidator();
    }
    return instance;
  }

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
