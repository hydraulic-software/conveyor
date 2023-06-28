# Outputs

Conveyor generates packages for Windows, macOS and Debian/Ubuntu based Linux distributions, along with a download HTML page that detects the user's operating system and CPU architecture. More package formats may be added in future. 

## Contents of the generated site

When you generate a repository site you will get the following files:

* For Windows:
    * An MSIX package and `.appinstaller` XML file. The `.appinstaller` file is what's checked to find updates, and it contains its own URL so you can open it from any location (i.e. a download), and it will still work.
    * An EXE file. This is a small custom installer, see below for details.
    * A plain zip file (which doesn't auto update).
* For macOS:
    * Zips containing separate Intel and ARM .app folders. They'll be signed and notarized if credentials were supplied.
    * Two `appcast.rss` files, one for each CPU architecture. These advertise updates to the integrated [Sparkle framework](https://sparkle-project.org/).
* For Linux:
    * A `.deb` package for Debian/Ubuntu derived distributions. The site directory is also an apt repository, and the `.deb` will install sources files that use it.
    * A plain tarball (which doesn't auto update).
* A `download.html` file that auto-detects the user's operating system and CPU when possible.
* A `metadata.properties` file that contains keys extracted from your config. This is here so your code can easily read the latest version by simply parsing key=value lines of text.
* If you are self-signing, you'll also have:
    * A `.crt` file containing your Windows self-signed certificate.
    * A `launch.mac` file containing a shell script that will download the Mac app with `curl`, unpack it to `/Applications` or `~/Applications` and then start it up.
    * A `install.ps1` file containing a PowerShell script. The script will download the certificate file, elevate to local admin, install it as a new root certificate and then install the MSIX.
    * The `download.html` file will contain commands to copy/paste to a terminal that will use those scripts.

## Windows

Conveyor uses Windows' built-in packaging technology, [MSIX](https://docs.microsoft.com/en-us/windows/msix/). Like the older MSI format, support for it is built in to Windows, but MSIX is a complete redesign with a different format, approach and capabilities. All Windows 10/11 systems support it and Microsoft have also backported it to Windows 7.[^1] MSIX files are enhanced ZIP files with several features that make it a good fit for modern desktop app distribution:

* **Delta downloads.** MSIX breaks apps into 64kb chunks and Windows only downloads those it hasn't already got. This works for *new installs* and *across unrelated vendors and apps*, meaning if the user has already downloaded some app using a popular runtime, your app using the same runtime will install near-instantly as only the unique program data will need to be fetched. Files on disk are also de-duplicated when possible by using hard links. This works because the "installer" the user downloads is in reality a small XML file that points to the real underlying file, which is itself indexed by hash.
* **Automatic upgrades**. Windows keeps MSIX apps up to date in the background, even if they aren't running. You can also force Windows to check for an update on every launch, if you need your app to stay tightly synchronized with a remote server.
* **Containerization.** Apps packaged with MSIX are run inside a form of lightweight container that virtualizes storage and the registry. This ensures apps can't alter the OS and that uninstalls are always 100% clean. It's backwards compatible and apps don't notice it's happening. This form of virtualization isn't a sandbox and doesn't stop apps interacting with each other or integrating with the operating system, so no features are lost, but it does stop the app's files being tampered with.
* **Slick enterprise IT integration.** Historically on Windows every app has rolled its own installer and update system, but IT teams need a unified system they can easily manage. To get it they must engage in a slow and painful "repackaging" process. That's bad for everyone, and leads to desktop apps having a reputation for being painful to deploy inside large enterprises. Because it's managed by Windows, MSIX gives IT teams [everything they need](https://docs.microsoft.com/en-us/windows/msix/desktop/managing-your-msix-deployment-overview) to manage app rollouts, rollbacks and access control, all fully integrated with Active Directory, InTune and Azure.
* **Support for CLI apps.** CLI apps are automatically added to the system search path, including in running terminal sessions. You can invoke programs from the command line the moment they're installed.

If you'd like to learn more about MSIX, check out [the Microsoft website for it](https://docs.microsoft.com/en-us/windows/msix). You can script the Windows package manager using PowerShell (e.g. with [the Appx cmdlets](https://docs.microsoft.com/en-us/powershell/module/appx/?view=windowsserver2022-ps)).

For people who don't want to or can't use MSIX for some reason, Conveyor also creates a plain `.zip` version of the app. This version won't auto update and is especially useful for IT departments that have a custom software distribution system, who would otherwise need to repackage the MSIX.

### Installer EXE

Conveyor generates a small (~500kb) installer EXE. You don't have to use this (you can direct your users to the `.appinstaller` file which Windows has a built in app for), but it's strongly recommended that you do. The installer drives the installation or upgrade process using the Windows package manager/download APIs directly, yielding these benefits:

1. Fewer clicks. The installer begins the process immediately and launches your app as soon as it's ready. This makes it more convenient for your users.
2. Bug workarounds. The "App Installer" app that Microsoft ships for installing MSIX files unfortunately isn't always reliable, especially in Windows 10. The Conveyor installer works around bugs in Windows to ensure a reliable install.
3. Users are familiar with installer EXEs.
4. If the app is already installed and the user runs the installer again, it immediately launches the app. Users who aren't sure how to use the start menu can therefore get the right behavior without risking double installs, confusing UI etc.

The EXE is also included into your package files. You can run it to do an update check, and it will be invoked as part of your app startup sequence if you've enabled [aggressive updates](configs/index.md#aggressive-updates) or the [escape hatch](configs/windows.md#escape-hatch-mechanism) feature.

## macOS

Conveyor outputs signed, notarized and [Sparkle-ized](https://sparkle-project.org/) app bundles inside a zip file. It doesn't produce DMGs. This is deliberate and for user experience reasons. Apps inside ZIPs have several advantages over the DMG format:

1. **Drastically faster.** On fast internet connections a DMG can take longer to verify and mount than download!
2. **Fewer steps for users.** Browsers will detect zips that contain app bundles and automatically extract them. The user can then simply open the app from the download pile in their dock to get started.
3. **Better usability.** The DMG system that macOS uses for app installs dates from NeXTStep in the early 90s and has never been improved. As such the usability of DMGs is quite poor. The user must master unusual actions like drag-and-drop, must understand how to "eject" the "disk" by dragging it to the trash can even though it's not a disk and they don't want to remove the app, must know that they actually should do so, and must understand that although the app *can* be run from the DMG it should actually *not* be run from there ... and so on.

ZIPs give users the experience of simply clicking the download link and having the app immediately extracted and ready for use. 

Conveyor doesn't currently generate fat binaries. Two separate packages are created and on Chrome the download page selects between them by asking the browser what the user's CPU architecture is, which avoids downloading unnecessary code. 

!!! note
    Future versions of Conveyor will make apps automatically relocate themselves to /Applications if started from the downloads directory, thus automating the entire installation process.

## Linux

Conveyor creates multiple types of output for Linux.

### Debian/Ubuntu packages

Conveyor creates a DEB package inside an apt repository. Although the user could add the repository manually there's no need, because the DEB itself will install a repository descriptor into `/etc/apt/sources.d`. Thus installing the DEB via the GUI or by running `apt install ./app-1.2.3.4.deb` is sufficient, and will ensure full dependency resolution plus subsequent updates. 

The packages comply with the Linux Filesystem Hierarchy Standard as much as possible and install to `/usr` with the working directory mapped to `/usr/lib/${long-fsname-dir}` by default (but all those things can be changed). 

Dependencies are automatically filled out by scanning the input files for native shared libraries and then using the reverse package index to discover appropriate package names. You can add additional dependencies if you require software that isn't a direct ELF shared library dependency. 

For desktop apps the packages install launcher icons and metadata for app stores like GNOME Software Center. 

For servers, systemd units can be added. The background service is automatically enabled and started on first install, as Debian policy requires. It's also shut down on uninstall, and stopped/restarted across upgrades, so you don't have to deal with files changing out from underneath you. A message is printed during install showing the startup status. For web servers sample nginx and Apache configs can be generated and installed, ready for use after a minor edit to set the desired host name.

Files in the `conf` sub-directory of your inputs are mapped to a directory under `/etc` and marked as "conf files" in the package, meaning if the user changes them and then upgrades the package, the two files will be merged, or the user will be asked what to do if a merge cannot be done.

You can fully customize or add code to the postinst / prerm scripts.

Packages and the repository are signed with GPG keys derived from your root entropy.

### Generic tarball

The `linux-tarball` task creates a `.tar.gz` of the application's files, suitable for extraction to any location. Additional metadata files like `.desktop` launchers or `.service` files are also placed in the tarball so the admin can install them if they want to.

### Other Linux package formats 

Future versions of Conveyor are planned to support:

1. RPMs.
3. Eventually, Snaps and FlatPaks.

[^1]: In case you're wondering, marketshare for Windows 8 is so small that it can be safely ignored.
