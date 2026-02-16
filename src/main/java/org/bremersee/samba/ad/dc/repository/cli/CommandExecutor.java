/*
 * Copyright 2019-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.samba.ad.dc.repository.cli;

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
import org.bremersee.samba.ad.dc.config.ApplicationProperties;
import org.bremersee.samba.ad.dc.misc.DefaultDnTool;
import org.bremersee.samba.ad.dc.misc.DnTool;

/**
 * The command executor.
 *
 * @author Christian Bremer
 */
@Slf4j
public class CommandExecutor {

  @Getter(AccessLevel.PROTECTED)
  private final ApplicationProperties properties;

  @Getter(AccessLevel.PROTECTED)
  private final DnTool dnTool;

  /**
   * Instantiates a new command executor.
   *
   * @param properties the properties
   */
  public CommandExecutor(ApplicationProperties properties) {
    this.properties = properties;
    this.dnTool = new DefaultDnTool(this.properties);
  }

  /**
   * Execute command.
   *
   * @param commands the commands
   * @return the command executor response
   */
  public CommandExecutorResponse execute(List<String> commands) {
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
      return new CommandExecutorResponse(output, error);

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

  /**
   * Execute.
   *
   * @param commands the commands
   * @param responseValidator the response validator
   */
  public void execute(List<String> commands,
      CommandExecutorResponseValidator responseValidator) {
    executeAndGet(commands, (CommandExecutorResponseParser<?>) responseValidator);
  }

  /**
   * Execute and get parsed response.
   *
   * @param <T> the type parameter
   * @param commands the commands
   * @param responseParser the response parser
   * @return the parsed response
   */
  public <T> T executeAndGet(List<String> commands,
      CommandExecutorResponseParser<T> responseParser) {
    return responseParser.parse(execute(commands));
  }

  private static List<String> parseCommands(List<String> commands) {
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

  /**
   * Quote string.
   *
   * @param value the value
   * @return the string
   */
  public static String quote(String value) {
    if (isEmpty(value)) {
      return "\"\"";
    }
    if (value.contains("\"")) {
      return '\'' + value + '\'';
    }
    return '"' + value + '"';
  }

}
