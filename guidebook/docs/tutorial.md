# Tutorial

In this tutorial we'll generate a fresh application using the templates built in to Conveyor. Then we'll compile a download site for it containing packages for every supported platform. Finally we'll take a look at how things are wired up to learn how to package a pre-existing project.

This tutorial doesn't try to cover all the features Conveyor has. Read through the rest of this guidebook to learn about the full range of possibilities.

!!! tip
    You can tick the checkmarks on this page to mark your progress. Their state is stored in a cookie.

## Step 1. Get Conveyor and create a root key

* [x] [Download Conveyor](download-conveyor.md) to install Conveyor. On macOS sure it's added to your path by using the GUI. 

You don't need to have any code signing certificates to use Conveyor or follow this tutorial. Nonetheless, Conveyor always needs cryptographic keys so it can at least self-sign your app. To get started we'll use self signing, which is good enough for testing, internal apps and distributing software to developers. The final steps of the tutorial show you how to use real code signing keys.

* [x] Run `conveyor keys generate` from a terminal.

This command will create a new root private key, convert it to a series of words and write it to a config file in your home directory. 

!!! tip "Key derivation"
    You can back this generated file up in any way you like, even by writing down the words with a pen (include the timestamp). Each type of key Conveyor needs will be derived from this one root, unless you supply a custom key for specific platforms.

## Step 2. Create a template project

Conveyor has three pre-canned "Hello World" project templates, all Apache 2 licensed so they can form the basis of your own apps. 

