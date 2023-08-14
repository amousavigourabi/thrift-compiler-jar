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
    LINUX_AARCH64,
    LINUX_ARMV7,
    LINUX_PPC64LE,
    LINUX_S390X,
    LINUX_X86_64,
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
   * Extracts the Thrift executable.
   *
   * @param thriftVersion the Thrift compiler version to extract
   * @return a {@link File} representation of the executables location after extraction
   * @throws IOException when the Thrift executable cannot be extracted
   */
  @NonNull public static File extract(String thriftVersion) throws IOException {
    ThriftExtractor thriftExtractor = new ThriftExtractor(thriftVersion);
    return thriftExtractor.getThriftExecutable();
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

    try (InputStream in = getClass().getClassLoader().getResourceAsStream(sourceFilePath)) {
      if (in == null) {
        throw new CannotLocateAppropriateExecutableException();
      }
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
      case LINUX_AARCH64:
        return "thrift-linux_aarch64.exe";
      case LINUX_ARMV7:
        return "thrift-linux_armv7.exe";
      case LINUX_PPC64LE:
        return "thrift-linux_ppc64le.exe";
      case LINUX_S390X:
        return "thrift-linux_s390x.exe";
      case LINUX_X86_64:
        return "thrift-linux_x86_64.exe";
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
    OperatingSystems os;
    if (SystemUtils.IS_OS_LINUX) {
      if (isX86_64()) {
        os = OperatingSystems.LINUX_X86_64;
      } else if (isS390x()) {
        os = OperatingSystems.LINUX_S390X;
      } else if (isArmV7()) {
        os = OperatingSystems.LINUX_ARMV7;
      } else if (isPpc64le()) {
        os = OperatingSystems.LINUX_PPC64LE;
      } else if (isAarch64()) {
        os = OperatingSystems.LINUX_AARCH64;
      } else {
        os = OperatingSystems.UNKNOWN;
      }
    } else if (SystemUtils.IS_OS_WINDOWS) {
      os = OperatingSystems.WINDOWS;
    } else {
      os = OperatingSystems.UNKNOWN;
    }
    return os;
  }

  /**
   * Checks whether this machine is x86_64.
   *
   * @return a {@code boolean} indicating whether this machine is x86_64
   */
  private boolean isX86_64() {
    String arch = System.getProperty("os.arch").toLowerCase();
    return arch.contains("x8664") || arch.contains("x64") || arch.contains("amd64");
  }

  /**
   * Checks whether this machine is s390x.
   *
   * @return a {@code boolean} indicating whether this machine is s390x
   */
  private boolean isS390x() {
    String arch = System.getProperty("os.arch").toLowerCase();
    return arch.contains("s390x");
  }

  /**
   * Checks whether this machine is armv7.
   *
   * @return a {@code boolean} indicating whether this machine is armv7
   */
  private boolean isArmV7() {
    String arch = System.getProperty("os.arch").toLowerCase();
    return arch.contains("arm") && arch.contains("v7");
  }

  /**
   * Checks whether this machine is ppc64le.
   *
   * @return a {@code boolean} indicating whether this machine is ppc64le
   */
  private boolean isPpc64le() {
    String arch = System.getProperty("os.arch").toLowerCase();
    return arch.contains("ppc64le");
  }

  /**
   * Checks whether this machine is aarch64.
   *
   * @return a {@code boolean} indicating whether this machine is aarch64
   */
  private boolean isAarch64() {
    String arch = System.getProperty("os.arch").toLowerCase();
    return arch.contains("aarch64");
  }
}
