# Tutorial

We're going to start from the simplest possible config, then incrementally improve it. Along the way you'll learn about the features Conveyor has.

## Step 1. Setting up

:octicons-tasklist-24: If you haven't already done so, follow the instructions in [Setting up](setting-up.md) to install Conveyor and (optionally) create or supply signing keys.

:octicons-tasklist-24: Pick a URL for hosting your packages. All you need is a directory on a web server. You can also use [GitHub Releases](configs/download-pages.md#publishing-through-github) to host your repository. Upgrading your users then just involves making a new release.

:octicons-tasklist-24: Render a nice icon at 128x128, 256x256 and 512x512 and place them in files called `icon-128.png`, `icon-256.png` and `icon-512.png` in your project root directory.

??? tip "Alternate icon file locations"
    Your icons are just input files like any other, but they must be imported into the root directory of your final app resources. If you want to put your icons elsewhere in your source tree, change the line where you import your icons to read `inputs += "your-images-dir/icon-*.png -> ."`. This tells Conveyor to grab files out of `your-images-dir` but that they shouldn't be placed in that location inside the package.
    

    File paths in input lists are always relative to the config file location.    

## Step 2. Integrate with your build system

Conveyor doesn't require you to use any particular build system, it only needs a directory of files to package. Nonetheless it can generate configuration snippets from Maven and Gradle projects, which is convenient for reducing duplication.

??? warning "Uber-jars"
    Don't use an uber/fat-jar for your program. It'll reduce the efficiency of delta download schemes like the one used by Windows. It also means modular JARs won't be encoded using the optimized `jimage` scheme. Use separate JARs for the best user experience.

### Gradle projects

:octicons-tasklist-24: In your `settings.gradle{.kts}` file, add the repository `maven.hq.hydraulic.software` along with `gradlePluginPortal()`:

```kotlin title="settings.gradle.kts"
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://maven.hq.hydraulic.software") }
    }
}
```

:octicons-tasklist-24:  In your `build.gradle{.kts}` file, apply the plugin with id `dev.hydraulic.conveyor`. [Get the latest version number and code snippet here](https://plugins.gradle.org/plugin/dev.hydraulic.conveyor).

The plugin adds two tasks, `printConveyorConfig` and `writeConveyorConfig`. The first prints the generated config to stdout, and the second writes it to an output file. By default this is called `generated.conveyor.conf` but can be changed.

:octicons-tasklist-24: Run `./gradlew printConveyorConfig` to check it works and see how much config is being discovered.  

:octicons-tasklist-24: Create `conveyor.conf` in your project root directory and add these two lines:

```javascript title="conveyor.conf"
include "/stdlib/jdk/17/openjdk.conf"  // (1)!
include "#!./gradlew -q printConveyorConfig"

app {
    display-name = My Amazing Project   // (2)!
    vendor = Global MegaCorp   // (3)!
    site.base-url = downloads.myproject.org/some/path   // (4)!
    inputs += "icon-*.png"
}
```

1. You can import JDKs by major version, major/minor version, and by naming a specific distribution.
2. You may not need to set this if the display name of your project is trivially derivable from the name of the Gradle project. Use `printConveyorConfig` to see what the plugin guessed.
3. This is optional. It'll be prefixed to the display name and used as a directory name  in various places; skip it if you don't work for an organization.
4. This is where the created packages will look for update metadata.
5. See below for information on icons.

Adjust the config for your project according to the annotations.

The second line will invoke Gradle each time you invoke Conveyor and include the output text. This approach adds a slight delay to each Conveyor run, because even with the Gradle daemon this isn't instant, but it means your config is always synced.

Alternatively use `include required("generated.conveyor.conf")` and run `gradle writeConveyorConfig` when you change your Gradle build. This avoids any delay from involving Gradle but means your settings can get out of sync.

!!! tip
    * When iterating on packages use the faster form, and then switch to the slower form when done.
    * The second line is a [hashbang include](configs/hocon-extensions.md#including-the-output-of-external-commands) and can run any command. It's a very useful way to incorporate dynamic behaviour into your configuration, or use alternative config languages if you don't like the one Conveyor uses natively ([HOCON](configs/hocon-spec.md)).

### Maven projects

For Maven there's no plugin. Instead Conveyor will read the project classpath by running the output of the `mvn` command and using it directly as configuration.

??? warning "Maven on Windows"
    Currently, automatic import from Maven only works on UNIX. On Windows you'll need to follow the instructions below for "other build systems". Better import from Maven is planned in a future release.

??? note "JavaFX apps"
    This framework has special support in the [standard library](stdlib/javafx.md). Check there to learn what else you'll want to add, or look at the [samples](samples/index.md).

:octicons-tasklist-24: Put the following into a file called `conveyor.conf` in your project.

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
2. This included file contains a single line, which runs Maven and tells it to print out the classpath. The results are then assigned to the `app.inputs` key, which is explained below: `include "#!=app.inputs mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout -Dmdep.pathSeparator=${line.separator}"`.
3. The `fsname` is what's used for names on Linux e.g. in the `bin` directory, for directories under `lib`. In fact when specified the vendor is also used, and the program will be called `global-megacorp-my-program` unless the `long-fsname` key is overridden.
4. You may not need to set this if the display name of your project is trivially derivable from the fsname. The default here would be `My Program`.
5. This is optional. It'll be prefixed to the display name and used as a directory name  in various places; skip it if you don't work for an organization.
6. This is where the created packages will look for update metadata.

### Other build systems

:octicons-tasklist-24: Create a `conveyor.conf` that looks like this:

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

This configuration adds your app's main JAR as the first input, allowing package metadata like version numbers and names to be derived from the file name. Then it adds the directory containing all the app  JARs (duplicates are ignored), and finally a set of icon files.

That's all you need! The display name and version of your application will be taken from the file name by default ("My App" and "1.0" given the file name in the example above).

## Step 4. Build your packages

:octicons-tasklist-24: Run `conveyor make site`. If you set a passphrase when generating keys, pass the `--passphrase` flag before `make`. The results will be in the `output` directory.

This command will build your packages with update repository metadata, and also generate a `download.html` file that provides the user with the fastest and simplest download experience possible given their browser and OS.

Now copy this directory to your web server using a tool like `scp`, `rsync` or however else you normally publish static web files. If using GitHub Releases, just make a new release with the contents of the site output (don't bother with `download.html`).

!!! tip "Faster builds"
    Conveyor builds are incremental and parallel, so you should find that rebuilding the site is much quicker the second time you try it. You can also instantly 'check out' the intermediate files, such as unpacked directories. Run `conveyor make` to see what's available. This lets you rapidly iterate on your packages, because once built it normally only takes a few seconds to create a new spin of your app.
    
    Nonetheless there are ways to make builds faster:
    
    1. Set `app.sign = false` during development to disable code signing and notarization. Notarization takes about two minutes and is unnecessary whilst iterating.
    2. Set `app.linux.compression = low` or `none` to switch to gzip or no compression when building Linux packages. The resulting packages are bigger, but build much faster than when using the default LZMA codec.

### Building unpacked apps

For testing it's convenient to create unarchived versions of the app.

:octicons-tasklist-24: Run one of these commands:

* `conveyor make windows-app` to create an unpacked Windows install.
* `conveyor -Kapp.machines=mac.amd64 make mac-app` to create an unpacked `.app` directory. Change `amd64` to `aarch64` to make an app folder for Apple Silicon instead of Intel Macs (Conveyor doesn't generate fat apps for download size reasons).
* `conveyor make linux-app` to create an unpacked Linux app .

If you want a package for a single platform:

* `conveyor make windows-zip` to get a zip, or `conveyor make windows-msix` to create an installable Windows package (this requires signing keys).
* `conveyor -Kapp.machines=mac.amd64 make unnotarized-mac-zip` to create a zip with your Mac app in it (again, change `amd64` to `aarch64` if you want an Apple Silicon build).
* `conveyor make linux-tarball` or `conveyor make debian-package` to create a tarball or deb.

## Step 6. Release a new version

To release a new version just adjust the version number in your build system or the `app.version` / `app.revision` keys, rerun `conveyor make site` and re-upload the results.

By default the update engines wait some hours between upgrades, so your installed copies won't upgrade immediately. To short-circuit this for testing:

1. On Windows, re-open the .appinstaller file you downloaded. That should offer an upgrade button.

2. On macOS, from the command line run the program with the `FORCE_UPDATE_CHECK=1` environment variable set, e.g. `FORCE_UPDATE_CHECK=1 /Applications/YourApp.app/Contents/MacOS/YourApp`. This should immediately offer to let you upgrade.

3. On Debian/Ubuntu, run `apt update; apt upgrade` and check that your app is in the list of things that will be upgraded.

## Next steps

Not happy with the defaults? There are [lots of settings](configs/index.md) available, including settings that expose platform specific metadata and features.

Conveyor also supports servers with full Linux `systemd` integration. Take a look at the [Linux config sections](configs/linux.md) to learn more.

Stuck? Try asking in our [GitHub Discussions forum](https://github.com/hydraulic-software/conveyor/discussions).
