# 8. Explore the integration

In this section you'll learn how to add Conveyor packaging to an existing project by studying how the sample projects are configured.

The template `conveyor.conf` files are small, which is normal. A combination of sensible defaults, automatically derived values and (optionally) config extracted from your build system keeps it easy. Still, there are around 150 different settings available to customize packages if you need them. Consult the configuration section of this guide to learn more about what you can control.

## Native / C++

### conveyor.conf

* [ ] Open `conveyor.conf` in the project root directory. 

The config is defined using a superset of JSON called [HOCON](../configs/hocon-spec.md) with a few [Conveyor-specific extensions](../configs/hocon-extensions.md). It will look roughly like this:

!!! tip
    Click the + icons to learn more about each part.

```javascript title="conveyor.conf"
app {
  display-name = Template App // (1)!
  fsname = template-app // (2)!
  version = 1
  site.base-url = "localhost:8899" // (3)!

  machines = [ 
    windows.amd64, linux.amd64.glibc, mac.amd64, mac.aarch64 // (4)!
  ]  
  
  icons = "icons/icon-rounded*" // (5)!
  windows.icons = "icons/icon-square*"

  mac.inputs = [ // (6)!
    build/mac/installation/bin -> Contents/MacOS
    build/mac/installation/lib -> Contents/Frameworks
  ]

  windows.amd64.inputs = build/win/installation/bin
  linux.amd64.inputs = build/linux/installation
}

conveyor.compatibility-level = 1
```

