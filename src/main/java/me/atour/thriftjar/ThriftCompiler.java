package me.atour.thriftjar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

/**
 * The Thrift compiler.
 */
@Slf4j
public class ThriftCompiler {

  /**
   * Runs the Thrift compiler.
   *
   * @param args Thrift compiler version flag followed by the CLI arguments for the Thrift compiler
   */
  public ThriftCompiler(String[] args) {
    try {
      String version = "0.18.1";
      if (args.length >= 1 && args[0].startsWith("--thriftversion") && args[0].length() > 15) {
        version = args[0].substring(15);
        args = Arrays.copyOfRange(args, 1, args.length);
      }
      runThrift(version, args);
    } catch (IOException e) {
      log.error("Failed to run Thrift because of IOException {}.", e.getMessage());
    } catch (InterruptedException e) {
      log.error("Failed to run Thrift because of InterruptedException {}.", e.getMessage());
    }
  }

  /**
   * Runs the Thrift compiler with the provided arguments.
   *
   * @param version the Thrift compiler version to use
   * @param args the Thrift arguments
   * @throws IOException when the Thrift executable cannot be extracted or run
   * @throws InterruptedException when the Thrift compiler {@link Process} gets interrupted
   */
  private void runThrift(String version, String[] args) throws IOException, InterruptedException {
    File exe = new ThriftExtractor(version).getThriftExecutable();
    executeThrift(exe, args);
  }

  /**
   * Execute the specified executable with the given arguments.
   *
   * @param executable the Thrift executable to run
   * @param args the CLI arguments to pass
   * @throws IOException when the {@link Process} cannot be started
   * @throws InterruptedException when the {@link Process} gets interrupted
   */
  private void executeThrift(@NonNull File executable, @NonNull String @NonNull [] args)
      throws IOException, InterruptedException {
    String absolutePathToExecutable = executable.getAbsolutePath();
    List<String> command = new ArrayList<>(args.length + 1);
    command.add(absolutePathToExecutable);
    Collections.addAll(command, args);
    ProcessBuilder pb = new ProcessBuilder(command);
    Process thriftCompiler = pb.start();
    Thread errorLogger = new Thread(() -> logStream(thriftCompiler.getErrorStream(), Level.ERROR));
    Thread infoLogger = new Thread(() -> logStream(thriftCompiler.getInputStream(), Level.INFO));
    errorLogger.start();
    infoLogger.start();
    int thriftExitCode = thriftCompiler.waitFor();
    errorLogger.join();
    infoLogger.join();
    if (thriftExitCode != 0) {
      throw new AbnormalThriftCompilerTerminationException(thriftExitCode);
    }
  }

  /**
   * Logs the contents of the {@link InputStream} to SLF4J.
   * Useful when trying to log from other {@link Process}es, like with the Thrift compiler.
   *
   * @param inputStream the {@link InputStream} to log
   * @param logLevel the log level to log at
   */
  private void logStream(@NonNull InputStream inputStream, @NonNull Level logLevel) {
    String line = "";
    int read = 0;
    while (read > -1) {
      try {
        read = inputStream.read();
      } catch (IOException e) {
        log.debug("Caught IOException while reading from stream in `logStream(InputStream, Level)`.", e);
        continue;
      }
      char readChar;
      if (read == -1) {
        readChar = '\n';
      } else {
        readChar = (char) read;
      }
      if (readChar == '\n') {
        if (line.equals("\r") || line.equals("")) {
          line = "";
          continue;
        }
        switch (logLevel) {
          case WARN:
            log.warn(line);
            break;
          case INFO:
            log.info(line);
            break;
          case ERROR:
            log.error(line);
            break;
          case DEBUG:
            log.debug(line);
            break;
          case TRACE:
          default:
            log.trace(line);
            break;
        }
        line = "";
      } else {
        line += readChar;
      }
    }
  }
}
