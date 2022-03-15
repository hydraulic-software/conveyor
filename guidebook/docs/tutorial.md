# Tutorial

We're going to start from the simplest possible config, then incrementally improve it. Along the way you'll learn about the features Conveyor has.

## Step 1. Get set up

If you haven't already done so, follow the instructions in [Getting set up](setting-up.md) to install Conveyor and set up signing keys.

## Step 2. Write your first build config

!!! tip "Build systems"
    If you're using Gradle or Maven then you have special support, but it's still worth reading this section so you understand what's happening later.

Put the following into a file called `conveyor.conf` in your project.

```hocon
# Use Java 17
include required("/stdlib/jdk/17/openjdk.conf")

app {
  inputs += build/jars/my-app-1.0.jar
  inputs += build/jars
  inputs += "icon-*.png"
  
  site.base-url = your-website.org/my-app
}
```

??? note "GUI apps using JavaFX or SWT"
    These frameworks have special support in the [standard library](stdlib/index.md). Check there to learn what else you'll want to add, or look at the [samples](samples/index.md).

??? note "File paths"
    If your application is a sub-module you can choose whether to put your conveyor.conf file in the repository root or the directory where your application module is defined. Just make sure that the inputs directory is correct relative to the config file itself.

This configuration adds your app's main JAR as the first input, allowing package metadata like version numbers to be derived from the file name. Then it adds the directory containing all the app  JARs (duplicates are ignored), and finally a set of icon files.

That's all you need! The display name and version of your application will be taken from the file name by default ("My App" and "1.0" given the file name in the example above).

**Inputs.** You can use globs, brace expansion, specify HTTP[S] URLs, extract archives and remap the contents of directory trees all from the inputs list. [Learn more](configs/inputs.md).

**Download URL.** You'll need to pick a URL where your downloads will be hosted before you create packages, so they know where to look for updates. It should be a dedicated directory on the web server, or you can use GitHub Releases. [Learn more](configs/download-pages.md).

**Naming.** If you need to control your branding better define the `app.display-name` and  `app.vendor` keys. You might also want to set the `app.fsname` key to control the naming of directories and executables on Linux, for example, if your app JAR isn't named as the user should see the app. [Learn more](configs/index.md).