1. The display name is the natural language name of the project as it appears to the user. It's initialized with a guess based on de-dashifying the `fsname` key.
2. The `fsname` is the name of the project as it appears on disk, e.g. in file names.
3. This is a directory on a web server where packages will look for update files.
4. You can restrict which platforms you support. See ["Machines"](../configs/index.md#machines) for details.
5. The templates come with pre-made icons. You should replace these files with your own. Conveyor will take care of converting to native formats and embedding the icon into the Windows EXE file.
6. The CMake build system produces an install directory that uses non-Mac UNIX conventions. Here, we adapt it to a Mac bundle layout. If your build system produces a `.app` bundle already you can just provide the path of the bundle directory. See Apple's document ["Placing content in a bundle"](https://developer.apple.com/documentation/bundleresources/placing_content_in_a_bundle).

The only complicated thing here is the [inputs](../configs/inputs.md). This config is using Conveyor's ability to change the layout of files in the package as they are copied in.

### CMakeLists

* [ ] Now open the `CMakeLists.txt` file.  

This file defines the build system. It contains various commands, all with comments explaining what they do. The build system demonstrates importing a third party library from a source zip, compiling it, dynamically linking against it, and passing the right linker flags to produce binaries that will work with Conveyor. It looks roughly like this:

```javascript
cmake_minimum_required(VERSION 3.16.3)
project(gl_cmake)

set(CMAKE_CXX_STANDARD 17) // (1)!
set(CMAKE_OSX_ARCHITECTURES arm64;x86_64) // (2)!
set(CMAKE_INSTALL_PREFIX ${CMAKE_BINARY_DIR}/installation) // (3)!

if (APPLE) // (4)!
    set(CMAKE_INSTALL_RPATH "@executable_path/../Frameworks")
    set(CMAKE_EXE_LINKER_FLAGS 
        ${CMAKE_EXE_LINKER_FLAGS} "-Wl,-headerpad,0xFF")  // (5)!
elseif (UNIX)
    set(CMAKE_INSTALL_RPATH "\$ORIGIN/../lib")
endif()

include_directories("include")
add_executable(gl_cmake
        src/main.cpp
        src/gl.c
)

if (APPLE)
    target_link_libraries(gl_cmake PRIVATE "-framework Cocoa") // (6)!
endif()

include(ImportGLFW.txt) // (7)!
target_link_libraries(gl_cmake PRIVATE glfw)

install(TARGETS gl_cmake)
```

1. Use the C++17 standard.
2. When compiling on macOS, cross-compile for Intel and Apple Silicon. The result is a universal fat binary.
3. "Install" to a directory tucked neatly into the build directory. Conveyor will pick up the binaries from this directory later.
4. Ensure libraries can be found. On UNIX systems libraries are stored in a different directory to the executable, and a header in the binary file tells the linker where to look. Unfortunately they are not set by default, so we must instruct the linker to add these headers here. The syntax varies between macOS and Linux. It isn't necessary for Windows where the convention is to put DLLs and EXEs in the same directory.
5. On macOS Conveyor will inject its own library into the binary to initialize the update system. Ensure there is sufficient empty space in the headers to make this possible.
6. On macOS we should also depend on Apple's Cocoa GUI framework, because otherwise Conveyor will think we're not a GUI app and not initialize Sparkle. In this case all the GUI work is being done by GLFW but that's rare, and only because this is such a simple example. Normally you'd need this regardless.
7. Import and compile an open source library. Delete these lines if you don't want to use OpenGL.


??? note "Code injection on macOS"
    Windows and Linux have built-in package managers that can update software automatically, but macOS does not. The only Apple provided way to ship software updates to Mac users is via the App Store. Conveyor doesn't go this route. Instead, it uses the popular [Sparkle Framework](https://sparkle-project.org/) to give your app the ability to update itself. Sparkle is a de-facto standard used across the Mac software ecosystem.

    For Sparkle to work it must be initialized at app startup. To avoid you needing to write Mac specific code in Objective-C or Swift, Conveyor will edit the Mach-O headers of your binary when it builds the bundle to inject a shared library that starts up Sparkle for you. This happens automatically for any app that links against Cocoa or AppKit and not Sparkle. This feature is particularly useful for apps that aren't written in C++, as long as they provide sufficient header padding space. The amount of space left for adding headers can be controlled using Apple's `ld` linker with the `-headerpad` flag. If your language toolchain doesn't support header padding, this technique won't work and you'll have to link against `Sparkle.framework` yourself.

## JVM

* [ ] Open `conveyor.conf` in the project root directory. It's defined using a superset of JSON called [HOCON](../configs/hocon-spec.md) with a few [Conveyor-specific extensions](../configs/hocon-extensions.md). It should look like this:

```javascript title="conveyor.conf"
include "/stdlib/jdk/17/openjdk.conf"   // (1)!
include "#!./gradlew -q printConveyorConfig"  // (2)!

app {
  display-name = My Amazing Project   // (3)!
  site.base-url = downloads.myproject.org/some/path   // (4)!
  
  icons = "icons/icon-square-*.png"   // (5)!
  mac.icons = "icons/icon-rounded-*.png"
}

conveyor.compatibility-level = 1   // (6)!
```

1. You can import JDKs by major version (optionally also the minor version) and by naming a specific distribution. [Learn more](../stdlib/jdks.md).
2. This is a [hashbang include](../configs/hocon-extensions.md#including-the-output-of-external-commands). The given program will be run and the output included as if it were a static HOCON file.
3. You may not need to set this if the display name of your project is trivially derivable from the name of the Gradle project. Use `printConveyorConfig` to see what the plugin guessed.
4. This is where the created packages will look for update metadata.
5. The templates come with pre-rendered icons in both square and rounded rectangle styles. This bit of config uses square by default and rounded rects on macOS only, but that's just a style choice to fit in with the native expectations. You can use whatever icons you like. They should be rendered as PNGs in a range of square sizes, ideally 32x32, 64x64, 128x128 etc up to 1024x1024.
6. This line will be added to a freshly written config if it's missing. Recording the schema/semantics expected by the config allows the format to evolve in future versions without breaking backwards compatibility.


### Gradle projects

You don't have to use any particular build system with Conveyor, but if you use Gradle then config can be extracted from your existing build using a simple plugin. The Gradle plugin doesn't replace or drive the package build process itself: you still do that using the `conveyor` command line tool. The plugin is narrowly scoped to generating configuration and nothing more. If you want Gradle to run Conveyor you can add a normal exec task to do so.

The plugin adds two tasks, `printConveyorConfig` and `writeConveyorConfig`. The first prints the generated config to stdout, and the second writes it to an output file. By default this is called `generated.conveyor.conf` but can be changed.

* [ ] Run `./gradlew -q printConveyorConfig` and examine the output. The plugin can read config from other plugins like the Java application plugin, the Jetpack Compose plugin and the OpenJFX plugin.
* [ ] Open `settings.gradle{.kts}` file. The following bit of code adds support for loading the Gradle plugin:

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
              maven { uri = "https://maven.hq.hydraulic.software" }
          }
    }
    ```

* [ ] Open `build.gradle{.kts}` file. It should apply the Conveyor plugin at the top:

=== "Kotlin"
    ```kotlin title="build.gradle.kts"
    plugins {
        id("dev.hydraulic.conveyor") version "1.0.1"
    }
    ```

=== "Groovy"
    ```groovy title="build.gradle"
    plugins {
        id 'dev.hydraulic.conveyor' version '1.0.1'
    }
    ```

[Get the latest version number and plugins code snippet here](https://plugins.gradle.org/plugin/dev.hydraulic.conveyor).

The hashbang include you saw earlier will run Gradle each time you invoke Conveyor to extract config. This approach adds a slight delay to each Conveyor run, because even with the Gradle daemon this process isn't instant, but it does mean your config is always synced.

You can also write `include required("generated.conveyor.conf")` and run `gradle writeConveyorConfig` when you change your Gradle build. This avoids any delay from involving Gradle but means your settings can get out of sync.

!!! tip
    When iterating on packages use the faster form, and then switch to the slower form when done.

Sometimes you need different versions of a library depending on which OS you use. A good example is when packaging Jetpack Compose apps, which require you to specify which OS you want in the dependency list itself. The Conveyor plugin provides a simple solution for this in the form of per-machine configurations. The one that matches the host OS is always used, and the others are emitted as config for Conveyor so it can build packages for other operating systems. For Compose Desktop apps it looks like this:

```groovy
dependencies {
    linuxAmd64(compose.desktop.linux_x64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)
}
```

### Maven projects

For Maven there's no plugin. Instead Conveyor will discover the JARs in your project by running the output of the `mvn` command and using it directly as configuration. Other aspects like project name must be specified explicitly. Better import from Maven is planned in a future release.

??? warning "Maven on Windows"
    Currently, automatic import from Maven only works on UNIX. On Windows you'll need to follow the instructions below for "other build systems".

??? note "JavaFX apps"
    This framework has special support in the [standard library](../stdlib/javafx.md). Check there to learn what else you'll want to add, or look at the [tasks section](../tasks/index.md).

Here's an example of how to package a Maven project:

```javascript title="conveyor.conf" linenums="1"
include "/stdlib/jdk/17/openjdk.conf"  // (1)!
include "/stdlib/jvm/from-maven.conf"  // (2)!

