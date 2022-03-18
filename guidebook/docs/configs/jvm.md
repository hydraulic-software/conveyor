# JVM apps

## Overview

Conveyor has integrated support for apps that run on the JVM (for any supported language). You get the following features:

* A custom launcher that replaces the `java` command and adds [extra features](#launcher).
* Full support for the module system:
    * Usage of `jlink` and `jdeps` to create minimal JVM distributions, containing only the modules that your app needs.
    * Fully modular JARs are detected and linked into the `modules` file, yielding faster startup and smaller downloads.
* Native libraries are extracted from JARs so they can be either signed, or discarded if they're for the wrong OS/CPU. This improves download times.
* [Maven and Gradle integration](maven-gradle.md):
    * A Gradle plugin that automatically generates configuration snippets.
    * Maven projects can have their classpath read directly, without needing a plugin.
* Integrated support for GUI frameworks like JavaFX and Compose Desktop.

To package an app that uses the JVM, you must choose a JDK and at least one JAR.

??? note "Java versions"
    Conveyor only supports apps that use Java 11 or later - Java 8 won't work. All classes must be inside a JAR. If you need Java 8 support
    please [let us know](mailto:contact@hydraulic.software).

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

# Add a system property and max heap size.
app.jvm.options = ${app.jvm.options} [
    -Xmx1024m
    -Dfoo.bar=baz
]

# Plumb the app version through to the app using constant command line arguments.
app.jvm.constant-app-arguments = [ --app-version, ${app.version} ]
```

## Keys

**`app.jvm`** An [input hierarchy](inputs.md) in the same manner as the top level `app` object. The inputs will be resolved (copied/downloaded/extracted) and any JMOD files anywhere in the result will be found and aggregated. Then a JVM will be created using those jmods and the jlink tool. As a consequence, if you have JMODs to add to the jlinked image (e.g. JavaFX), you should add them here alongside the JVM itself.

**`app.jvm.gui`** The GUI entry point. If set to a string then it's the main class invoked when the app is started via the operating system GUI (e.g. start menu, Mission Control, etc). If left blank the JARs will be scanned to find a main class advertised in the manifest. If more than one JAR advertises a main class, an error is reported.

**`app.jvm.constant-app-arguments`** A list of arguments that will always be passed to the app in addition to whatever the user specifies. Can be useful to plumb metadata from the app definition through to the app itself, like by telling it its own version number.

**`app.jvm.options`** A list of arguments to pass to the JVM on startup. Useful for configuring max heap size and system properties, amongst other things. Within JVM options the `&&` token is special and will be replaced at startup with the path to the directory where the app's root input files are stored (there may be other files in there as well). By default, the `app.dir` system property points at `&&`.

!!! warning
    * Watch out for accidental mis-use of HOCON syntax when writing something like `jvm-arguments = [ --one --two ]`. This creates a _single_ argument containing "--one --two" which is unlikely to be what you want. Instead write `jvm-arguments = [ --one, --two ]` or put each argument on a separate line. Conveyor will warn you if you seem to be doing this.

!!! important
    When a JVM app is signed on macOS or Windows the JVM attach mechanism is disabled using the `-XX:+DisableAttachMechanism` flag. That's because the attach mechanism allows any local user to overwrite the app's code in memory without needing to alter files on disk, thus defeating code signing.
    
    As a consequence debuggers and profilers won't be able to find a signed JVM app, by design.
    
    This rule doesn't apply on Linux because that platform doesn't use code signing in the same way. On macOS the OS forbids debugger attachment unless the app opts in to allowing this, thus apps cannot tamper with each other's memory even when running as the same user. On Windows anti-virus checks are done when code is loaded, and so programs that allow arbitrary code injection allow lateral movement by malware.


**`app.jvm.modules`** List of modules to take from the underlying JDK for the usage of classpath JARs. The modules and their transitive dependencies will be included, all others will be dropped. Defaults to `[ detect ]`. The special entry `detect`  is replaced with modules detected using the `jdeps` tool (see below). Note that this is *not* the place to list modular JARs in your app - it's only for modules to take from the JDK itself. Modular JARs should be added to base inputs like any other JARs.

**`app.jvm.jlink-flags`** Extra flags passed to the `jlink` command. Can be used to invoke plugins and so on. See the output of `jlink --help` and `jlink --list-plugins` to see what's available. By default this adds `--ignore-signing-information` and specifies a jimage file ordering, which helps with startup time.

**`app.jvm.mac.plist`** HOCON structure converted to the `Info.plist` file used for the linked JVM on macOS. You can normally ignore this.

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

## Native libraries

The JAR files that get shipped are rewritten in two ways:

1. Native shared/JNI libraries are moved out of the JAR.
2. The metadata of the files is canonicalized to eliminate non-determinism that would otherwise reduce the efficiency of update mechanisms.

Moving native libraries out of JARs has these benefits:

* On Windows and macOS the security systems want to see that all native code is signed. Libraries hidden inside JARs would not be signed by the regular processes and on macOS this can result in the OS refusing to load them.
* Unpacking shared libraries improves startup time. 
* It improves the effectiveness of update delta compression.
* It reduces download sizes by deleting libraries meant for other operating systems or CPU architectures.

Therefore your software should always attempt to load shared libraries by using `System.loadLibrary` first, before trying to extract native libraries from a JAR. Alternatively you can use `System.load` in combination with the `java.home` system property but remember to add either `lib` on UNIX or `bin` on Windows.

## Modules

Conveyor can use the Java Platform Module System (JPMS a.k.a Jigsaw).

**Linked JVMs.** Conveyor always bundles a specialized JVM created using `jlink`. That's why Java 8 isn't supported (if you need support for Java 8 please get in touch). Linking is primarily a size and startup time optimization: it gets rid of parts of the JDK you don't need, and linked modules get incorporated into the single `modules` file which uses an more optimized format called "jimage". Classes can be loaded more quickly and take up less space when processed this way. You can use the `jimage` command line tool in any JDK to view the contents of a `modules` file.

**Modular JARs.** Conveyor will link a JAR that provides a `module-info.class` into the bundled JVM as long as it doesn't depend on any auto-modules. As a consequence those JARs won't be found in the app data directory - only JARs on the classpath will be placed there.  Conveyor always puts automatic modules (those that declare a module name in their manifest) as ordinary classpath JARs. At this time you cannot control whether modules are linked or placed on the classpath, except by pre-processing JARs to add or remove `module-info.class` files. So: Explicit modules are always put on the module path and linked, other JARs never are.

**Automatic dependency detection.** A modern JDK comes with many modules that your app probably doesn't use. The `jdeps` tool tries to figure out what JDK modules a non-modular JAR needs by doing static analysis of the bytecode. Conveyor uses `jdeps` automatically whenever it finds a pseudo-module called `detect` in the `app.jvm.modules` list, and the default list contains only `detect`.

JDeps will look for both usage of JDK modules, and also internal packages. The output of `jdeps` _is a guess_. You may need to correct these types of mistake:

1. It might miss the usage of modules if they're only accessed by reflection/ServiceLoader, or by languages that aren't bytecode languages.
2. It might pull in modules that you don't actually need, because some obscure feature of a library you use happens to need it but you never actually use that library feature.
2. It might not notice the usage of internal APIs, if done via reflection.

As an example of point (2) the popular JNA library will pull in all of Swing, because it has a utility function to get the native window handle of an AWT window. If you don't use Swing then this is just pointless bloat.

Both problems are easy to fix. If a module is missed, just add it to the `app.jvm.modules` list. If you know you won't hit the codepath that requires a module, get rid of it by adding the module name prefixed with `-`. Like this:

```
app {
	jvm {
		# Add support for elliptic curve cryptography.
		modules += jdk.crypto.ec
		
		# Delete the SQL module.
		modules += -java.sql
	}
}
```

Note that you can't delete a module non-optionally required by an explicit module - this will just be ignored. The JVM wouldn't start up otherwise.

**Module system flags.** If non-reflective usage of internal APIs is statically detected you'll get a warning and the necessary `--add-opens` flags will be passed. If you need to access internal APIs that aren't detected by jdeps, you'll need to add the necessary `--add-opens` flags yourself in the `app.jvm.options` key. If you add an inferred flag then that will suppress the warning.

When adding module related flags to the `app.jvm.options` key, be aware that there must not be a space between the flag name and value. Although that works with the normal Java launcher, the special launcher used by Conveyor-packaged apps requires them in native JVM form. Write e.g. `app.jvm.options += "--add-modules=jdk.incubator.foreign"` and not `app.jvm.options += --add-modules jdk.incubator.foreign`.

**Diagnosing issues.** If you'd like to see what decisions Conveyor has made about your app, you can make the `repacked-jars` task for some machine and look at the files called `required-jdk-modules.txt` (this is the output of jdeps run over your jars) and `modular-jars.txt` which is a list of the JARs that will be linked in to the optimized JVM.

## Launcher

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
