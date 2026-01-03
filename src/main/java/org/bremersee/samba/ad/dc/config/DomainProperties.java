package org.bremersee.samba.ad.dc.config;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class DomainProperties implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String hostName;

  private int maximumPasswordLength = 75;

  private String simplePasswordRegexTemplate = "^(?=.{%d,%d}$).*";

  private String complexPasswordRegexTemplate = "(?=^.{%d,%d}$)"
      + "((?=.*\\d)(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[^A-Za-z0-9])(?=.*[a-z])"
      + "|(?=.*[^A-Za-z0-9])(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[A-Z])(?=.*[^A-Za-z0-9]))^.*";

  private List<String> forbiddenNewSamAccountNames = new ArrayList<>();

  private String defaultNisDomain;
}
