# Gradle plugin

This module contains a simple Gradle plugin that extracts settings from a build and emits a Conveyor config snippet. This makes it easy
to keep metadata and dependencies from your source build and packaging build in sync.

**If you want to learn how to package your app then [start with the main documentation](https://conveyor.hydraulic.dev/).**  

To apply this plugin [look up the latest version](https://plugins.gradle.org/plugin/dev.hydraulic.conveyor) and then use code like this:

```
plugins {
    id("dev.hydraulic.conveyor") version "1.5"
}
```

It adds two tasks:

* `printConveyorConfig` - emits the generated config to standard out where it's easy to examine, or can be immediately included into the
  main config using a hashbang include.
* `writeConveyorConfig` - emits the generated config to a file in the _project_ source directory (not the build directory). It can then be
  included into the main config using a regular include. The advantage is that Gradle doesn't get invoked each time, so it's faster, but
  you have to re-run when necessary.

The plugin extracts the following:

* `project.version`, assigned to `app.version`
* `project.group`, assigned to `app.rdns-name`
* `project.name`, assigned to `gradle.project-name`
* `buildDir`, assigned to `gradle.build-dir`
* From the `application` plugin: the main class and JVM arguments are extracted.
* From the `java` plugin: the JDK version and vendor used (if recognized), resulting in a JDK import.
* From the Jetpack Compose Desktop plugin: main class, JVM arguments, description, vendor.
* From the JavaFX plugin: the version you're using and which modules.

It defines machine specific dependency configurations that can be used to add dependencies which should only take effect on particular
platforms:

* `linuxAmd64`
* `linuxAarch64`
* `linuxAmd64Muslc`
* `linuxAarch64Muslc`
* `macAmd64`
* `macAarch64` (Apple Silicon)
* `windowsAmd64`
* `windowsAarch64`

The results are emitted to the right section of config. Also, the host machine running Gradle is examined and the `implementation`
configuration is made to extend the appropriate machine-specific configuration. This can be helpful even if you don't use Conveyor.

## Example 1

A simple app that depends on [Conscrypt](https://github.com/google/conscrypt), an arbitrarily chosen library with native code:

```kotlin
plugins {
    `java-library`
    application
    id("dev.hydraulic.conveyor") version "1.5"
}

dependencies {
    val conscryptVersion = "2.5.2"
    windowsAmd64("org.conscrypt:conscrypt-openjdk:$conscryptVersion:windows-x86_64")
    macAmd64("org.conscrypt:conscrypt-openjdk:$conscryptVersion:osx-x86_64")
    linuxAmd64("org.conscrypt:conscrypt-openjdk:$conscryptVersion:linux-x86_64")
}

application {
  mainClass.set("yourMainClass")
}
```

## Example 2

A more sophisticated example showing how to exclude a platform independent grouping dependency, and add all the platform 
specific dependencies using a bit of refactored generic code. It's for an app that uses the SWT GUI toolkit:

```kotlin
plugins {
    `java-library`
    application
    id("dev.hydraulic.conveyor") version "1.5"
}

repositories {
    mavenCentral()
}

val swt_version = "3.119.0"

fun DependencyHandlerScope.swt(platformConveyor: String, platformSwt: String) {
    // Add the platform specific SWT dependency to the platform specific dependency configuration.
    add(platformConveyor, "org.eclipse.platform:org.eclipse.swt.$platformSwt:$swt_version") {
        // We don't need the empty grouping artifact and it gets in the way.
        exclude("org.eclipse.platform", "org.eclipse.swt.\${osgi.platform}")
    }
}

dependencies {
    swt("macAmd64", "cocoa.macosx.x86_64")
    swt("macAarch64", "cocoa.macosx.aarch64")
    swt("windowsAmd64", "win32.win32.x86_64")
    swt("linuxAmd64", "gtk.linux.x86_64")
}

application {
    mainClass.set("yourMainClass")
    // SWT needs this JVM flag.
    applicationDefaultJvmArgs = listOf("-XstartOnFirstThread")
}
```
