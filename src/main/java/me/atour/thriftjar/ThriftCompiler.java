package me.atour.thriftjar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * The Thrift compiler.
 */
@Slf4j
public class ThriftCompiler {

  /**
   * Runs the Thrift compiler.
   *
   * @param args CLI arguments for the Thrift compiler
   */
  public ThriftCompiler(String[] args) {
    try {
      runThrift(args);
    } catch (IOException e) {
      log.error("Failed to run Thrift because of IOException {}.", e.getMessage());
    } catch (InterruptedException e) {
      log.error("Failed to run Thrift because of InterruptedException {}.", e.getMessage());
    }
  }

  /**
   * Runs the Thrift compiler with the provided arguments.
   *
   * @param args the Thrift arguments
   * @throws IOException when the Thrift executable cannot be extracted or run
   * @throws InterruptedException when the Thrift compiler {@link Process} gets interrupted
   */
  private static void runThrift(String[] args) throws IOException, InterruptedException {
    File exe = new ThriftExtractor().getThriftExecutable();
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
  private static void executeThrift(@NonNull File executable, @NonNull String @NonNull [] args)
      throws IOException, InterruptedException {
    String absolutePathToExecutable = executable.getAbsolutePath();
    List<String> command = new ArrayList<>(args.length + 1);
    command.add(absolutePathToExecutable);
    Collections.addAll(command, args);
    ProcessBuilder pb = new ProcessBuilder(command);
    Process thriftCompiler = pb.start();
    int thriftExitCode = thriftCompiler.waitFor();
    if (thriftExitCode != 0) {
      throw new AbnormalThriftCompilerTerminationException(thriftExitCode);
    }
  }
}
