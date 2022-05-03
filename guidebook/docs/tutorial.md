# Tutorial

In this tutorial we'll generate a fresh application using the templates built in to Conveyor. Then we'll compile a download site for it containing packages for every supported platform. Finally we'll take a look at how things are wired up and thus learn how to package a pre-existing project.

This tutorial doesn't try to cover all the features Conveyor has, it's only here to get you started. Read through the rest of this guidebook to learn about the full range of possibilities.

!!! tip
    You can tick the checkmarks on this page to mark your progress. Their state is stored in a cookie.

## Step 1. Setting up

* [x] Follow the instructions in [Setting up](setting-up.md) to install Conveyor and (optionally) supply certificates.
* [x] Pick a URL for hosting your packages. This is where auto-updates will check for new versions. All you need is a directory on a web server. [You can use GitHub Releases](configs/download-pages.md#publishing-through-github) to host your repository files and GitHub Sites to host the generated `download.html` file. For testing you can also use `localhost`.

## Step 2. Create a sample project

Conveyor has two pre-canned "Hello World" project templates. One is for a GUI app using [JetPack Compose for Desktop](https://www.jetbrains.com/lp/compose-desktop/), and the other uses [JavaFX](https://www.openjfx.io). This is the quickest way to try things out.

* [x] Make sure you have a modern JDK installed so you can compile the samples. Conveyor requires apps to use Java 11 or higher.
* [x] Run the following command with flags customized as you see fit:

```
conveyor generate {javafx,compose} \
                          --output-dir=path/to/my-project \
                          --site-url=https://mysite.com/downloads \
                          --rdns=com.example.myproject \
                          --display-name="My Amazing Project"
```

where you pick one of `javafx` or `compose`, `--rdns` should be set to a Java style package name, and `--site-url` can be `http://localhost` if you don't want to try uploading the results somewhere. The `--output-dir` will be created and populated with a buildable project.

!!! tip "Cross platform UI"
    JetPack Compose is the next-gen native UI toolkit on Android and it also runs on Windows/Mac/Linux, making it easy to share code between mobile and desktop. [JavaFX also runs on mobile](https://gluonhq.com/products/mobile/) and [the web](https://www.jpro.one).

## Step 3. Build unpackaged apps

* [ ] Change into the output directory you selected above and run `./gradlew jar` to compile the sample.
* [ ] Now build a self-contained but unpackaged app for your current platform:

```
# If on Windows:
conveyor make windows-app

# If on Linux:
conveyor make linux-app

# If on macOS, one of the following for Intel/M1 Macs respectively:
conveyor -Kapp.machines=mac.amd64 make mac-app
conveyor -Kapp.machines=mac.aarch64 make mac-app
```

This will compile the project and then create an unzipped, unpackaged app in the `output` directory. Now run the generated program directly in the usual manner for your operating system to check it works.

Because we generated a JVM app the above commands can be run on any OS in any combination. You should pick the one that matches your machine only so you can run the resulting output.

The command for macOS is different to those for Windows and Linux because Conveyor supports two CPU architectures for macOS, so you have to disambiguate which you want. The `-K` switch sets a key in the config file for the duration of that command only. Here we're setting the `app.machines` key which controls which targets to create packages for.

## Step 4. Build and upload the download site

* [ ] Request a full build of the download / repository site:

```
conveyor make site
```

The previous contents of the output directory will be replaced. You'll now find there:

* For Linux:
    * A plain tarball (which doesn't auto update).
    * A `.deb` package for Debian/Ubuntu derived distributions.
    * `apt` control files like `InRelease` and `Packages`. The generated site is also an apt repository, and the `.deb` will install sources files that use it.
* For macOS:
    * Zips containing separate Intel and ARM .app folders. If you provided Apple signing certificates and a notarization service password in Step 1 then these will be signed and notarized.
    * Two `appcast.rss` files, one for each CPU architecture. These control updates.
* For Windows:
    * A plain zip file (which doesn't auto update).
    * An MSIX package and `.appinstaller` XML file. The latter can be opened on any Windows 10/11 install and will trigger the built-in "App Installer" app. That in turn will download the parts of the MSIX file that the user's system needs, and then install the package. The `.appinstaller` file is what's checked to find updates.
* A `download.html` file that auto detects the user's operating system and CPU when possible.
* If you *didn't* supply code signing certificates in Step 1 you'll also have:
    * A `.crt` file containing your Windows self-signed certificate.
    * A `launch.mac` file containing a shell script that will download the Mac app with `curl`, unpack it to `/Applications` or `~/Applications` and then start it up.
    * A `launch.win.txt` file containing a PowerShell script (the extension is to force the web server to serve it as text). The script will download the certificate file, elevate to local admin, install it as a new root certificate and then install the MSIX.
    * The `download.html` file will contain commands to copy/paste to a terminal that will use those scripts.


You can now copy the contents of the directory to the URL you specified when creating the project. Try downloading and installing the package for your developer machine to see it in action.

## Step 5. Release an update

The version of the packages is taken from the version defined in the build system. 

* [ ] Open the source code of the app and change the message that's displayed when you click the button. Now change the line in  `build.gradle[.kts]`  that reads `version = "1.0"` to `version = "1.1"`. 
* [ ] Run the following commands:

```
# Rebuild the app with the new version number.
./gradlew jar   

# Rebuild the site.
conveyor make site
```

* [ ] Now copy this directory to your web server using a tool like `scp`, `rsync` or however else you normally publish static web files. If using GitHub Releases, just make a new release with the contents of the site output (don't add  `download.html` to the release, instead add it to your GitHub Pages or equivalent).

??? tip "Faster builds"
    Conveyor builds are incremental and parallel, so you should find that rebuilding the site is much quicker the second time you try it. You can also instantly 'check out' the intermediate files, such as unpacked directories. Run `conveyor make` to see what's available. This lets you rapidly iterate on your packages, because once built it normally only takes a few seconds to create a new spin of your app.
    

    Nonetheless there are ways to make builds faster:
    
    1. Set `app.sign = false` during development to disable code signing and notarization. Notarization takes about two minutes and is unnecessary whilst iterating.
    2. Set `app.linux.compression = low` or `none` to switch to gzip or no compression when building Linux packages. The resulting packages are bigger, but build much faster than when using the default LZMA codec.

Each operating system has its own approach to how and when updates are applied:

* **Debian/Ubuntu derived distros:** `apt-get update && apt-get upgrade` as per normal. Updates will also be presented via the normal graphical software update tool, along side OS updates.
* **macOS:** Updates are downloaded in the background when the app starts if:
    * It's not the first start and it's been more than one hour since the last update check.
    * *or* if the `FORCE_UPDATE_CHECK` environment variable is set. For example you can run `FORCE_UPDATE_CHECK=1 /Applications/YourApp.app/Contents/MacOS/YourApp` from a terminal to force an immediate update check.
    * Once the update is downloaded the user is prompted to install and restart.
    * The update schedule and UI can be adjusted in the config file.
* **Windows:** Updates are checked:
    * If the user re-opens the `.appinstaller` XML file. This is the easiest way to test updates. You don't have to re-download it because the `.appinstaller` file contains its own URL and the "App Installer" app will re-download a fresh copy from the download site when it's opened.
    * Every 8 hours in the background by the OS, regardless of whether the app is being used or not.
    * Optionally, on every app launch with a frequency you can specify. These update checks can be configured to block startup, ensuring that the user is always up to date. [Learn more here](configs/windows.md).

* [ ] You can now test the update procedure. 

??? tip "Windows package management"
    MSIX files are conceptually similar to Linux packages and they share many of the same features. An MSIX package is simply a signed zip with some additional metadata in XML files that define how the package should be integrated with the OS (e.g. start menu entries, adding programs to the %PATH% etc).
    

    MSIX and the features it offers are [described in the "Outputs" section](outputs.md). You can control MSIX packages from the command line using PowerShell, for example with the [Add-AppxPackage](https://docs.microsoft.com/en-us/powershell/module/appx/add-appxpackage?view=windowsserver2022-ps) cmdlet.
    
    A useful tool is [MSIX Hero](https://msixhero.net/), which is a sophisticated GUI tool for the package manager. It allows you to explore the contents of packages, force update checks, run apps inside the MSIX container and more.

## Step 6. Explore the integration

In this section you'll learn how to add Conveyor packaging to an existing project by studying how the sample projects are configured.

* [ ] Open `conveyor.conf` in the project root directory. It's defined using a superset of JSON called [HOCON](configs/hocon-spec.md) with a few [Conveyor-specific extensions](configs/hocon-extensions.md). It should look like this:

```javascript title="conveyor.conf"
include "/stdlib/jdk/17/openjdk.conf"   // (1)!
include "#!./gradlew -q printConveyorConfig"  // (2)!

app {
  display-name = My Amazing Project   // (3)!
  site.base-url = downloads.myproject.org/some/path   // (4)!
  
  inputs += "icons/*.png"  // (5)!
  icons = "icon-square-*.png"
  mac.icons = "icon-rounded-*.png"
}

conveyor.compatibility-level = 1   // (6)!
```

1. You can import JDKs by major version (optionally also the minor version) and by naming a specific distribution. [Learn more](stdlib/jdks.md).
2. This is a [hashbang include](configs/hocon-extensions.md#including-the-output-of-external-commands). The given program will be run and the output included as if it were a static HOCON file.
3. You may not need to set this if the display name of your project is trivially derivable from the name of the Gradle project. Use `printConveyorConfig` to see what the plugin guessed.
4. This is where the created packages will look for update metadata.
5. The templates come with pre-rendered icons in both square and rounded rectangle styles. This bit of config uses square by default and rounded rects on macOS only, but that's just a style choice to fit in with the native expectations. You can use whatever icons you like. They should be rendered as PNGs in a range of square sizes, ideally 32x32, 64x64, 128x128 etc up to 1024x1024.
6. This line will be added to a freshly written config if it's missing. Recording the schema/semantics expected by the config allows the format to evolve in future versions without breaking backwards compatibility.

This sample is small, as most Conveyor configs are. A combination of sensible defaults, automatically derived values and (optionally) config extracted from your build system keeps it easy. Still, there are around 150 different settings available to customize packages if you need them. Consult the configuration section of this guide to learn more about what you can control.

### Gradle projects

You don't have to use any particular build system with Conveyor, but if you use Gradle then config can be extracted from your existing build using a simple plugin. The Gradle plugin doesn't replace or drive the package build process itself: you still do that using the `conveyor` command line tool. The plugin is narrowly scoped to generating configuration and nothing more. If you want Gradle to run Conveyor you can add a normal exec task to do so.

The plugin adds two tasks, `printConveyorConfig` and `writeConveyorConfig`. The first prints the generated config to stdout, and the second writes it to an output file. By default this is called `generated.conveyor.conf` but can be changed.

* [ ] Run `./gradlew -q printConveyorConfig` and examine the output. The plugin can read config from other plugins like the Java application plugin, the JetPack Compose plugin and the OpenJFX plugin.
* [ ] Open `settings.gradle{.kts}` file. The following bit of code adds support for loading the Gradle plugin:

```kotlin title="settings.gradle.kts"
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.hq.hydraulic.software")
    }
}
```

* [ ] Open `build.gradle{.kts}` file. It should apply the Conveyor plugin at the top:

```
plugins {
    id 'dev.hydraulic.conveyor' version '0.9.8'
}
```

[Get the latest version number and plugins code snippet here](https://plugins.gradle.org/plugin/dev.hydraulic.conveyor).

The hashbang include you saw earlier will run Gradle each time you invoke Conveyor to extract config. This approach adds a slight delay to each Conveyor run, because even with the Gradle daemon this process isn't instant, but it does mean your config is always synced.

You can also write `include required("generated.conveyor.conf")` and run `gradle writeConveyorConfig` when you change your Gradle build. This avoids any delay from involving Gradle but means your settings can get out of sync.

!!! tip
    When iterating on packages use the faster form, and then switch to the slower form when done.

??? "Why does the plugin only generate config?"
    Conveyor isn't implemented itself as a Gradle plugin because:
    
    * It will support non-JVM apps in the near future.
    * It must support projects that use any build system.
    * It needs a customized JVM that has various bug fixes backported to it, and which thus won't match the one being used to run Gradle.
    * The Gradle API frequently changes in backwards-incompatible ways.


### Maven projects

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

### Other build systems

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

??? note "File paths"
    Inputs are resolved relative to the location of the config file, not where Conveyor is run from.

??? warning "Uber-jars"
    Don't use an uber/fat-jar for your program. It'll reduce the efficiency of delta download schemes like the one used by Windows. It also means modular JARs won't be encoded using the optimized `jimage` scheme. Use separate JARs for the best user experience.

This configuration adds your app's main JAR as the first input, allowing package metadata like version numbers and names to be derived from the file name. Then it adds the directory containing all the app  JARs (duplicates are ignored), and finally a set of icon files.

That's all you need! The display name and version of your application will be taken from the file name by default ("My App" and "1.0" given the file name in the example above).  

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