app {
    fsname = my-program  // (3)!
    display-name = myPROGRAM  // (4)!
    vendor = Global MegaCorp  // (5)!
    version = 1.0
    
    site.base-url = downloads.myproject.org/some/path  // (6)!
    inputs += "icon-*.png"
}
```

1. You can import JDKs by major version, major/minor version, and by naming a specific distribution.
2. This included file contains a single line, which runs Maven, tells it to print out the classpath and assigns the result to the `app.inputs` key: `include "#!=app.inputs[] mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout -Dmdep.pathSeparator=${line.separator}"`. In future this file will be updated to trigger a higher quality Maven import.
3. The `fsname` is what's used for names on Linux e.g. in the `bin` directory, for directories under `lib`. In fact when specified the vendor is also used, and the program will be called `global-megacorp-my-program` unless the `long-fsname` key is overridden.
4. You may not need to set this if the display name of your project is trivially derivable from the fsname. The default here would be `My Program`.
5. This is optional. It'll be prefixed to the display name and used as a directory name  in various places; skip it if you don't work for an organization.
6. This is where the created packages will look for update metadata.

### Other build systems

Create a `conveyor.conf` that looks like this:

```javascript title="conveyor.conf" linenums="1"
include "/stdlib/jdk/17/openjdk.conf"  // (1)!

app {
    inputs += build/jars/my-program-1.0.jar
    inputs += build/jars
    
    icons = "icon-*.png"

    vendor = Global MegaCorp  // (2)!
    
    site.base-url = downloads.myproject.org/some/path  // (3)!
}
```

1. You can import JDKs by major version, major/minor version, and by naming a specific distribution.
2. This is optional. It'll be prefixed to the display name and used as a directory name  in various places; skip it if you don't work for an organization.
3. This is where the created packages will look for update metadata.

This configuration adds your app's main JAR as the first input, allowing package metadata like the main class name, the version number and name to be derived from the file name. Then it adds the directory containing all the app JARs (duplicates are ignored), and finally a set of icon files. That's all you need! The extra inferred configuration will look like this:

```javascript
// This will all be figured out by Conveyor automatically:
app {
    fsname = my-program  // (1)!
    display-name = My Program  // (2)!
    version = 1.0  // (3)!
    
    jvm {
        main-class = com.example.MyProgram  // (4)!
        
        modules = [ ... ]  // (5)!
    }
}
```

1. The `fsname` is what's used for names on Linux e.g. in the `bin` directory, for directories under `lib`. In fact when specified the vendor is also used, and the program will be called `global-megacorp-my-program` unless the `long-fsname` key is overridden.
2. The display name is the human readable brand name. It's generated from the `app.fsname` here by replacing dashes with spaces and re-capitalizing.
3. The version number was taken from the file name of the first input.
4. The main class was read from the MANIFEST.MF of the JAR file. If your JAR isn't actually executable by running `java -jar` or if you have more than one JAR with a main class, then this will fail and you'll need to specify it by hand.
5. The JDK modules to include in the JVM will be inferred by scanning your JARs with the `jdeps` tool, so the JVM will be shrunk automatically.

Sometimes you don't want the settings to be inferred from the first input like that. In this case you can specify the config directly. Look at these sections of the guidebook to learn more:

* [Generic configuration for any kind of app](../configs/index.md)
* [Configuration for JVM apps](../configs/jvm.md)

??? note "File paths"
    Inputs are resolved relative to the location of the config file, not where Conveyor is run from.

??? warning "Uber-jars"
    Don't use an uber/fat-jar for your program unless you're obfuscating. It'll reduce the efficiency of delta download schemes like the one used by Windows. It also means modular JARs won't be encoded using the optimized `jimage` scheme. Use separate JARs for the best user experience.

### Code changes

It's possible to package JVM apps with no code changes at all. However, you will probably want to benefit from a few minor tweaks:

1. The `app.version` system property is set to the value of the `app.version` configuration key. You can use this to avoid duplicating your version number in different places.
2. The `app.dir` system property points at the directory in your package install where input files can be found. Some JARs may be found there, but note that explicitly modular JARs will disappear into the `modules` file in the JVM directory and so you won't find them here. Look up files from those JARs using the standard Java resources API instead.
3. You can [set any other system properties you like in the config](../configs/jvm.md), allowing the app to know at runtime the value of any config values. By extension you can also set system properties to the value of arbitrary programs that were run at build time by using hashbang imports and build system integration.
   

<script>var tutorialSection = 8;</script>
