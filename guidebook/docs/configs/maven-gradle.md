# Maven and Gradle integration

Conveyor can run external commands whilst evaluating config files, see the [modified HOCON spec](hocon-spec.md) for details. This makes it easy to dynamically calculate config from any script or program, including external build systems.

## Maven

Instead of "installing" your app somewhere locally to get a directory of JARs, you can make Conveyor  get the classpath of your project directly from Maven. Add this to the top of your config file:

```
include "#!=app.inputs mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout -Dmdep.pathSeparator=${line.separator}"
```

This will use the [`dependency:build-classpath`](https://maven.apache.org/plugins/maven-dependency-plugin/build-classpath-mojo.html) goal to emit a list of paths to JARs on your file system, which will then be treated as an array and written to the key `app.inputs`. Maven's output will replace whatever was in the `app.inputs` key already, so make sure this include statement comes first before anything else that might need to add inputs.

!!! note
    The above command will only work on UNIX.

## Gradle

Conveyor provides an [open source Gradle plugin](https://github.com/hydraulic-software/conveyor/tree/master/gradle-plugin) which extracts configuration from your build and emits it as HOCON. It also lets you configure machine-specific dependencies that are automatically put into the right section of the input hierarchy.

!!! info
    You must use Gradle 7 or above for the plugin to work.

??? info "Which tool on top?"
    The Gradle plugin restricts itself to generating configuration for a few different reasons:

    * It's much faster to run Conveyor directly than go via the slow Gradle daemon. Conveyor can invoke Gradle using hashbang include (see below) when you want the two to be tightly in sync.
    * You'll probably want to set up signing keys/certificates, which isn't the sort of task you automate with a build system. 
    * Only generating configuration keeps the integration transparent and easy to understand, which is important when working with a complex system like Gradle.
    * A minimalist plugin makes it less likely to break as Gradle evolves its APIs, which has frequently been a problem in the past with other plugins.

To use it, [look up the latest version](https://plugins.gradle.org/plugin/dev.hydraulic.conveyor) and then apply the plugin in your Gradle build:

=== "Kotlin"
    ```kotlin title="build.gradle.kts"
    plugins {
        id("dev.hydraulic.conveyor") version "1.0"
    }
    ```
=== "Groovy"
    ```groovy title="build.gradle"
    plugins {
        id 'dev.hydraulic.conveyor' version '1.0'
    }
    ```

For now, you will also need to add our repository to your `settings.gradle{.kts}`:

=== "Kotlin"
    ```kotlin title="settings.gradle.kts"
    pluginManagement {
        repositories {
            gradlePluginPortal()
            maven("https://maven.hq.hydraulic.software")
        }
    }
    ```
=== "Groovy"
    ```groovy title="settings.gradle"
    pluginManagement {
        repositories {
            gradlePluginPortal()
            maven { url = "https://maven.hq.hydraulic.software" }
        }
    }
    ```

Now you will have two tasks, `printConveyorConfig` and `generateConveyorConfig`. The first prints the config to the screen so you can examine it. The latter writes the config to the project source directory under the name `generated.conveyor.conf` where it's easily included into your main config file. It also adds the following dependency configurations:

* `linuxAmd64`
* `linuxAarch64`
* `linuxAmd64Muslc`
* `linuxAarch64Muslc`
* `macAmd64`
* `macAarch64` (Apple Silicon)
* `windowsAmd64`
* `windowsAarch64`

to which you can add dependencies that should only be used on particular machines. The configurations are set up such that whichever matches the machine running Gradle is added to the `implementation` configuration and the rest are kept to one side.

The plugin extracts information from other plugins:

* The `version` becomes `app.version`
* The `group` becomes `app.rdns-name`
* `buildDir` becomes `gradle.build-dir`
* `project.name` becomes `gradle.project-name`
* From the `application` plugin: main class,  JVM arguments.
* From the JetPack Compose Desktop plugin: main class, JVM arguments, description, vendor.
* From the JavaFX plugin: the modules you're using.

Here's a worked example for a Compose Desktop app:

`build.gradle.kts`:

```kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("kapt") version "1.6.10"
    id("org.jetbrains.compose") version "1.0.1"
    id("dev.hydraulic.conveyor") version "1.0"
}

version = "1.0"
group = "dev.hydraulic.samples"

repositories {
    mavenCentral()
    google()
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        name = "Compose for Desktop DEV"
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            vendor = "Hydraulic Software"
            description = "An example of how to package a Compose Desktop app with Conveyor"
        }
    }
}

dependencies {
    "linuxAmd64"(compose.desktop.linux_x64)
    "macAmd64"(compose.desktop.macos_x64)
    "macAarch64"(compose.desktop.macos_arm64)
    "windowsAmd64"(compose.desktop.windows_x64)
}

```

And now we can write in our `conveyor.conf` file one of:

```
# Invoke Gradle each time (slow)
include "#!gradlew --console=plain --quiet printConveyorConfig"

app { ... }
```

... or ...

```
# Read the file generated using :writeConveyorConfig (fast)
include required("generated.conveyor.conf")

app { ... }
```

The first form will invoke Gradle each time to read the config. It means you will never be out of sync, but even with the Gradle daemon this is slow and takes a moment. The second form reads the generated file from disk, so Gradle doesn't get involved and it's nice and fast.

??? tip "Best of both worlds"
    You can of course create two conf files, one for each approach, and switch between them using the `-f` flag. Each conf only contains includes from Gradle and a base config. Then your CI can use the version that always invokes Gradle, and for local iteration you can use the fast one.

You can easily extend this by just adding some code to the end of the relevant tasks that appends to the file, like this:

=== "Kotlin"
    ```kotlin
    tasks.named<hydraulic.conveyor.gradle.WriteConveyorConfigTask>("writeConveyorConfig") {
        doLast {
            val extraConf = "// ..."
            destination.get().asFile.appendText(extraConf)
        }
    }
    ```
=== "Groovy"
    ```groovy
    tasks.writeConveyorConfig {
        doLast {
            var extraConf = "// Hello World"
            destination.get().asFile.append(extraConf)
        }
    }
    ```

You can change the `destination` property to control where the config file is written to.

If you want Gradle to run Conveyor as well, just define a normal execution task using words to this effect:

=== "Kotlin"
    ```kotlin
    tasks.register<Exec>("convey") {
        val dir = layout.buildDirectory.dir("packages")
        outputs.dir(dir)
        commandLine("conveyor", "make", "--output-dir", dir.get(), "site")
        dependsOn("jar", "writeConveyorConfig")
    }
    ```
=== "Groovy"
    ```groovy
    tasks.register("conveyor", Exec) {
        var dir = layout.buildDirectory.dir("packages")
        outputs.dir(dir)
        commandLine = ["conveyor", "make", "--output-dir", dir.get(), "site"]
        dependsOn("jar", "writeConveyorConfig")
    }
    ```
