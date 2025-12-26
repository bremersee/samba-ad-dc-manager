package org.bremersee.samba.ad.dc.model;

import jakarta.validation.constraints.NotEmpty;

public record AesEncValue(@NotEmpty String encryptedValue, @NotEmpty String salt) {

}
