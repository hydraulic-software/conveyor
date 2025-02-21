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

1. This is a [hashbang include](../../configs/hocon.md#including-the-output-of-external-commands). The given program will be run and the output included as if it were a static HOCON file.
2. You may not need to set this if the display name of your project is trivially derivable from the name of the Gradle project. Use `printConveyorConfig` to see what the plugin guessed.
3. This is where the created packages will look for update metadata.
4. The templates come with pre-rendered icons in both square and rounded rectangle styles. This bit of config uses square by default and rounded rects on macOS only, but that's just a style choice to fit in with the native expectations. You can use whatever icons you like. They should be rendered as PNGs in a range of square sizes, ideally 32x32, 64x64, 128x128 etc up to 1024x1024.

Many settings are missing (e.g. `app.rdns-name`) because they'll be read from your build configuration.

## Gradle plugin

To add the plugin:

=== "Kotlin"
    ```kotlin title="build.gradle.kts"
    plugins {
        id("dev.hydraulic.conveyor") version "1.12"
    }
    ```

=== "Groovy"
    ```groovy title="build.gradle"
    plugins {
        id 'dev.hydraulic.conveyor' version '1.12'
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

## Adapting a Compose Multiplatform app

!!! tip
    Use the built-in template apps as a guide by running `conveyor generate compose my-sample-project` and looking at how it's set up.

A default, non-Conveyorized Kotlin Compose Multiplatform app can be generated using the [KMP wizard](https://kmp.jetbrains.com/). You should use Compose 1.2 or higher.

Add `id("dev.hydraulic.conveyor") version "<<version>>"` to the `plugins` section of the `composeApp/build.gradle.kts` file ([locate the latest version here](https://plugins.gradle.org/plugin/dev.hydraulic.conveyor)).

Set `version = "1.0"`, or whatever your app version is, at the top level of the build file. Conveyor needs the module to be versioned, setting a version number elsewhere isn't enough.

Locate the line that says `jvm("desktop")` and underneath it add this fragment to set the JDK (you can use any JDK version and vendor):

```
jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
    vendor.set(JvmVendorSpec.JETBRAINS)
}
```

Find the _top level_ `dependencies` block (not the one inside the `kotlin{}` block), or add one if it's missing, and add this:

```
dependencies {
    // Use the configurations created by the Conveyor plugin to tell Gradle/Conveyor where to find the artifacts for each platform.
    linuxAmd64(compose.desktop.linux_x64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)
}
```

There might already be a dependency `debugImplementation(compose.uiTooling)` in this block, in which case it's fine to leave it.

If you have a `packageName = "example-project"` line inside a `nativeDistributions{}` block, ensure it _isn't_ a reverse DNS name but rather
an "fsname", i.e. the name you want to appear in file names.

Finally, add this at the bottom of your `build.gradle.kts` file:

```
// region Work around temporary Compose bugs.
configurations.all {
    attributes {
        // https://github.com/JetBrains/compose-jb/issues/1404#issuecomment-1146894731
        attribute(Attribute.of("ui", String::class.java), "awt")
    }
}
// endregion
```

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
    id("dev.hydraulic.conveyor") version "1.12"
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
