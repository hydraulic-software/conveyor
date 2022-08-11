# Release notes

## Known issues

### Missing features

* Supported apps and packages:
    * Only GUI apps are supported in this release. Support for CLI-only apps is implemented (Conveyor is packaged with itself), but the feature needs more polish before being ready to launch. Let us know if you'd like to try it out anyway.
    * Only DEB based Linux distros get native packages. For other distros Conveyor creates a tarball which doesn't auto update. RPM / FlatPak support is on the roadmap.
    * ARM Linux isn't yet supported.
    * Packages for app stores aren't yet supported.
* Registration of URL handlers and file type associations isn't yet supported. It can be done by adding the right keys to each platform's metadata files in the configs.
* There's no way to customize the generated download HTML. This feature is implemented but requires more polish before it's ready.
* Debian packages don't have their license metadata set yet, so will show in GNOME Software as proprietary even if they aren't.

### Bugs

* Changes in terminal size whilst a build is in progress won't be respected.
* Maven integration doesn't work on Windows.
* PowerShell 7 is not backwards compatible with "Windows PowerShell" (5.x and 6.x). The commands used for installing self-signed apps must be run in "Windows PowerShell", not PowerShell 7.
* On Windows 11 app icons will show in the taskbar as too small and surrounded by a white box. This is actually a regression in Windows due to the taskbar rewrite, and affects some other apps that don't use Conveyor too. A workaround is under development.
* Providing a directory as your primary input will cause an icons related crash. Make sure your first input points to a file.
* Specifying an input glob in combination with a target will cause the destination to be incorrectly treated as a file (e.g. `"foo.* -> bar"`).

## Version history

### 2.0

* Conveyor now gives Windows users an EXE file that triggers download/installation of the MSIX using the Windows API. This simplifies the Windows UX, makes it more familiar for end users, avoids problems with a small minority of machines that have fallen behind on software updates for the App Installer app due to admins disabling the Windows Store, and enables the provisioning of other features in the future.
* Conveyor now generates a `metadata.properties` file in the site directory which contains information about the software in the form of `key=value` text. You can control what else gets written here by changing the `app.site.export-keys` list, which contains a list of config keys to write out. This file is useful for software that wants to know what the latest version is, without needing to deal with platform specific XML.
* You can now specify machine-specific JVM options, and also control the options for each launcher/machine combination independently.
* You can now control the Windows console mode of each launcher independently.
* You can now control the list of files that are cleaned up after JDK linking with the `app.jvm.unwanted-jdk-files` key. 
* The default Windows timestamping authority has changed from DigiCert to Certum due to [this DigiCert issue](https://knowledge.digicert.com/solution/authenticode-signature-verification-fails-with-new-timestamping-cross-root.html), which affects blank/new Windows 10 installs.
* Fixed: Usage with recent Azul JDKs, which contain symlinks in the macOS download that were confusing Conveyor's logic for finding the right files.
* Fixed: The PowerShell one-liner used for self-signed Windows apps now works when the web server doesn't set MIME types correctly (e.g. for GitHub Releases), and when the system is configured to restrict PowerShell.
* Fixed: Notarization failure when using Apple certificates linked to developer accounts that are authorized for iPhone development.
* Fixed: some bugs that could occur when a company name in certificates used characters requiring X.500 escape sequences.
* Fixed: The template apps no longer require the system properties set by packaging.
* Fixed: The CMake template app uses the win32 subsystem (no console) on Windows.

### 1.0

First release.
