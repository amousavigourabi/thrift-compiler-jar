package me.atour.thriftjar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

/**
 * The Thrift compiler.
 */
@Slf4j
public class ThriftCompiler {

  /**
   * Hides the default no-arg constructor.
   */
  private ThriftCompiler() {
    throw new IllegalStateException();
  }

  /**
   * Supported operating systems enum.
   */
  enum OperatingSystems {
    LINUX_X86,
    WINDOWS,
    UNKNOWN
  }

  /**
   * Runs the Thrift compiler.
   *
   * @param args CLI arguments for the Thrift compiler
   */
  public static void main(String[] args) {
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
   */
  public static void runThrift(String[] args) throws IOException, InterruptedException {
    File exe = extractThriftCompiler();
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
  public static void executeThrift(@NonNull File executable, @NonNull String @NonNull [] args)
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

  /**
   * Extracts the specified Thrift compiler executable from the JAR until JVM termination.
   *
   * @return a {@link File} representing the location to which the executable is extracted
   */
  @NonNull public static File extractThriftCompiler() throws IOException {
    File target = File.createTempFile("thrift", "");
    if (!target.delete() || !target.mkdirs()) {
      throw new CannotExtractExecutableException();
    }
    target.deleteOnExit();

    File bin = new File(target, "bin");
    if (!bin.mkdirs()) {
      throw new CannotExtractExecutableException();
    }
    bin.deleteOnExit();

    String thriftVersion = "0.18.1";

    String sourceFilePath = "bin" + '/' + thriftVersion + '/' + executableName();

    File thriftTemp = new File(bin, "thrift.exe");

    try (InputStream in = ThriftCompiler.class.getClassLoader().getResourceAsStream(sourceFilePath)) {
      Files.copy(Objects.requireNonNull(in), thriftTemp.toPath());
    }

    if (!thriftTemp.setExecutable(true)) {
      throw new CannotExtractExecutableException();
    }
    thriftTemp.deleteOnExit();
    return thriftTemp;
  }

  /**
   * Gets the file name of the appropriate executable for this machine.
   *
   * @return the file name of the executable
   */
  @NonNull public static String executableName() {
    switch (getOs()) {
      case WINDOWS:
        return "thrift-windows.exe";
      case LINUX_X86:
        return "thrift-linux_x86.exe";
      case UNKNOWN:
      default:
        throw new CannotLocateAppropriateExecutableException();
    }
  }

  /**
   * Gets the OS and architecture of the machine.
   *
   * @return some {@link OperatingSystems} value for this machine
   */
  public static OperatingSystems getOs() {
    OperatingSystems os;
    if (SystemUtils.IS_OS_LINUX && isX86()) {
      os = OperatingSystems.LINUX_X86;
    } else if (SystemUtils.IS_OS_WINDOWS) {
      os = OperatingSystems.WINDOWS;
    } else {
      os = OperatingSystems.UNKNOWN;
    }
    return os;
  }

  /**
   * Checks whether this machine is x86.
   *
   * @return a {@code boolean} indicating whether this machine is x86
   */
  public static boolean isX86() {
    return System.getProperty("os.arch").contains("x86");
  }
}
