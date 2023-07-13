# Thrift compiler JAR

Multi-platform Thrift compiler.

JAR that packages precompiled Thrift compiler binaries for Windows and Linux x86, providing portability
across this limited set of platforms. Supports all Thrift versions >=0.6.0 for Windows and 0.18.1 for Linux
x86. As Thrift only published binaries for Windows, there is no possibility to download the binaries and
avoid using the embedded ones for now.

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
String version = "0.18.1";
String[] args = { version, "-r", "--gen", "java", "example.thrift" };
new ThriftCompiler(args);
```

## Installation

To install the project, first clone it from GitHub. Then go to the directory it was cloned to and run the
Maven install command as follows, to install the project to your local Maven repository.

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
