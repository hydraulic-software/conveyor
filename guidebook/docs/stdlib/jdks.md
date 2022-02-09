# JDKs

Import a JDK:

```
import required("/stdlib/jdk/YOUR_VERSION/YOUR_DISTRO.conf")
```

`YOUR_VERSION` can be replaced with either a major Java feature version e.g. `11`, `14`, `17` etc, in which case it will resolve to the latest known version of that Java release by the given distributor. Or you can specify the precise version, like `17.0.1`. Be aware that not all versions are available from all distributors.

`YOUR_DISTRO` is a name like `openjdk`. See below for the full list.

This will merge your config with another config snippet from the stdlib that defines the URLs of the JDK download for each target machine.

## Version matrix

To see which versions of which JDK distros are known run `conveyor jdk-table`. The default output is a colored table. Redirecting stdout to something that's not a terminal will yield machine parseable output with space separated columns in which the first column is the version, and the others contain names of distributions (try `conveyor jdk-table | cat`).

## Distributions

* `adoptopenjdk`: This project offered plain vanilla OpenJDK builds until Java 17, at which point it was renamed to Eclipse Temurin.
* `amazon`: Amazon Corretto is a plain vanilla OpenJDK build with occasional bug fix backports. As used by AWS.
* `azul`: Plain vanilla OpenJDK builds from Azul. Not to be confused with Azul's commercial Zulu Prime JVM which has a pauseless garbage collector, LLVM based JIT compiler and ReadyNow technology.
* `eclipse`: Plain vanilla OpenJDK builds, previously provided under the name AdoptOpenJDK.
* `graalvm-community-11`: GraalVM is a HotSpot based JVM with a unique and advanced JIT compiler. It's especially good at running non-Java languages fast. These are the Java 11 based Community Edition (free/open source) builds. The version number for GraalVM deviates from the OpenJDK standard: use a version like `21.3` rather than `11`.
* `graalvm-community-17`: Same but based on Java 17.
* `jetbrains`: The JetBrains Runtime is the spin of OpenJDK used by JetBrain's IDEs. Mostly the same as OpenJDK but with many bug fixes and enhancements useful for Swing GUI apps.
* `microsoft`: A plain vanilla OpenJDK build, which unlike most others is also available for ARM Windows.
* `openjdk`: The upstream builds supplied by OpenJDK. Note that there are no long term releases available for these.

## Custom JDKs

Don't worry if the one you want to use isn't in the list. The standard library snippets are very small. Here's an example:

```
// Snippet: JDK 17.0 by corretto
app.jvm.feature-version = 17
app.jvm.version = 17.0

app.jvm {
    linux.aarch64.glibc.inputs += "https://corretto.aws/downloads/resources/17.0.0.35.1/amazon-corretto-17.0.0.35.1-linux-aarch64.tar.gz"
    linux.amd64.glibc.inputs += "https://corretto.aws/downloads/resources/17.0.0.35.1/amazon-corretto-17.0.0.35.1-linux-x64.tar.gz"
    linux.amd64.muslc.inputs += "https://corretto.aws/downloads/resources/17.0.0.35.1/amazon-corretto-17.0.0.35.1-alpine-linux-x64.tar.gz"
    mac.aarch64.inputs += "https://corretto.aws/downloads/resources/17.0.0.35.2/amazon-corretto-17.0.0.35.2-macosx-aarch64.tar.gz"
    mac.amd64.inputs += "https://corretto.aws/downloads/resources/17.0.0.35.1/amazon-corretto-17.0.0.35.1-macosx-x64.tar.gz"
    windows.amd64.inputs += "https://corretto.aws/downloads/resources/17.0.0.35.1/amazon-corretto-17.0.0.35.1-windows-x64-jdk.zip"
}
```

You can easily point your config at any set of JDK distributions. The exact internal layout doesn't matter - JMODs will be located regardless of where they are in the directory or archive.