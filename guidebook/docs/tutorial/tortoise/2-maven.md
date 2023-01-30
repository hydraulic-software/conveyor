---
hide:
- navigation
---

# Maven projects

For Maven there's no plugin. Instead, Conveyor will discover the JARs in your project by running the output of the `mvn` command and using it directly as configuration. Other aspects like project name must be specified explicitly. Better import from Maven is planned in a future release.

??? warning "Maven on Windows"
    Currently, automatic import from Maven only works on UNIX. On Windows you'll need to follow the instructions below for "other build systems".

Here's an example of how to package a Maven project:

```javascript title="conveyor.conf" linenums="1"
include "/stdlib/jdk/17/openjdk.conf"  // (1)!
include "/stdlib/jvm/from-maven.conf"  // (2)!

app {
    fsname = my-program  // (3)!
    display-name = myPROGRAM  // (4)!
    vendor = Global MegaCorp  // (5)!
    version = 1.0
    rdns-name = org.some-org.some-product
    
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

## Adapting a JavaFX app

The idea is straightforward - get all the JARs your app needs into a single directory. Below is a config that packages the gallery demo for [the modern AtlantaFX skin](https://github.com/hydraulic-software/atlantafx). Here's the [commit that added the packaging](https://github.com/hydraulic-software/atlantafx/commit/c1246ce1c377814a80d908bfd16a8d1aab600f03). The Maven POM in this project collects all the app JARs into the `sampler/target/dependencies` directory when the commands at the top of the file are run by hand. Conveyor can then just pick them up and package them.

```
// Before packaging do the build like this:
//
// mvn install -pl styles
// mvn install -pl base
// mvn prepare-package jar:jar -pl sampler

// Use a vanilla Java 17 build, latest as of packaging.
include required("/stdlib/jdk/17/openjdk.conf")

// Import JavaFX JMODs.
include required("/stdlib/jvm/javafx/from-jmods.conf")

javafx.version = 18.0.2

app {
  display-name = AtlantaFX Sampler
  fsname = atlantafx-sampler

  // Not allowed to have versions ending in -SNAPSHOT
  version = 0.1

  // Open source projects use Conveyor for free.
  vcs-url = github.com/mkpaz/atlantafx
  
  // Import the JARs.
  inputs += sampler/target/dependencies

  // Linux/macOS want rounded icons, Windows wants square.
  icons = "sampler/icons/icon-rounded-*.png"
  windows.icons = "sampler/icons/icon-square-*.png"

  jvm {
    gui.main-class = atlantafx.sampler.Launcher
    modules = [ java.logging, jdk.localedata, java.desktop, javafx.controls, javafx.swing, javafx.web ]
  }

  site.base-url = downloads.hydraulic.dev/atlantafx/sampler
}
```

There are several tasks accomplished by this config:

1. Import a JDK of the right version from your preferred vendor.
2. Import the JavaFX JMODs so the JVM will have it linked in.
3. Set the names, versions and get a free license by pointing to the GitHub URL.
4. Supply Conveyor with the directory where the app JARs can be found.
5. Import icons for macOS and Windows. Icons must be PNG files. You don't have to supply all the sizes.
6. Define the main class and the JDK modules (including the javafx modules) that you want to use.
7. Define the URL where the packages will look for online updates.

Please see the [Gradle page](2-gradle.md#adapting-a-javafx-app) to see how to set your stage icons.