**Icons.** You'll need to render a few icon images, at least for common sizes like 128x128, 256x256 and 512x512. Put them next to your config file with names like `icon-256.png`, `icon-512.png`. The more sizes you have the better, as the operating systems won't have to downscale them on the fly and you can tweak the artwork to look better. They must be in PNG format. [Learn more](configs/index.md#icons).

**Main class.** The above config assumes your app JAR has a `Main-Class` manifest attribute. If it doesn't you'll need to add a line like `jvm.main-gui-class = com.foobar.Main` inside the `app{}` section. Your config can control many aspects of how a JVM app runs. [Learn more](configs/jvm.md).

## Step 2. Integrate with your build system

Conveyor takes JARs and other files as input. Above we used a directory of JARs, but it's often more convenient to take the paths directly from the build system.

??? warning "Uber-jars"
    Don't use an uber-jar for your program. Although it may seem convenient it'll reduce the efficiency of delta download schemes like the one used by Windows, slowing things down for your users and waste bandwidth. It also means modular JARs won't be encoded using the optimized `jimage` scheme. Use separate JARs for the best user experience.

For **Maven** projects you can get the classpath directly from your POM. [See here for details](configs/maven-gradle.md).

For **Gradle** projects you have two options:

1. **Simple.** Use the `application` plugin and run `installDist` before invoking Conveyor. The JARs can be found in the `build/install/<appname>/lib` directory. In that case, adjust the input paths in the config from `build/all-jars` to `build/install/<appname>/lib`.

2. **Better.** Or you can use [the Conveyor Gradle plugin](configs/maven-gradle.md#reading-configuration-from-gradle) to generate a config snippet. This extracts configuration from your Gradle build and also understands other common plugins, especially for GUI frameworks like JavaFX or JetPack Compose. For JetPack Compose apps this is required, as the Compose plugin doesn't provide a direct equivalent to the `installDist` command.

Let's assume you're using [the open source Gradle plugin](https://github.com/hydraulic-software/conveyor/tree/master/gradle-plugin). Do the following:

1. In your `settings.gradle{.kts}` file, add the repository `maven.hq.hydraulic.software` along with `gradlePluginPortal()`.
1. In your `build.gradle{.kts}` file, apply the plugin with id `dev.hydraulic.conveyor`. [Find the latest version number here](https://plugins.gradle.org/plugin/dev.hydraulic.conveyor).
3. The plugin generates Conveyor configuration. You now face a choice of how to use it:
    1. Add `include required("#!./gradlew -q printConveyorConfig")` to the top of your config. This will invoke Gradle each time you invoke Conveyor and import settings from your build. Therefore this adds a slight delay to each Conveyor run, because even with the Gradle daemon this is a bit slow, but it means your config is always synced.
    2. Or, add `include required("generated.conveyor.conf")` and run `gradle writeConveyorConfig` when you change your Gradle build. This avoids any delay from invoking Gradle but means your settings can get out of sync.


When experimenting locally it may be worth using the faster form and then switching to the slower form when done.

## Step 3. Build your app for each platform and test

You don't  have to do this step if you trust your runtime's cross-platform abstraction. But it's best to do so at least once, no different to how it's good to test on all the major browsers when writing a website.

Run:

* `conveyor make windows-app` to create an unpacked Windows install.
* `conveyor make mac-app` to create an unpacked `.app` directory.
* `conveyor make linux-app` to create an unpacked Linux app .

If you want a single file:

* `conveyor make windows-msix` to create an installable Windows package (this requires signing keys).
* `conveyor make unnotarized-mac-zip` to create a zip with your Mac app in it.
* `conveyor make linux-tarball` or `conveyor make debian-package` to create a tarball or deb.

The results will be in the `output` directory. You can change that location with the `--output-dir` or `-o` flag. If the output directory was created by Conveyor then running `make` with different tasks will switch the contents from one to another, so be careful to treat the output as read only.

Conveyor builds are incremental and parallel. You should find that building the second and third app is quicker than the first, and once built switching between them is nearly instant. This lets you rapidly iterate on your packages, because once built it normally only takes a few seconds to create a new spin of your app.

!!! tip "Faster builds"
    1. Set `app.sign = false` during development to disable code signing and notarization, which can be slow.
    2. Set `app.linux.compression = low` or `none` to switch to gzip or no compression when building Linux packages. The resulting packages are bigger but much faster to build than with the default LZMA codec.

## Step 4. Build a download site

`conveyor make site` will wrap your packages with update repository metadata, and also generate a `download.html` file that provides the user with the fastest and simplest download experience possible given their browser and OS.

Now copy this directory to your web server using a tool like `scp`, `rsync` or however else you normally publish static web files.

!!! tip "GitHub Releases"
    You can use [GitHub Releases and Pages](configs/download-pages.md#publishing-through-github) to host your repository. Upgrading your users then just involves making a new release.

## Step 5. Release a new version

To release a new version just adjust the version number in your build system or the `app.version` / `app.revision` keys, rerun `conveyor make site` and re-upload the results. Remember to compile and re-JAR your app first!

By default the update engines wait some hours between upgrades, so your installed copies won't upgrade immediately. To short-circuit this for testing:

1. On Windows, re-open the .appinstaller file you downloaded. That should offer an upgrade button.

2. On macOS, from the command line run the program with the `FORCE_UPDATE_CHECK=1` environment variable set, e.g. `FORCE_UPDATE_CHECK=1 /Applications/YourApp.app/Contents/MacOS/YourApp`. This should immediately offer to let you upgrade.

3. On Debian/Ubuntu, run `apt update; apt upgrade` and check that your app is in the list of things that will be upgraded.

## Step 5. Refine your configuration

Not happy with the defaults? There are [lots of settings](configs/index.md) available, including settings that expose platform specific metadata and features.

Conveyor also supports servers with full Linux `systemd` integration. Take a look at the [Linux config sections](configs/linux.md) to learn more.
