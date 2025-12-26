package org.bremersee.samba.ad.dc.config;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import lombok.Data;
import org.bremersee.spring.core.regex.RegexFlags;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
public class EmailProperties implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Email regex from <a href="https://emailregex.com/">emailregex.com</a> (RFC 5322 Official
   * Standard).
   *
   * <p>RFC 6530 is not supported.
   */
  public static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+"
      + "(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
      + "|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]"
      + "|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+"
      + "[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}"
      + "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:"
      + "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]"
      + "|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";

  private String emailRegex = EMAIL_REGEX;

  @NestedConfigurationProperty
  private RegexFlags emailRegexFlags = new RegexFlags();

  private String sender = "christian@bremersee.org";

  private String regards = "Christian";

  private String baseUri = "http://localhost:8080";

  public RegexFlags getEmailRegexFlags() {
    if (Objects.equals(getEmailRegex(), EMAIL_REGEX) && !emailRegexFlags.isCaseInsensitive()) {
      emailRegexFlags.setCaseInsensitive(true);
    }
    return emailRegexFlags;
  }

}
