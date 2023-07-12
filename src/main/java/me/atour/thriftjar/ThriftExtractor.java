package me.atour.thriftjar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.SystemUtils;

/**
 * Extracts the Thrift executable from the JAR.
 */
public class ThriftExtractor {

  /**
   * The extracted Thrift executable.
   */
  @Getter
  private File thriftExecutable;

  /**
   * Supported operating systems enum.
   */
  enum OperatingSystems {
    LINUX_X86,
    WINDOWS,
    UNKNOWN
  }

  /**
   * Extracts the Thrift executable.
   *
   * @param thriftVersion the Thrift compiler version to extract
   * @throws IOException when the Thrift executable cannot be extracted
   */
  public ThriftExtractor(String thriftVersion) throws IOException {
    extractThriftCompiler(thriftVersion);
  }

  /**
   * Extracts the specified Thrift compiler executable from the JAR until JVM termination.
   *
   * @param thriftVersion the Thrift compiler version to extract
   * @throws IOException when the Thrift executable cannot be extracted
   */
  @NonNull private void extractThriftCompiler(String thriftVersion) throws IOException {
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

    String sourceFilePath = "bin" + '/' + thriftVersion + '/' + executableName();

    File thriftTemp = new File(bin, "thrift.exe");

    try (InputStream in = ThriftCompiler.class.getClassLoader().getResourceAsStream(sourceFilePath)) {
      Files.copy(Objects.requireNonNull(in), thriftTemp.toPath());
    }

    if (!thriftTemp.setExecutable(true)) {
      throw new CannotExtractExecutableException();
    }
    thriftTemp.deleteOnExit();
    thriftExecutable = thriftTemp;
  }

  /**
   * Gets the file name of the appropriate executable for this machine.
   *
   * @return the file name of the executable
   */
  @NonNull private String executableName() {
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
   * @return some {@link ThriftExtractor.OperatingSystems} value for this machine
   */
  private ThriftExtractor.OperatingSystems getOs() {
    ThriftExtractor.OperatingSystems os;
    if (SystemUtils.IS_OS_LINUX && isX86()) {
      os = ThriftExtractor.OperatingSystems.LINUX_X86;
    } else if (SystemUtils.IS_OS_WINDOWS) {
      os = ThriftExtractor.OperatingSystems.WINDOWS;
    } else {
      os = ThriftExtractor.OperatingSystems.UNKNOWN;
    }
    return os;
  }

  /**
   * Checks whether this machine is x86.
   *
   * @return a {@code boolean} indicating whether this machine is x86
   */
  private boolean isX86() {
    return System.getProperty("os.arch").contains("x86");
  }
}
