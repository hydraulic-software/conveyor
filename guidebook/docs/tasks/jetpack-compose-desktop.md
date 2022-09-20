# Jetpack Compose Desktop

Jetpack Compose is the latest Android UI toolkit, developed by Google and ported to the desktop environment by JetBrains.  

## Eton Notes

You can [get the source to an example note taking application](https://github.com/hydraulic-software/eton-desktop).  

## Generate a starter app

Pick a reverse DNS name to identify your app (e.g. `io.github.username.projectname`) and run `conveyor generate compose` to create a new Jetpack Compose app from scratch. It will come with a Conveyor configuration and Gradle integration all set up.   

## Icons

Compose expects to set the window icon itself, rather than having it be taken from the executable or package automatically. You can use code like this to ensure the window icon is always set correctly:

```kotlin
fun main() {
  singleWindowApplication(
        title = "Example app version ${System.getProperty("app.version")}",
        icon = appIcon
  ) { /* .... */ }
}

private val appIcon: Painter? by lazy {
    // app.dir is set when packaged to point at our collected inputs.
    val appDirProp = System.getProperty("app.dir")
    val appDir = appDirProp?.let { Path.of(it) }
    // On Windows we should use the .ico file. On Linux, there's no native compound image format and Compose can't render SVG icons,
    // so we pick the 128x128 icon and let the frameworks/desktop environment rescale. On macOS we don't need to do anything.
    var iconPath = appDir?.resolve("app.ico")?.takeIf { it.exists() }
    iconPath = iconPath ?: appDir?.resolve("icon-square-128.png")?.takeIf { it.exists() }
    if (iconPath?.exists() == true) {
        BitmapPainter(iconPath.inputStream().buffered().use { loadImageBitmap(it) })
    } else {
        null
    }
}
```

## conveyor.conf

```hocon
include required("/stdlib/jvm/enhancements/client/v1.conf")
include "#!./gradlew -q printConveyorConfig"

app {
  display-name = Sample app
  fsname = sample-app
  site.base-url = "localhost:8899"

  icons = "icons/icon-square-*.png"
  mac.icons = "icons/icon-rounded-*.png"
  site.icons = icons/icon-rounded-256.png
}
```

## build.gradle.kts

```kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    kotlin("jvm") version "1.6.20"
    id("org.jetbrains.compose") version "1.2.0-beta01"
    id("dev.hydraulic.conveyor") version "1.1"
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
    // Add the Compose Desktop libraries specific to each platform to machine-specific dependency configurations.
    // The one that matches the current Gradle build host will be added to 'implementation' automatically, so this is sufficient.
    linuxAmd64(compose.desktop.linux_x64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)
}
```
