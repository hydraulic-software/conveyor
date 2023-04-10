---
hide:
  - navigation
---

# Integrating Conveyor with Gradle projects

## conveyor.conf

Config can be extracted from your build system using a simple plugin. The Gradle plugin doesn't replace or drive the package build process itself: you still do that using the `conveyor` command line tool. The plugin is narrowly scoped to generating configuration and nothing more. If you want Gradle to run Conveyor you can add a normal exec task to do so.

The hand-written `conveyor.conf` file will typically look like this:

```javascript title="conveyor.conf"
include "#!./gradlew -q printConveyorConfig"  // (1)!

app {
  display-name = My Amazing Project   // (2)!
  site.base-url = downloads.myproject.org/some/path   // (3)!
  
  icons = "icons/icon-rounded-*.png"   // (4)!
  windows.icons = "icons/icon-square-*.png"
}
```

1. This is a [hashbang include](../../configs/hocon-extensions.md#including-the-output-of-external-commands). The given program will be run and the output included as if it were a static HOCON file.
2. You may not need to set this if the display name of your project is trivially derivable from the name of the Gradle project. Use `printConveyorConfig` to see what the plugin guessed.
3. This is where the created packages will look for update metadata.
4. The templates come with pre-rendered icons in both square and rounded rectangle styles. This bit of config uses square by default and rounded rects on macOS only, but that's just a style choice to fit in with the native expectations. You can use whatever icons you like. They should be rendered as PNGs in a range of square sizes, ideally 32x32, 64x64, 128x128 etc up to 1024x1024.

Many settings are missing (e.g. `app.rdns-name`) because they'll be read from your build configuration.

## Gradle plugin

To add the plugin:

=== "Kotlin"
    ```kotlin title="settings.gradle.kts"
    pluginManagement {
        repositories {
            gradlePluginPortal()
            maven("https://maven.hq.hydraulic.software")
        }
    }
    ```
    ```kotlin title="build.gradle.kts"
    plugins {
        id("dev.hydraulic.conveyor") version "1.4"
    }
    ```

=== "Groovy"
    ```groovy title="settings.gradle"
    pluginManagement {
        repositories {
            gradlePluginPortal()
            maven { uri = "https://maven.hq.hydraulic.software" }
        }
    }
    ```
    ```groovy title="build.gradle"
    plugins {
        id 'dev.hydraulic.conveyor' version '1.4'
    }
    ```

The plugin adds two tasks, `printConveyorConfig` and `writeConveyorConfig`. The first prints the generated config to stdout, and the second writes it to an output file. By default this is called `generated.conveyor.conf` but can be changed.

* [ ] Run `./gradlew -q printConveyorConfig` and examine the output.

!!! note
     The plugin can read config from other plugins like the Java application plugin, the Jetpack Compose plugin and the OpenJFX plugin. It can in some cases identify which JDK you want to bundle with your app from the Java toolchains setting. If it doesn't then it'll emit a comment to the generated config explaining why not, and you'll have to add another line of config to [import a JDK](../../configs/jvm.md#importing-a-jdk).

### Hashbang includes

The hashbang include you saw earlier will run Gradle each time you invoke Conveyor to extract config. This approach adds a slight delay to each Conveyor run, because even with the Gradle daemon this process isn't instant, but it does mean your config is always synced. You can also write `include required("generated.conveyor.conf")` and run `gradle writeConveyorConfig` when you change your Gradle build. This avoids any delay from involving Gradle but means your settings can get out of sync. Wrapping the packaging process with a script lets you have the best of both worlds.

### Machine-specific dependencies 

Sometimes you need different versions of a library depending on which OS you use. A good example is when packaging Jetpack Compose apps (see below for an example). The Conveyor plugin provides a simple solution for this in the form of per-machine configurations. The one that matches the host OS is always used, and the others are emitted as config for Conveyor so it can build packages for other operating systems. The available configurations are named `linuxAmd64`, `macAmd64`, `macAarch64` and `windowsAmd64`.

## Adapting a Compose Desktop app

!!! tip
    You can [get the source to an example Compose Desktop based note taking application](https://github.com/hydraulic-software/eton-desktop).

For [Jetpack Compose Desktop](https://www.jetbrains.com/lp/compose-desktop/) apps a bit more work is required. Different Conveyor plugin versions support different Compose Desktop versions:

* For Compose 1.2, use Conveyor plugin `1.4` or higher.
* For Compose 1.0/1.1, use Conveyor plugin `1.0.1`. You'll need to [import a JDK](../../configs/jvm.md#importing-a-jdk) yourself.

If you use the wrong one you'll get a `NoSuchMethodError` exception. Both JVM and Multiplatform Kotlin plugins are supported.

* [ ] Add machine-specific dependencies to the top level of your build file.

```kotlin
dependencies {
    linuxAmd64(compose.desktop.linux_x64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)
}
```

* [ ] Add workarounds for Compose issues.

You'll need to add the following snippet to your build file (translate to Groovy if not using Kotlin Gradle syntax).

```kotlin
// region Work around temporary Compose bugs.
configurations.all {
    attributes {
        // https://github.com/JetBrains/compose-jb/issues/1404#issuecomment-1146894731
        attribute(Attribute.of("ui", String::class.java), "awt")
    }
}

// Force override the Kotlin stdlib version used by Compose to 1.7, as otherwise we can end up with a mix of 1.6 and 1.7 on our classpath.
dependencies {
    val v = "1.7.10"
    for (m in setOf("linuxAmd64", "macAmd64", "macAarch64", "windowsAmd64")) {
        m("org.jetbrains.kotlin:kotlin-stdlib:$v")
        m("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$v")
        m("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$v")
    }
}
// endregion
```
#### Kotlin Multiplatform

If using Kotlin Multiplatform:

* [ ] Add `withJava()` and also ensure you're using the right version of the Kotlin standard library in your project dependencies.

```kotlin
kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }

    sourceSets {
        val jvmMain: KotlinSourceSet by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(compose.desktop.currentOs)
            }
        }
    }
}
```

* [ ] Add `kotlin.mpp.stability.nowarn=true` to your `gradle.properties` file, to silence a warning printed during build configuration time that would interfere with config import otherwise.

### Setting icons

Compose expects to set the window icon itself, rather than having it be taken from the executable or package automatically. You can use code like this to ensure the window icon is always set correctly:

```kotlin
fun main() {
    val version = System.getProperty("app.version") ?: "Development"
    application {
        // app.dir is set when packaged to point at our collected inputs.
        val appIcon = remember {
            System.getProperty("app.dir")
                ?.let { Paths.get(it, "icon-512.png") }
                ?.takeIf { it.exists() }
                ?.inputStream()
                ?.buffered()
                ?.use { BitmapPainter(loadImageBitmap(it)) }
        }

        Window(onCloseRequest = ::exitApplication, icon = appIcon, title = "Conveyor Compose for Desktop sample $version") {
            App()
        }
    }
}
```

and make sure your `conveyor.conf` contains something like this to ensure the PNG files get bundled:

```
app {
  icons = icon.svg
  windows.inputs += TASK/rendered-icons/windows
  linux.inputs += TASK/rendered-icons/linux
}
```

## Adapting a JavaFX app

This is almost exactly like a normal Gradle app except that a bit more work is required for icons. Firstly, bundle the icons into your app as data files:

```
app {
  // We include the PNGs in the Windows and Linux app packages so they can be set as the window icon.
  windows.icons   = "icons/icon-square-*.png"
  windows.inputs += ${app.windows.icons}
  linux.icons     = "icons/icon-rounded-*.png"
  linux.inputs   += ${app.linux.icons}
  mac.icons       = "icons/icon-rounded-*.png"
}
```

And then add code like this in your application class, being sure to invoke `loadIconsForStage(stage)` during startup:

```java
public class App {
    private static void loadIconsForStage(Stage stage) {
        String appDir = System.getProperty("app.dir");
        if (appDir == null)
            return;
        Path iconsDir = Paths.get(appDir);
        try (var dirEntries = Files.newDirectoryStream(iconsDir, "icon-*.png")) {
            for (Path iconFile : dirEntries) {
                try (var icon = Files.newInputStream(iconFile)) {
                    stage.getIcons().add(new Image(icon));
                }
            }
        }
    }
}
```

## Adapting a SWT app

SWT is a small JVM UI toolkit that maps directly to the operating system's native widgets. Here's a Gradle config (in Kotlin syntax) that uses the Conveyor plugin and sets up dependencies as appropriate. This sample also demonstrates how to write a bit of custom Gradle code to minimize repetition when specifying platform specific dependencies:

```kotlin
plugins {
	`java-library`
    application
    id("dev.hydraulic.conveyor") version "1.4"
}

repositories {
    mavenCentral()
}

val swt_version = "3.119.0"

// Add the platform specific SWT dependency to the platform specific dependency configuration.
fun DependencyHandlerScope.swt(platformConveyor: String, platformSwt: String) {
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
