# Thrift compiler JAR

Multi-platform Thrift compiler.

Executable JAR that packages precompiled Thrift compiler binaries for Windows and Linux systems,
providing portability across these platforms. Supports all Thrift versions starting at 0.6.0 for Windows
and Linux systems running on 64-bit x86, aarch64, s390x, ppc64le, and armv7 machines. As Thrift only
publishes binaries for Windows, it is impossible to avoid using the embedded binaries and instead
download them on the fly.

## Usage

The JAR is executable, which means you can run it directly from the terminal.
The arguments passed to the JAR will directly be used to run the Thrift compiler.
The only exception to this is if the first argument is `--thriftversion`, this would be used
to specify what version of Thrift to use instead.

This is an example of how the JAR would be used to compile `example.thrift` using Thrift 0.18.0.

```shell
java -jar thrift-compiler-jar.jar --thriftversion=0.18.0 -r --gen java example.thrift
```

Another way this application can be used is through its API.
This is an example of how you could invoke the Thrift 0.18.1 compiler to compile `example.thrift`.

```java
String version = "--thriftversion=0.18.1";
String[] args = { version, "-r", "--gen", "java", "example.thrift" };
new ThriftCompiler(args);
```

One issue with the above method is that it provides little flexibility. For example, when multiple
invocations of the Thrift compiler are necessary, it will extract the binaries for every single one of
those invocations. To avoid this, you can instead use the following, more fluent, calls.

```java
String version = "0.18.1";
String[] args = { "-r", "--gen", "java", "example.thrift" };
File binary = ThriftExtractor.extract(version);
ThriftCompiler.run(binary, args);
```

In the case you need to invoke the compiler multiple times, you can just run the
`ThriftCompiler#run(File, String[])` call multiple times, to avoid extracting the binary for all
executions. If you want to use your own Thrift executable with the application, you can also use your
own binaries using this call in the following way.

```java
File customBinary = ...;
String[] args = { "-r", "--gen", "java", "example.thrift" };
ThriftCompiler.run(customBinary, args);
```

## Installation

To install the project, first clone it from GitHub. Then go to the directory it was cloned to and run the
Maven install command to install the project to your local Maven repository.

```shell
mvn clean install
```

Then, you can use the project by including the following Maven dependency in your projects.

```xml
<dependency>
  <groupId>me.atour</groupId>
  <artifactId>thrift-compiler-jar</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Reproducibility

This project contains precompiled binaries. These can pose large security risks. After all, why would
they be trusted? To take away some of these concerns, the used binaries are built transparently using
the actions in the [amousavigourabi/thrift-binary](https://github.com/amousavigourabi/thrift-binary)
repository. These builds are designed to be fully reproducible by re-running the actions. This can be
done by creating your own fork and triggering the run of the build workflow. By comparing the outputs
of this new run and the binaries included in this project, it can be verified that no malicious code was
injected in the pre-compiled binaries.
