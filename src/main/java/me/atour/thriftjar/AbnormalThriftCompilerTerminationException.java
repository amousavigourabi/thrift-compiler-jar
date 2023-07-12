package me.atour.thriftjar;

/**
 * Thrown when the Thrift executable return a non-zero exit code.
 */
public class AbnormalThriftCompilerTerminationException extends RuntimeException {

  /**
   * Constructs the {@link RuntimeException} for the given exit code.
   *
   * @param exitCode the Thrift compiler's exit code
   */
  public AbnormalThriftCompilerTerminationException(int exitCode) {
    super(String.valueOf(exitCode));
  }
}
