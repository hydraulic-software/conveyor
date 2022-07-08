# 7. Release an update

To release a new version you simply re-compile your app then re-generate the app site. It should be uploaded to the same `site.base-url`
location that you used before, overwriting any files that were present.

## Native / C++

The version of the package is defined in the `conveyor.conf` file.

* [ ] Open the source code of the app and change the message that's displayed in the title bar.
* [ ] Re-compile the app binary/binaries.
* [ ] Replace `version = 1` in `conveyor.conf` with `version = 2`.
* [ ] Re-run `conveyor make site`


## JVM

The version of the packages is taken from the version defined in the build system.

* [ ] Open the source code of the app and change the message that's displayed when you click the button.
* [ ] Now change the line in  `build.gradle[.kts]`  that reads `version = "1.0"` to `version = "1.1"`.
* [ ] Re-run `./gradlew jar`.
* [ ] Re-run `conveyor make site`.

## Testing the update

Each operating system has its own approach to how and when updates are applied:

* **Windows:** Updates are checked:
    * If the user re-opens the `.appinstaller` XML file. This is the easiest way to test updates. You don't have to re-download it because the `.appinstaller` file contains its own URL and the "App Installer" app will re-download a fresh copy from the download site when it's opened.
    * Every 8 hours in the background by the OS, regardless of whether the app is being used or not.
    * Optionally, on every app launch with a frequency you can specify. These update checks can be configured to block startup, ensuring that the user is always up to date. [Learn more here](../configs/windows.md).
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

    MSIX and the features it offers are [described in the "Outputs" section](../outputs.md). You can control MSIX packages from the command line using PowerShell, for example with the [Add-AppxPackage](https://docs.microsoft.com/en-us/powershell/module/appx/add-appxpackage?view=windowsserver2022-ps) cmdlet.
    
    A useful tool is [MSIX Hero](https://msixhero.net/), which is a sophisticated GUI tool for the package manager. It allows you to explore the contents of packages, force update checks, run apps inside the MSIX container and more.

<script>var tutorialSection = 7;</script>
