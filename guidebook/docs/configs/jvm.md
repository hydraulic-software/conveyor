# JVM apps

## Overview

Conveyor has integrated support for apps that run on the JVM (for any supported language). You get the following:

* A custom launcher that replaces the `java` command and adds [extra features](#launcher-features).
* Support for the module system:
    * Usage of `jlink` and `jdeps` to create minimal JVM distributions, containing only the modules that your app needs.
    * Fully modular JARs are detected and linked into the `modules` file, yielding faster startup and smaller downloads.
* Native libraries are extracted from JARs so they can be either signed, or discarded if they're for the wrong OS/CPU.
* [Maven and Gradle integration](maven-gradle.md):
    * A Gradle plugin that automatically generates configuration snippets.
    * Maven projects can have their classpath read directly, without needing a plugin.
* Integrated support for GUI frameworks like JavaFX and Compose Desktop.

To package an app that uses the JVM, you must choose a JDK and at least one JAR.

??? note "Java versions"
    Conveyor only supports apps that use Java 11 or later - Java 8 won't work. All classes must be inside a JAR. If you need Java 8 support
    please [email contact@hydraulic.software](mailto:contact@hydraulic.software).

## Synopsis

```properties
# Add some app JARs to the classpath, taken from build/libs relative to the config file.
app.inputs += "build/libs/*.jar"

# Add the latest Java 17 JDK distributed by OpenJDK.
import required("/stdlib/jdk/17/openjdk.conf")

# Or define a custom JDK locally.
basedir = my-jdk-dir/myjdk-17.0
app.jvm {
  linux.amd64.inputs += ${basedir}-linux-x64.tar.gz
  linux.amd64.muslc.inputs += ${basedir}-alpine-linux-x64.tar.gz
  linux.aarch64.inputs += ${basedir}-linux-aarch64.tar.gz
  windows.amd64.inputs += ${basedir}-windows-x64-jdk.zip
  mac.amd64.inputs += ${basedir}-macosx-x64.tar.gz
}

# Request extra modules that weren't auto-detected by jdeps.
app.jvm.modules += java.{desktop,logging,net.http}

# Set the main GUI class.
app.jvm.gui = com.foobar.Main

# Add JVM arguments for every launcher.
app.jvm.options += -Xmx1024m
app.jvm.windows.options += -Xss4M
app.jvm.mac.aarch64.options += -Xss4M

# Set system properties.
app.jvm.system-properties {
	mylib.nativeLibPath = <libpath>
}

# Plumb the app version through to the app using constant command line arguments.
app.jvm.constant-app-arguments = [ --app-version, ${app.version} ]

# Add a supplementary CLI tool that'll be a part of the same package.
# It'll be named foo-tool[.exe] based on the class name. Any Kt suffix is stripped.
app.jvm.cli = [ com.foobar.FooTool ]

# You can control the name:
app.jvm.cli.foo-cli.main-class = com.foobar.FooTool

# And have more than one.
app.jvm.cli {
	foo-cli.main-class = com.foobar.FooTool
	foo-dump.main-class = com.foobar.FooDumper
}

# And control some of the settings for each one independently.
app.jvm.cli.foo-cli {
    main-class = com.foobar.FooTool
    # Set an explicit class path. You normally never need this because the
    # default of *.jar is good enough. 
    class-path = "some-prefix-*.jar"
    # JVM options added to the global list.
    options = [ -Xmx500M ]
}
```

## Keys

**`app.jvm`** An [input hierarchy](inputs.md) in the same manner as the top level `app` object. The inputs will be resolved (copied/downloaded/extracted) and examined for a `jmods` directory. If a `jmods` directory is found then the contents will be used to create a JVM that contains only the code your application needs, using the `jlink` tool. If you have jmods to add to the jlinked image (e.g. JavaFX), you should add them here alongside the JVM itself. If there is no `jmods` directory then the JDK is used as-is and jlinking isn't done. 

**`app.jvm.gui`** The GUI launcher. See the [launchers section](#launchers).

**`app.jvm.cli`** CLI launchers. See the [launchers section](#launchers).

**`app.jvm.constant-app-arguments`** A list of arguments that will always be passed to the app in addition to whatever the user specifies. Can be useful to plumb metadata from the app definition through to the app itself, like by telling it its own version number.

**`app.jvm.system-properties`** A map of system properties. The default properties are:

* `app.dir` - points at the install directory where input files are placed.
* `app.displayName` - equal to the `${app.display-name}` key.
* `app.version` - equal to the `${app.version}` key.
* `app.revision` - equal to the `${app.revision}` key.
* `app.vendor` - equal to the `${app.vendor}` key.

Some special tokens are supported. See [JVM options](#jvm-options) for details.

If you include [the client enhancements config from the standard library](../stdlib/jvm-clients.md), you also get:

* `jna.nosys` - set to false, which makes JNA work in the packaged environment.
* `picocli.ansi` - set to `tty`, to make sure PicoCLI always uses colors even on Windows.
* `java.net.useSystemProxies` - set to true, to make the JVM use the system HTTP proxy settings.

**`app.jvm.options`** See [JVM options](#jvm-options) 

!!! warning
    * Watch out for accidental mis-use of HOCON syntax when writing something like `constant-app-arguments = [ --one --two ]`. This creates a _single_ argument containing "--one --two" which is unlikely to be what you want. Instead, write `constant-app-arguments = [ --one, --two ]` or put each argument on a separate line. Conveyor will warn you if you seem to be doing this.

!!! important
    When a JVM app is signed on macOS or Windows the JVM attach mechanism is disabled using the `-XX:+DisableAttachMechanism` flag. That's because the attach mechanism allows any local user to overwrite the app's code in memory without needing to alter files on disk, thus defeating code signing.
    
    As a consequence debuggers and profilers won't be able to find a signed JVM app, by design.
    
    This rule doesn't apply on Linux because that platform doesn't use code signing in the same way. On macOS the OS forbids debugger attachment unless the app opts in to allowing this, thus apps cannot tamper with each other's memory even when running as the same user. On Windows anti-virus checks are done when code is loaded, and so programs that allow arbitrary code injection allow lateral movement by malware.

**`app.jvm.modules`** List of modules to take from the underlying JDK for the usage of classpath JARs. The modules and their transitive dependencies will be included, all others will be dropped. Defaults to `[ detect ]`. The special entry `detect`  is replaced with modules detected using the `jdeps` tool (see below). Note that this is *not* the place to list modular JARs in your app - it's only for modules to take from the JDK or any other supplied JMODs. Modular JARs should be added to base inputs like any other JARs.

**`app.jvm.jlink-flags`** Extra flags passed to the `jlink` command. Can be used to invoke plugins and so on. See the output of `jlink --help` and `jlink --list-plugins` to see what's available. By default this adds `--ignore-signing-information` and specifies a jimage file ordering, which helps with startup time.

**`app.jvm.mac.plist`** HOCON structure converted to the `Info.plist` file used for the linked JVM on macOS. You can normally ignore this.

**`app.jvm.strip-debug-info`** If true (defaults to false) then JVM classfile debug attributes are stripped during repacking.

**`app.jvm.unwanted-jdk-files`** A list of file names that are erased from the application after jlinking and launcher creation is done. This is useful for cleaning up files that are usually only needed for development purposes. You can remove default items from the list by prefixing them with a `-` (minus). The list defaults to: 

```
unwanted-jdk-files = [
    # This helps other tools figure out what the JVM version 
    # is, but it isn't needed by apps.
    release

    # This contains JVM flags but we burn them into the launcher 
    # to ensure they're covered by code signing, and for startup 
    # time wins.
    lib/jvm.cfg

    # This is used for AppCDS but that isn't used by Conveyor yet.
    lib/classlist

    # This is used for third party apps that are reflecting/reading,
    # the contents of the linked JDK. It isn't used in normal operations.
    lib/jrt-fs.jar

    # JLI is the Java Launcher Infrastructure or something like that. 
    # It provides some code for the `java` command, but we use our own
    # native launcher so don't need this file.
    bin/jli.dll
    lib/libjli.so
    lib/libjli.dylib

    # File used only when compiling against jvm.dll, not needed for 
    # end users.
    lib/jvm.lib
]
```

**`app.jvm.extract-native-libraries`** If true (the default) then native libraries will be deleted from JARs and placed in the lib directory of the bundled JVM. If false, they are left alone. [See below](#jar-stripping) for more information.

## Importing a JVM/JDK

These days there are many distributions of Java to choose from. You've probably already chosen your JVM/JDK for development purposes, but you'll need to specify which one you want so Conveyor can download the different versions for different platforms.

The standard library has many snippets for different distributions and versions of the JVM. [Learn what's available](../stdlib/jdks.md) or run `conveyor jdk-table`.

You can also easily add your own custom JVM by just adding the right inputs:

```hocon
app {
  jvm {
    # Directories that contain the unpacked JDKs.
    linux.amd64.glibc.inputs += my-jdk/linux/amd64
    mac.amd64.inputs += my-jdk/mac/amd64
    windows.amd64.inputs += my-jdk/windows/amd64
  }
}
```

## JAR stripping

The JAR files that get shipped are rewritten in three ways:

1. Native shared/JNI libraries are moved out of the JAR.
2. The metadata of the files is canonicalized to eliminate non-determinism that would otherwise reduce the efficiency of update mechanisms.
3. Debug info is (optionally) stripped.

Therefore, your software should always attempt to load shared libraries by using `System.loadLibrary` first, before trying to extract native libraries from a JAR. If you use a library that doesn't then try the following options:

1. File a bug against the upstream library asking them to use `System.loadLibrary` only try to extract libs if they catch an exception. This is a pretty standard way to avoid problems when packaging. 
2. If there's a system property you can set you can use the `<libpath>` token, e.g. `app.jvm.system-properties.fooLib.jniPath = <libpath>`. At runtime this will become the path to where the extracted native libraries can be found.
3. You can set `app.jvm.extract-native-libraries = false` to disable this behaviour but then you're likely to hit the fact that the files
   won't be signed, and Apple's notarization servers will detect them and reject the app. So you will have to pre-process the JAR to replace shared libraries with the signed versions.

Moving native libraries out of JARs has these benefits:

* On Windows and macOS the security systems want to see that all native code is signed. Libraries hidden inside JARs would not be signed by the regular processes and on macOS this can result in the OS refusing to load them.
* Unpacking shared libraries improves startup time. 
* It improves the effectiveness of update delta compression.
* It reduces download sizes by deleting libraries meant for other operating systems or CPU architectures.

## Modules

Conveyor can use the Java Platform Module System (JPMS a.k.a. Jigsaw).

**Linked JVMs.** Conveyor always bundles a specialized JVM created using `jlink`. That's why Java 8 isn't supported (if you need support for Java 8 please [let us know](mailto:contact@hydraulic.software)). Linking is primarily a size and startup time optimization: it gets rid of parts of the JDK you don't need, and linked modules get incorporated into the single `modules` file which uses an more optimized format called "jimage". Classes can be loaded more quickly and take up less space when processed this way. You can use the `jimage` command line tool in any JDK to view the contents of a `modules` file.

**Modular JARs.** Conveyor will link a JAR that provides a `module-info.class` into the bundled JVM as long as it doesn't depend on any auto-modules and isn't excluded (see below). As a consequence those JARs won't be found in the app data directory, only JARs on the classpath will be placed there.  Conveyor always puts automatic modules (those that declare a module name in their manifest) as ordinary classpath JARs.

**Automatic dependency detection.** A modern JDK comes with many modules that your app probably doesn't use. The `jdeps` tool tries to figure out what JDK modules a non-modular JAR needs by doing static analysis of the bytecode. Conveyor uses `jdeps` automatically whenever it finds an entry of `detect` in the `app.jvm.modules` list, and the default list contains only `detect`.

You may need to correct the automatically inferred module path in some cases:

1. If a JDK module is missed because it's only accessed by reflection/ServiceLoader, or by languages that aren't bytecode languages.
2. If a JDK module is pulled in but isn't really required by your app, typically because some obscure feature of a library you use happens to need it but you never actually use that library feature.
3. If your app contains code that violates module boundaries, and you don't want to fix it by adding `--add-opens` flags to the JVM options.

As an example of point (2) the popular JNA library will pull in all of Swing, because it has a utility function to get the native window handle of an AWT window. If you don't use Swing then this is just pointless bloat.

Problems are easy to fix. If a module is missed, just add it to the `app.jvm.modules` list. If you want to get rid of a JDK module or move a module onto the classpath, add the module name prefixed with `-` like this:

```
app {
	jvm {
		# Add support for elliptic curve cryptography.
		modules += jdk.crypto.ec
		
		# Delete the SQL module.
		modules += -java.sql
		
		# Move GSON onto the classpath and off the module path.
		modules += -com.google.gson
	}
}
```

Note that you can't delete a module non-optionally required by an explicit module - this will just be ignored. The JVM wouldn't start up otherwise.

!!! warning "Gradle modularity bugs"
    Starting from version 7 Gradle will automatically run and compile your app with modules on the module path. As of December 2022 there is [a bug](https://github.com/gradle/gradle/issues/19587) such that sometimes Gradle won't notice a JAR is a JPMS module even when it is. In this case Conveyor may place more JARs onto the module path than occurs in your unit tests or Gradle `run` tasks. Although this won't matter if all your code (including libraries) is respecting module boundaries, you may need to align Conveyor with Gradle by adding some negative entries to the modules list to push modular JARs back onto the classpath.

**Module system flags.** If non-reflective usage of internal APIs is statically detected you'll get a warning and the necessary `--add-opens` flags will be passed. If you need to access internal APIs that aren't detected by jdeps, you'll need to add the necessary `--add-opens` flags yourself in the `app.jvm.options` key. If you add an inferred flag then that will suppress the warning.

When adding module related flags to the `app.jvm.options` key, be aware that there must not be a space between the flag name and value. Although that works with the normal Java launcher, the special launcher used by Conveyor-packaged apps requires them in native JVM form. Write e.g. `app.jvm.options += "--add-modules=jdk.incubator.foreign"` and not `app.jvm.options += --add-modules jdk.incubator.foreign`.

**Diagnosing issues.** If you'd like to see what decisions Conveyor has made about your app, you can make the `processed-jars` task for some machine and look at the files called `required-jdk-modules.txt` (this is the output of jdeps run over your jars) and `modular-jars.txt` which is a list of the JARs that will be linked in to the optimized JVM.

## Launchers

JVM apps are started by a native executable that Conveyor supplies and customizes during the build (see below). You can have one GUI launcher and zero or more supplementary command line launchers. The GUI launcher is the one executed when the user starts the app via the start menu, by running the bundle on macOS (e.g. via Mission Control or the Finder) or using the desktop environment on Linux. CLI launchers can only be executed from the terminal, or by your own software. You're limited to a single GUI launcher because macOS doesn't normally have an installation concept, so the icon used to launch the software is the same as the directory that contains it. This makes it awkward to have more than one GUI entry point per app and it's not conventional to do so on that platform.

Launchers are defined using keys. If no launchers are defined then the JARs will be scanned to find a main class advertised in the manifest  and that will become the GUI launcher. If more than one JAR advertises a main class, an error is reported. As such, for most apps you can simply ignore launchers and let Conveyor figure it out.

The GUI launcher is defined by **`app.jvm.gui`** and CLI launchers are defined using the **`app.jvm.cli`** list/map key. A launcher is defined by a config object, but there are shorthand syntaxes in which the contents of the object are inferred.

### Launcher objects

A fully defined launcher uses the following object:

```hocon
{
  main-class = com.foobar.FooTool
  exe-name = foo-tool
  class-path = "some-prefix-*.jar"
  options = [ -Xmx500M ]   
  console = true
}
```

For each launcher you can specify these keys:

**`main-class`** The fully qualified name of a class with a static main method, or for JavaFX apps, that inherits from `javafx.application.Application`.  

**`exe-name`** The name of the binary executable on disk. You don't need a `.exe` suffix, one will be added for you on Windows. If not specified, will use either the display name or the fsname, depending on platform conventions. 

**`class-path`** A list of file names or globs that select a set of JAR files from the inputs. Defaults to `*.jar` which is usually good enough. Note that explicit JPMS modules don't have to be specified here, as they will be jlinked into the distribution JVM and thus are always available.

**`console`** Controls whether the launcher uses console mode on Windows. See the [documentation for the console key](windows.md#console-key) for more details. If not specified, defaults to true for CLI launchers and false for the GUI launcher. You should normally never need to set this, but it may be helpful in some cases if you have CLI launchers that aren't meant to be invoked by the end user directly. Note that setting console = false will suppress the terminal window from popping up, but won't make the app appear in the start menu. You can only have one entry in the start menu.

### JVM options

JVM command line flags ("options") can be used to control heap size, the garbage collector and so on. Note that this mostly but not entirely the same as arguments to the `java` program. Keys for setting JVM options are arranged in a hierarchy with less specific keys being used as defaults for more specific keys. Each launcher can define:

* **`options`**
* **`windows.options`**
* **`windows.amd64.options`**
* **`mac.options`**
* **`mac.amd64.options`**
* **`mac.aarch64.options`**
* **`linux.options`**
* **`linux.amd64.options`**
* **`linux.amd64.glibc.options`**

Each launcher always includes JVM options defined by the `app.jvm` object, i.e. you can also set options by configuring:

* **`app.jvm.options`**
* **`app.jvm.windows.options`**
* **`app.jvm.windows.amd64.options`**
* ... etc ...

In this way you can set JVM flags for every entry point in your app whilst also specifying options that apply only to specific platforms and launchers.

The options to set `app.jvm.system-properties` are added automatically. Options support two special tokens:

* `&&` - this is replaced by the path to the directory where the program executable is found.
* `<libpath>` - this is replaced by the path to where JNI libraries are placed. This can be useful for dealing with libraries that don't
  call `System.loadLibrary` before attempting to extract JNI libs to the user's home directory, but do support overriding the behavior
  with a system property.

### Defining launchers

You can define them in several ways, depending on how many defaults you accept:

```hocon
# Main class for the GUI launcher. The executable name is either 
# the ${app.fsname} or ${app.display-name} depending on OS.
app.jvm.gui = com.example.FooBar

# Define the full launcher object:
app.jvm.gui {
  # ...
}

# A single CLI launcher. The exe name is set to be the fsname.
app.jvm.cli = com.example.FooBar

# A list of main classes. The executable name is the main 
# class name converted to kebab-case like this:
#
# - com.example.FooBar     = foo-bar[.exe]
# - com.example.FooBarKt   = foo-bar[.exe]    (for kotlin users)
# - com.example.Foo$BarApp = bar-app[.exe]
app.jvm.cli = [ com.example.FooBar, com.example.AnotherApp ]

# An object in which the keys are the executable names and the 
# bodies are launchers:
app.jvm.cli {
  app1 {
    main-class = com.example.FooBar
    options = [ -Xmx500M ]
  }
  app2 {
    main-class = com.example.AnotherApp
  }
}
```

### Launcher features

JVM apps packaged with Conveyor use a custom native program to start the JVM. It adds the following features:

* Initializes the Sparkle update engine on macOS.
* Improved command line support for Windows:
    * ANSI escapes (colors) are enabled, and [the popular PicoCLI framework](https://www.picocli.info) is automatically configured to use them. Modern versions of Windows have much better terminal support than in the past, along with [a modern tabbed terminal emulator](https://www.microsoft.com/en-us/p/windows-terminal/9n0dx20hk701#activetab=pivot:overviewtab) that's also [open source](https://github.com/microsoft/terminal).
    * The console and default character encoding are both set to UTF-8 by default. This matches Linux and macOS, and means that drawing with Unicode symbols and emoji should work. [See here for details](index.md#character-encodings).
    * The `$HOME` environment variable is set to the user's home directory (normally called `%USERPROFILE%` on Windows). This makes it easier to use native code.
* Exceptions that escape `main` are shown in a GUI alert box on macOS and Windows.
* Supports hard-wired command line arguments set from the build configuration, e.g. to tell the app its own version.
* Improves security by ensuring JVM configuration options are signed, and disabling features that can be used to inject code into a running program. This makes it harder for malware to use your app to subvert code signing.
* On Windows, `%APPDATA%` and `%LOCALAPPDATA%` are rewritten to point to the private containerized locations used by MSIX packaging. This means files you create under these directories will be findable by other apps you start, rather than shielded from them as is the default.
* Exposes the app's install location via the `app.dir` system property.

The launcher supports some of the same features as the java launcher, for example JavaFX apps don't need a main method, the Mac specific `-XstartOnFirstThread` flag is understood and the initial stack size can be set.

!!! note "Future features"
    The custom launcher enables many useful features to be added in the future. Ideas include startup time optimization via automatically
    configured [AppCDS](https://openjdk.java.net/jeps/310), exposing APIs to control and monitor the update process, integrating
    NodeJS and using JavaScript modules through it, automatically moving apps to `/Applications` on macOS and regularizing how file/URL
    open requests are exposed to the OS (which currently requires operating-system specific approaches and APIs).

## ProGuard / obfuscation

The output of tools like ProGuard or R8 can be straightforwardly integrated with Conveyor. At this time, Conveyor won't run these tools for you. You should use your build system to do it instead.

Using ProGuard with Jetpack Compose will require a bit of custom config. `./gradlew proguardReleaseJars` runs ProGuard and put all the JARs for the app in the `build/compose/tmp/main-release/proguard/` directory. The Conveyor Gradle plugin doesn't know about that (yet) and will create config that points to the original jars in your Gradle cache, but it's easy enough to fix. If you run `./gradlew printConveyorConfig` you'll see what it's generating and it's all pretty straightforward. Our goal is to replace the inputs it emits with config that points at ProGuard's output, except for the JAR that provides Skiko for each OS. So something like this should work:

```
gradle-cache = ${env.HOME}/.gradle    # Note: UNIX specific config!

app {
    # Import all the obfuscated JARs, except the JAR that contains the platform native graphics code.
    inputs = [{
      from = build/compose/tmp/main-release/proguard
      remap = [
          "**"
          "-skiko-awt-runtime-*.jar"
      ]
    }]

    # Put the dropped JAR back with the right version for each platform. 
    windows.amd64.inputs = ${app.inputs} [ ${gradle-cache}/caches/modules-2/files-2.1/org.jetbrains.skiko/skiko-awt-runtime-windows-x64/0.7.34/1a302b2d58bdf6627446a94098672ab982b90fd0/skiko-awt-runtime-windows-x64-0.7.34.jar ]
    mac.amd64.inputs = ${app.inputs} [ ${gradle-cache}/caches/modules-2/files-2.1/org.jetbrains.skiko/skiko-awt-runtime-macos-x64/0.7.34/466356827dcdb20c4202fa280ff95c41b215313/skiko-awt-runtime-macos-x64-0.7.34.jar ]
    mac.aarch64.inputs = ${app.inputs} [ ${gradle-cache}/caches/modules-2/files-2.1/org.jetbrains.skiko/skiko-awt-runtime-macos-arm64/0.7.34/3f7fe53a3c9c0c96dbe07c22ba4a38234ff13487/skiko-awt-runtime-macos-arm64-0.7.34.jar ]
    linux.amd64.inputs = ${app.inputs} [ ${gradle-cache}/caches/modules-2/files-2.1/org.jetbrains.skiko/skiko-awt-runtime-linux-x64/0.7.34/60f05c42be49b3ba7fc98173f4f6e80c3b9de4fd/skiko-awt-runtime-linux-x64-0.7.34.jar ]
}
```

The Skiko runtime paths here come from running `./gradlew printConveyorConfig` and looking at the output. You'd need to update them when switching to a new Jetpack Compose version.

!!! note "Machine specific configs"
    If you're using the Conveyor Gradle plugin's machine specific configs then this won't work because you'll overwrite the machine-specific inputs lists. So in these more advanced cases you may need to post-process the generated config, or write your own Gradle task to generate it.  
