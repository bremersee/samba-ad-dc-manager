package org.bremersee.samba.ad.dc.repository.tools.cli;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bremersee.exception.ServiceException;
import org.bremersee.samba.ad.dc.ErrorCode;
import org.bremersee.samba.ad.dc.config.DomainControllerProperties;

@Slf4j
class CommandExecutor {

  @Getter(AccessLevel.PROTECTED)
  private final DomainControllerProperties properties;

  CommandExecutor(DomainControllerProperties properties) {
    this.properties = properties;
  }

  CommandExecutorResponse execute(List<String> commands) {
    return executeAndGet(commands, response -> response);
  }

  void execute(List<String> commands,
      CommandExecutorResponseValidator responseValidator) {
    executeAndGet(commands, (CommandExecutorResponseParser<?>) responseValidator);
  }

  <T> T executeAndGet(List<String> commands,
      CommandExecutorResponseParser<T> responseParser) {

    List<String> extendedCommands = new ArrayList<>();
    if (getProperties().getCli().getSsh().isUsingSsh()) {
      extendedCommands.add(properties.getCli().getSsh().getSshCommand());
    }
    if (getProperties().getCli().getSudo().isUsingSudo()) {
      extendedCommands.add(properties.getCli().getSudo().getSudoCommand());
    }
    extendedCommands.addAll(commands);
    List<String> commandTokens = parseCommands(extendedCommands);
    try {
      ProcessBuilder pb = new ProcessBuilder(commandTokens);
      if (!isEmpty(properties.getCli().getExecDir())) {
        pb.directory(new File(properties.getCli().getExecDir()));
      }

      if (log.isDebugEnabled()) {
        log.debug("Executing commands = {}", String.join(" ", commandTokens));
      }
      Process p = pb.start();
      StringWriter out = new StringWriter();
      StringWriter err = new StringWriter();
      IOUtils.copy(p.getInputStream(), out, StandardCharsets.UTF_8);
      IOUtils.copy(p.getErrorStream(), err, StandardCharsets.UTF_8);
      p.waitFor();
      String output = out.toString();
      String error = err.toString();
      if (log.isTraceEnabled()) {
        log.trace("Program output:\n{}", output);
        log.trace("Program error output:\n{}", error);
      }
      return responseParser.parse(new CommandExecutorResponse(output, error));

    } catch (IOException | InterruptedException e) {
      ServiceException se = ServiceException.internalServerError(
          "Running commands failed.",
          ErrorCode.prefixErrorCode("6fa0f473-6204-4f75-9130-a1049910d8fd"),
          e);
      log.error("Executing commands [{}] failed.", commandTokens, se);
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      throw se;
    }
  }

  static List<String> parseCommands(List<String> commands) {
    return Stream.ofNullable(commands)
        .flatMap(Collection::stream)
        .filter(command -> !isEmpty(command))
        .flatMap(command -> {
          Stream<String> stream = Stream.empty();
          StringTokenizer tokenizer = new StringTokenizer(command, " ");
          while (tokenizer.hasMoreTokens()) {
            stream = Stream.concat(stream, Stream.of(tokenizer.nextToken()));
          }
          return stream;
        })
        .filter(command -> !isEmpty(command))
        .toList();
  }

  static String quote(String value) {
    if (isEmpty(value)) {
      return "\"\"";
    }
    if (value.contains("\"")) {
      return '\'' + value + '\'';
    }
    return '"' + value + '"';
  }

}