One is a native OpenGL app written in C++, the second is for a GUI JVM app using the reactive [JetPack Compose for Desktop](https://www.jetbrains.com/lp/compose-desktop/) toolkit, and the last is another JVM GUI app using [JavaFX](https://www.openjfx.io). Generating a project based on these templates is the quickest way to try things out. The JVM apps are easier to play with because you don't need cross-compilers. For the C++ project you'll need to compile it on each OS that you wish to target.

* [x] For the native C++ app, install CMake and the compiler toolchain for each platform you will target.
* [x] For a JVM app, install a JDK 11 or higher.
* [x] Run the following command, picking an app type and reverse DNS name as you see fit. There are also `--display-name` and `--output-dir` flags but they are optional.

```
conveyor generate {cmake,compose,javafx} --rdns=com.example.my-project
```

* [x] Change into the output directory you just created.

Identifying an app with a reverse DNS name is required for some platforms like macOS, and other needed values can be derived from it. A directory named after the last component (in this case `my-project`) will be populated with a buildable project. Use `names-with-dashes` when separating words, not `camelCase`, as that way you'll get smarter defaults.

We'll explore what's inside the project directory in a moment. For now, note that there's a `conveyor.conf` file at the top level directory. This is where your packages are configured.

!!! tip "Reverse DNS names"
    RDNS names are just a naming convention meant to keep apps clearly separated. Actual domain name ownership isn't checked by anything. If you don't have a website consider creating a [GitHub](https://www.github.com) account and then using `io.github.youruser.yourproject`, which will ensure no naming conflicts with anyone else.

!!! tip "Cross platform UI"
    JetPack Compose is the next-gen native UI toolkit on Android and it also runs on Windows/Mac/Linux, making it easy to share code between mobile and desktop. [JavaFX also runs on mobile](https://gluonhq.com/products/mobile/) and [the web](https://www.jpro.one). The native C++ app uses OpenGL and the [GLFW library](https://glfw.org/), which abstracts the operating system's windowing APIs.

## Step 3. Compile the app

### Native / C++

Native apps can be written in any language that produces a binary, but this tutorial will use C++. Native apps must be compiled for each OS you wish to target. Getting access to different build machines is more work than necessary for this tutorial, so you should now edit `conveyor.conf` and edit the `machines = [ ... ]` line, changing the contents of the list to reflect which platform(s) you're using and will compile the app for.

Instructions for how to build are in the `README.md` file. It's a conventional CMake build system of the kind widely used in the C++ ecosystem, so we won't go into details here.

* [ ] Edit the machines key in the `conveyor.conf` file.
* [ ] Follow the build instructions in `README.md` to create the binaries.

**You'll need to re-compile the app any time you change the app's code. At this time Conveyor won't run it for you.**

??? note "Icons and manifests"
    Windows programs contain embedded metadata like icon files, XML manifests and whether it's a console app. Conveyor will edit the EXE to reflect settings in your config, so you don't need to set these things up in your build system. Any icons or manifests already there will be replaced. [Learn how to control the binary manifest](configs/windows.md#manifest-keys).


### JVM

* [ ] Run `./gradlew jar` to download the Gradle build tool and compile the app to a portable JAR.

**You'll need to re-run this command any time you change the app's code. At this time Conveyor won't run it for you.**

## Step 4. Build un-packaged apps

Now create a self-contained but unpackaged app directory for your current platform:

```
# Windows:
conveyor make windows-app

# Linux:
conveyor make linux-app

# One of the following for Intel/Apple Silicon Macs respectively:
conveyor -Kapp.machines=mac.amd64 make mac-app
conveyor -Kapp.machines=mac.aarch64 make mac-app
```

This will compile the project and then create an unzipped, un-packaged app in the `output` directory. 

* [ ] Run the generated program directly in the usual manner for your operating system to check it works.

The command for macOS is different to those for Windows and Linux because Conveyor supports two CPU architectures for macOS, so you have to disambiguate which you want. The `-K` switch sets a key in the config file for the duration of that command only. Here we're setting the `app.machines` key which controls which targets to create packages for.

Note that when using the C++ app template, both ARM and Intel Mac packages will actually contain fat binaries that work on either. You can point users at a single download, or keep them separate so the download sizes can be reduced later. For Java apps each package is specific to one CPU architecture as this reduces download sizes for your end users.

!!! tip
    * The `make` command makes use of a local file cache, downloading for any external files only once, and re-using them in subsequent project builds. Thus, a new download of the same file will be triggered only in case of events like cache or cache content removal, cache file system location change, etc. [Learn more about the disk cache](running.md#the-cache).
    * The generated project configuration file uses a small subset of the options available for your project configuration. For a detailed review of the configuration file structure and advanced configuration options please visit the [Writing config files](configs/index.md) section.

## Step 5. Serve the download site

* [ ] Request a full build of the download / repository site:

```
conveyor make site
```

The previous contents of the output directory will be replaced. You'll now find there packages for each OS in multiple formats, some update metadata files and a download page ([details](outputs.md)). You can use whatever files you wish: there is no requirement to use them all. 

The default generated `conveyor.conf` file tells each package to look for updates on `localhost:8899`. This is good enough for testing and Conveyor doesn't require a license for localhost projects, so grab your favourite web server and serve that directory. We recommend [Caddyserver](https://caddyserver.com/). You can just run `caddy file-server --browse --listen :8899` from inside the output directory.

??? tip "Error messages"
    Forgetting to run the `./gradlew jar` command in the previous step will result in the following warning, however the build will proceed and you may therefore get an inconsistent build.

    ```
    🔔 There are no JAR files in your inputs. Did you forget to add some?
    ```

!!! warning
    * There's no way to change the site URL after release because it's included in the packages. Choose wisely!
    * The Python 3 built in web server doesn't support `Content-Range` HTTP requests which is necessary for Windows `.appinstaller` installation to succeed.
    * Don't edit the `.appinstaller` file yourself. It is constructed in a special way to work around bugs in older versions of Windows, and editing the file by hand can break those mitigations. You should never have a need to edit this file as everything in it can be controlled using config.

## Step 6. Install the app

* [ ] [Browse to your new download site](http://localhost:8899/download.html) and take a look at what's there.

You don't have to use this HTML page. It's there purely as a convenience.

The generated download HTML detects the user's computer whilst allowing them to pick a different download
if they want. For properly signed packages you get a standard green download button. For self-signed packages, installation is more complex
and instructions are provided. They require using the terminal (on Windows) or using 
[a hidden keyboard shortcut](https://support.apple.com/guide/mac-help/open-a-mac-app-from-an-unidentified-developer-mh40616/mac) on macOS.

On Windows the link will go to the `.appinstaller` file and not the `.msix` package directly. When the user downloads and opens the
AppInstaller file they will get software updates and faster downloads, because data from other programs they already might have will be
re-used. If they download and install the MSIX file directly then those features are unavailable.

## Step 7. Release an update

To release a new version you simply re-compile your app then re-generate the app site. It should be uploaded to the same `site.base-url` 
location that you used before, overwriting any files that were present.

### Native / C++ 

The version of the package is defined in the `conveyor.conf` file.

* [ ] Open the source code of the app and change the message that's displayed in the title bar.
* [ ] Re-compile the app binary/binaries.
* [ ] Replace `version = 1` in `conveyor.conf` with `version = 2`.
* [ ] Re-run `conveyor make site`


### JVM

The version of the packages is taken from the version defined in the build system.

* [ ] Open the source code of the app and change the message that's displayed when you click the button.
* [ ] Now change the line in  `build.gradle[.kts]`  that reads `version = "1.0"` to `version = "1.1"`.
* [ ] Re-run `./gradlew jar`.
* [ ] Re-run `conveyor make site`.

### Testing the update 

Each operating system has its own approach to how and when updates are applied:

* **Windows:** Updates are checked:
    * If the user re-opens the `.appinstaller` XML file. This is the easiest way to test updates. You don't have to re-download it because the `.appinstaller` file contains its own URL and the "App Installer" app will re-download a fresh copy from the download site when it's opened.
    * Every 8 hours in the background by the OS, regardless of whether the app is being used or not.
    * Optionally, on every app launch with a frequency you can specify. These update checks can be configured to block startup, ensuring that the user is always up to date. [Learn more here](configs/windows.md).
* **macOS:** Updates are downloaded in the background when the app starts if:
    * It's not the first start and it's been more than one hour since the last update check.
    * *or* if the `FORCE_UPDATE_CHECK` environment variable is set. For example you can run `FORCE_UPDATE_CHECK=1 /Applications/YourApp.app/Contents/MacOS/YourApp` from a terminal to force an immediate update check.
    * Once the update is downloaded the user is prompted to install and restart.
    * The update schedule and UI can be adjusted in the config file.
* **Debian/Ubuntu derived distros:** `apt-get update && apt-get upgrade` as per normal. Updates will also be presented via the normal graphical software update tool, along side OS updates.

* [ ] You can now test the update procedure. 


??? tip "Faster builds"
    Conveyor builds are incremental and parallel, so you should find that rebuilding the site is much quicker the second time you try it. You can also instantly 'check out' the intermediate files, such as unpacked directories. Run `conveyor make` to see what's available. This lets you rapidly iterate on your packages, because once built it normally only takes a few seconds to create a new spin of your app.

    Nonetheless there are ways to make builds faster:
    
    1. Set `app.sign = false` during development to disable code signing and notarization. Notarization takes about two minutes and is unnecessary whilst iterating.
    2. Set `app.linux.compression = low` or `none` to switch to gzip or no compression when building Linux packages. The resulting packages are bigger, but build much faster than when using the default LZMA codec.


??? tip "Windows package management"
    MSIX files are conceptually similar to Linux packages and they share many of the same features. An MSIX package is simply a signed zip with some additional metadata in XML files that define how the package should be integrated with the OS (e.g. start menu entries, adding programs to the %PATH% etc).
    

    MSIX and the features it offers are [described in the "Outputs" section](outputs.md). You can control MSIX packages from the command line using PowerShell, for example with the [Add-AppxPackage](https://docs.microsoft.com/en-us/powershell/module/appx/add-appxpackage?view=windowsserver2022-ps) cmdlet.
    
    A useful tool is [MSIX Hero](https://msixhero.net/), which is a sophisticated GUI tool for the package manager. It allows you to explore the contents of packages, force update checks, run apps inside the MSIX container and more.

## Step 8. Explore the integration

In this section you'll learn how to add Conveyor packaging to an existing project by studying how the sample projects are configured.

The template `conveyor.conf` files are small, which is normal. A combination of sensible defaults, automatically derived values and (optionally) config extracted from your build system keeps it easy. Still, there are around 150 different settings available to customize packages if you need them. Consult the configuration section of this guide to learn more about what you can control.

### Native / C++

* [ ] Open `conveyor.conf` in the project root directory. It's defined using a superset of JSON called [HOCON](configs/hocon-spec.md) with a few [Conveyor-specific extensions](configs/hocon-extensions.md). It will look roughly like this:

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
4. You can restrict which platforms you support. See ["Machines"](configs/index.md#machines) for details.
5. The templates come with pre-made icons. You should replace these files with your own. Conveyor will take care of converting to native formats and embedding the icon into the Windows EXE file.
6. The CMake build system produces an install directory that uses non-Mac UNIX conventions. Here, we adapt it to a Mac bundle layout. If your build system produces a `.app` bundle already you can just provide the path of the bundle directory. See Apple's document ["Placing content in a bundle"](https://developer.apple.com/documentation/bundleresources/placing_content_in_a_bundle).

The only complicated thing here is the [inputs](configs/inputs.md). This config is using Conveyor's ability to change the layout of files in the package as they are copied in.

Now open the `CMakeLists.txt` file. This defines the build system. It contains various commands, all with comments explaining what they do. The build system demonstrates importing a third party library from a source zip, compiling it, dynamically linking against it, and passing the right linker flags to produce binaries that will work with Conveyor. See the `README.md` file for further discussion.

??? note "Code injection on macOS"
    Windows and Linux have built-in package managers that can update software automatically, but macOS does not. The only Apple provided way to ship software updates to Mac users is via the App Store. Conveyor doesn't go this route. Instead, it uses the popular [Sparkle Framework](https://sparkle-project.org/) to give your app the ability to update itself. Sparkle is a de-facto standard used across the Mac software ecosystem. 

    For Sparkle to work it must be initialized at app startup. To avoid you needing to write Mac specific code in Objective-C or Swift, Conveyor will edit the Mach-O headers of your binary when it builds the bundle to inject a shared library that starts up Sparkle for you. This happens automatically for any app that links against Cocoa or AppKit and not Sparkle. This feature is particularly useful for apps that aren't written in C++, as long as they provide sufficient header padding space. The amount of space left for adding headers can be controlled using Apple's `ld` linker with the `-headerpad` flag. If your language toolchain doesn't support header padding, this technique won't work and you'll have to link against `Sparkle.framework` yourself.

### JVM

* [ ] Open `conveyor.conf` in the project root directory. It's defined using a superset of JSON called [HOCON](configs/hocon-spec.md) with a few [Conveyor-specific extensions](configs/hocon-extensions.md). It should look like this:

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

1. You can import JDKs by major version (optionally also the minor version) and by naming a specific distribution. [Learn more](stdlib/jdks.md).
2. This is a [hashbang include](configs/hocon-extensions.md#including-the-output-of-external-commands). The given program will be run and the output included as if it were a static HOCON file.
3. You may not need to set this if the display name of your project is trivially derivable from the name of the Gradle project. Use `printConveyorConfig` to see what the plugin guessed.
4. This is where the created packages will look for update metadata.
5. The templates come with pre-rendered icons in both square and rounded rectangle styles. This bit of config uses square by default and rounded rects on macOS only, but that's just a style choice to fit in with the native expectations. You can use whatever icons you like. They should be rendered as PNGs in a range of square sizes, ideally 32x32, 64x64, 128x128 etc up to 1024x1024.
6. This line will be added to a freshly written config if it's missing. Recording the schema/semantics expected by the config allows the format to evolve in future versions without breaking backwards compatibility.


#### Gradle projects

You don't have to use any particular build system with Conveyor, but if you use Gradle then config can be extracted from your existing build using a simple plugin. The Gradle plugin doesn't replace or drive the package build process itself: you still do that using the `conveyor` command line tool. The plugin is narrowly scoped to generating configuration and nothing more. If you want Gradle to run Conveyor you can add a normal exec task to do so.

The plugin adds two tasks, `printConveyorConfig` and `writeConveyorConfig`. The first prints the generated config to stdout, and the second writes it to an output file. By default this is called `generated.conveyor.conf` but can be changed.

* [ ] Run `./gradlew -q printConveyorConfig` and examine the output. The plugin can read config from other plugins like the Java application plugin, the JetPack Compose plugin and the OpenJFX plugin.
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

#### Maven projects

For Maven there's no plugin. Instead Conveyor will read the project classpath by running the output of the `mvn` command and using it directly as configuration. Other aspects like project name must be specified explicitly. Better import from Maven is planned in a future release.

??? warning "Maven on Windows"
    Currently, automatic import from Maven only works on UNIX. On Windows you'll need to follow the instructions below for "other build systems". 

??? note "JavaFX apps"
    This framework has special support in the [standard library](stdlib/javafx.md). Check there to learn what else you'll want to add, or look at the [samples](samples/index.md).

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
2. This included file contains a single line, which runs Maven, tells it to print out the classpath and assigns the result to the `app.inputs` key: `include "#!=app.inputs mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout -Dmdep.pathSeparator=${line.separator}"`. 
3. The `fsname` is what's used for names on Linux e.g. in the `bin` directory, for directories under `lib`. In fact when specified the vendor is also used, and the program will be called `global-megacorp-my-program` unless the `long-fsname` key is overridden.
4. You may not need to set this if the display name of your project is trivially derivable from the fsname. The default here would be `My Program`.
5. This is optional. It'll be prefixed to the display name and used as a directory name  in various places; skip it if you don't work for an organization.
6. This is where the created packages will look for update metadata.

#### Other build systems

Create a `conveyor.conf` that looks like this:

```javascript title="conveyor.conf" linenums="1"
include "/stdlib/jdk/17/openjdk.conf"  // (1)!

app {
    inputs += build/jars/my-program-1.0.jar
    inputs += build/jars
    inputs += "icon-*.png"

    vendor = Global MegaCorp  // (2)!
    
    site.base-url = downloads.myproject.org/some/path  // (3)!
}
```

1. You can import JDKs by major version, major/minor version, and by naming a specific distribution.
2. This is optional. It'll be prefixed to the display name and used as a directory name  in various places; skip it if you don't work for an organization.
3. This is where the created packages will look for update metadata.

This configuration adds your app's main JAR as the first input, allowing package metadata like version numbers and names to be derived from the file name. Then it adds the directory containing all the app  JARs (duplicates are ignored), and finally a set of icon files.

That's all you need! The display name and version of your application will be taken from the file name by default ("My App" and "1.0" given the file name in the example above).  

??? note "File paths"
Inputs are resolved relative to the location of the config file, not where Conveyor is run from.

??? warning "Uber-jars"
Don't use an uber/fat-jar for your program unless you're obfuscating. It'll reduce the efficiency of delta download schemes like the one used by Windows. It also means modular JARs won't be encoded using the optimized `jimage` scheme. Use separate JARs for the best user experience.

## Step 9. Set a real site URL

So far we've been using `localhost` as the download site URL. This is convenient for testing because Conveyor treats this as a sort of trial mode. Once you change the `app.site.base-url` config key to something else, there are two scenarios.

### Open source projects

Set the `app.vcs-url` key to the URL of your version control repository (e.g. `github.com/user/project`). You will be able to use Conveyor for free. Any version control system can be used, it doesn't have to be git. The download site URL you use must be publicly accessible, or become so soon after using Conveyor. If it never becomes accessible, or the downloads don't seem to match the source code, Conveyor will stop working until the issue is rectified.

### Proprietary projects

If you don't specify a public source repository then the config will be edited to include a freshly issued license key.

License keys are associated with projects, and projects are defined by the download site URL. Anyone can build a project - Conveyor isn't licensed on a per user basis. During the introductory period Conveyor is free to use even for apps that aren't open source. Once the introductory period ends, you will need to link each license key you're using with a subscription. You'll be notified via the app when the introductory period is coming to an end. 

## Step 10. Sign with real keys

To get rid of security warnings you can use a proper code signing certificate. This step is optional; if you're just experimenting or will be distributing to a network where the admins can install custom root certificates, you can skip this section.

Conveyor can use existing keys, certificates and hardware security modules you may have, and it can also assist you in getting certificates if you don't already have them. To learn more, read about [keys and certificates](keys-and-certificates.md).

## Next steps

Not happy with the defaults? There are [lots of settings](configs/index.md) available, including settings that expose platform specific metadata and features.

Conveyor also supports servers with full Linux `systemd` integration. Take a look at the [Linux config sections](configs/linux.md) to learn more.

Stuck? Try asking in our [GitHub Discussions forum](https://github.com/hydraulic-software/conveyor/discussions).


<script>

// A little bit of code to make the task checkmarks sticky.

function getCookie(cname) {
  let name = cname + "=";
  let decodedCookie = decodeURIComponent(document.cookie);
  let ca = decodedCookie.split(';');
  for(let i = 0; i <ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) == ' ') {
      c = c.substring(1);
    }
    if (c.indexOf(name) == 0) {
      return c.substring(name.length, c.length);
    }
  }
  return "";
}

function setCookie(cname, cvalue, exdays) {
  const d = new Date();
  d.setTime(d.getTime() + (exdays*24*60*60*1000));
  let expires = "expires="+ d.toUTCString();
  document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}

let ticks = Array.from(document.querySelectorAll(".task-list-control > input[type=checkbox]"));

var initialCheckedIndexes = getCookie("completed-tasks").split(",");

for (var i = 0; i < ticks.length; i++) { 
  let cb = ticks[i];
	cb.removeAttribute("disabled"); 
	if (!initialCheckedIndexes.includes(""+i))
		cb.removeAttribute("checked");
	cb.addEventListener("click", function(e) { 
	  const nowCheckedIndexes = ticks.map((control, index) => { 
	      if (control.checked) { return index } else { return -1 }     
	  }).filter(el => el != -1);
	  setCookie("completed-tasks", nowCheckedIndexes.join(","));
  });
}
</script>

<style>
.task-list-control {
  cursor: pointer;
}
</style>
