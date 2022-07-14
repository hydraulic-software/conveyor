# JavaFX

JavaFX is a mature, hardware accelerated GUI toolkit for desktop apps. The standard library snippet for JavaFX tells Conveyor to download
and link the modules into the runtime JVM image. It's useful for:

* Gradle based apps that use the OpenJFX plugin.
* Maven based apps that use the OpenJFX plugin.
* Plain apps that don't use either build system.

## Gradle builds

When using [the JavaFX Gradle plugin from openjfx.io](https://openjfx.io/openjfx-docs/#gradle), everything is done for you. Just follow
the instructions for adding [the Conveyor Gradle plugin](../configs/maven-gradle.md#gradle).

## Maven builds

Just include `/stdlib/jvm/javafx/from-maven.conf` at the top of your config. The config will run Maven to obtain the list of JARs for 
each supported platform. The native libraries will be pre-extracted during build time and it should all just work. Remember to sync 
the first line with whatever version and distribution of the JVM you wish to use.

```
include required("/stdlib/jdk/17/openjdk.conf")
include required("/stdlib/jvm/javafx/from-maven.conf")

app {
	....
}
```

## JMods version

The following config will cause the JavaFX JMODs to be downloaded and linked in to the runtime. Your inputs should therefore _not_ include
the JavaFX files.

```hocon
include "/stdlib/jvm/17/openjdk.conf"
include "/stdlib/jvm/javafx/from-jmods.conf" 1️⃣

javafx.version = 17 2️⃣ 

app {
  inputs += "my-app-1.0.jar"
  inputs += "lib/*.jar"

  jvm {
    gui.main-class = hydraulic.demoapp.Demo
    modules += "javafx.{controls,fxml,media,swing,web}"  3️⃣
  }
}
```

1️⃣ Include the JavaFX snippet in the standard library.

2️⃣ Set the version of JavaFX you want to use.

3️⃣ Add the JavaFX modules you want to use. You will need at least `javafx.controls`. Be aware that `javafx.web` bundles a build of WebKit which will significantly increase your download size.
