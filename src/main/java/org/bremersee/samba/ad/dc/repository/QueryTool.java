package org.bremersee.samba.ad.dc.repository;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class QueryTool {

  public static String[] getTokens(String searchTerm) {
    return getTokens(searchTerm, 1);
  }

  public static String[] getTokens(String searchTerm, int minLength) {
    if (isEmpty(searchTerm)) {
      return new String[0];
    }
    String trimmed = searchTerm.trim();
    if (trimmed.isEmpty()) {
      return new String[0];
    }
    StringTokenizer tokenizer = new StringTokenizer(trimmed, ", \t\n\r\f");
    List<String> tokens = new ArrayList<>();
    String firstQuotedToken = null;
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken().trim();
      if (isQuotedStartToken(token)) {
        if (isQuotedEndToken(token) && token.length() > 1) {
          addToken(tokens, token.substring(1, token.length() - 1), minLength);
        } else {
          if (!isEmpty(firstQuotedToken)) {
            addToken(tokens, firstQuotedToken, minLength);
            firstQuotedToken = null;
          }
          token = token.substring(1);
          if (!isEmpty(token)) {
            firstQuotedToken = token;
          }
        }
      } else if (isQuotedEndToken(token)) {
        token = token.substring(0, token.length() - 1);
        StringBuilder sb = new StringBuilder();
        if (!isEmpty(firstQuotedToken)) {
          sb.append(firstQuotedToken);
        }
        if (!isEmpty(token)) {
          if (!sb.isEmpty()) {
            sb.append(" ");
          }
          sb.append(token);
        }
        addToken(tokens, sb.toString(), minLength);
        firstQuotedToken = null;
      } else if (!isEmpty(firstQuotedToken)) {
        firstQuotedToken = firstQuotedToken + " " + token;
      } else {
        addToken(tokens, token, minLength);
      }
    }
    addToken(tokens, firstQuotedToken, minLength);
    return tokens.toArray(new String[0]);
  }

  private static void addToken(List<String> tokens, String token, int minLength) {
    if (!isEmpty(token) && token.length() >= minLength) {
      tokens.add(token);
    }
  }

  private static boolean isQuotedStartToken(String token) {
    return token.startsWith("\"") || token.startsWith("'");
  }

  private static boolean isQuotedEndToken(String token) {
    return token.endsWith("\"") || token.endsWith("'");
  }

}
