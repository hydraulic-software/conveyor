# JDKs

!!! note "Gradle projects usually import a JDK automatically"
    The [Conveyor Gradle plugin](../configs/maven-gradle.md#gradle) normally selects a JDK from the Java toolchain configuration. Run `./gradlew -q printConveyorConfig` and check for an `/stdlib/jdk/...` include before adding one manually. Importing both the Gradle-selected JDK and a snippet from this page causes a duplicate-JDK error.

For projects that don't import a JDK from Gradle, add a standard-library snippet to your Conveyor config:

```
include required("/stdlib/jdk/YOUR_VERSION/YOUR_DISTRO.conf")
```

`YOUR_VERSION` can be replaced with either a major Java feature version e.g. `11`, `14`, `17` etc, in which case it will resolve to the latest known version of that Java release by the given distributor. Or you can specify the precise version, like `17.0.1`. Be aware that not all versions are available from all distributors.

`YOUR_DISTRO` is a name like `openjdk`. See below for the full list.

This will merge your config with another config snippet from the stdlib that defines the URLs of the JDK download for each target machine.

## Version matrix

To see which versions of which JDK distros are known run `conveyor jdk-table`. The default output is a colored table. Redirecting stdout to something that's not a terminal will yield machine parseable output with space separated columns in which the first column is the version, and the others contain names of distributions (try `conveyor jdk-table | cat`).

## Distributions

If you aren't sure which to pick, we recommend Amazon or Eclipse.

* `amazon`: Amazon Corretto is a plain vanilla OpenJDK build with occasional bug fix backports. As used by AWS.
* `azul`: The Azul Zulu JDK. Plain vanilla builds from Azul. Not to be confused with Azul's commercial Zulu Prime JVM which has additional features not found in any other JVM.
* `eclipse`: Plain vanilla OpenJDK builds, previously provided under the name AdoptOpenJDK and now also known as Adoptium or Temurin. 
* `jetbrains`: The JetBrains Runtime is the spin of OpenJDK used by JetBrain's IDEs. Mostly the same as OpenJDK but with many bug fixes and enhancements useful for Swing GUI apps.
* `microsoft`: A plain vanilla OpenJDK build, which unlike most others is also available for ARM Windows. They only produce builds for LTS releases and point users to Eclipse for non-LTS releases.
* `openjdk`: The upstream builds supplied by OpenJDK. Note that there are no long term support releases available for these, so major versions stop receiving updates after six months.

!!! important
    Avoid using `adoptopenjdk`. It is in the stdlib for backwards compatibility only. Over time this project stopped providing builds for all platforms and the latest JDK11 release does not provide builds for macOS at all. For comprehensive and up to date builds of JDK11 and JDK17, please consider switching to another vendor (e.g. `eclipse` or `amazon`).

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

## JMOD-only inputs

Conveyor links a minimal Java runtime containing only the modules your app needs, using the JDK's JMOD files. Starting with Java 24 some vendors, including Eclipse Temurin, no longer put JMOD files in their JDK downloads. Given a JDK without them Conveyor has to bundle the entire JDK, making your app several times bigger.

Temurin publishes its JMOD files as a separate download, so for Temurin 24.0.2 onwards the `eclipse` snippets download only those instead of the full JDK. If Temurin hasn't published JMODs for a platform, the snippet uses the full JDK for that platform.

You can write JMOD-only inputs for other JDKs too. Because JMODs don't record everything Conveyor needs to know about the JDK, you must also supply a `release` file giving the version (including the build number), vendor, OS, CPU architecture and, on Linux, the C library. These values must describe exactly the same build as the JMODs, so don't mix them up between versions or vendors. For example:

```
app.jvm {
    linux.amd64.glibc.inputs += { from = "https://example.com/jdk-25.0.1+8-linux-x64-jmods.tar.gz", to = jmods }
    linux.amd64.glibc.inputs += { to = release, content = """
JAVA_VERSION="25.0.1+8"
IMPLEMENTOR="Example Vendor"
IMPLEMENTOR_VERSION="ExampleJDK-25.0.1+8"
OS_NAME="linux"
OS_ARCH="x64"
MODULES=""
LIBC="glibc"
    """ }
}
```

## GraalVM

You can write config to point at a GraalVM install, but please be aware that this won't let you select languages. GraalVM uses a different packaging system to other JDKs and thus the normal linking process isn't sufficient to create a minimal JDK with support for Truffle languages. Improved support for GraalVM is planned for a future release.
