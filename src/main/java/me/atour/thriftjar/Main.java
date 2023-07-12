package me.atour.thriftjar;

/**
 * Main application class.
 */
public class Main {

  /**
   * Runs the Thrift compiler.
   *
   * @param args CLI arguments for the Thrift compiler
   */
  public static void main(String[] args) {
    new ThriftCompiler(args);
  }
}
