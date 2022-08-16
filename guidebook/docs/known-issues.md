# Known issues

## Missing features

* Supported apps and packages:
    * Only GUI apps are supported in this release. Support for CLI-only apps is implemented (Conveyor is packaged with itself), but the feature needs more polish before being ready to launch. Let us know if you'd like to try it out anyway.
    * Only DEB based Linux distros get native packages. For other distros Conveyor creates a tarball which doesn't auto update. RPM / FlatPak support is on the roadmap.
    * ARM Linux isn't yet supported.
    * Packages for app stores aren't yet supported.
* The Gradle plugin doesn't support Kotlin Multiplatform projects yet, due to KMM's alpha status.
* Registration of URL handlers and file type associations isn't yet supported. It can be done by adding the right keys to each platform's metadata files in the configs.
* There's no way to customize the generated download HTML. This feature is implemented but requires more polish before it's ready.
* Debian packages don't have their license metadata set yet, so will show in GNOME Software as proprietary even if they aren't.
* The P7B certificate format isn't supported.

## Bugs

* On Windows 11 app icons will show in the taskbar as too small and surrounded by a white box. This is actually a regression in Windows due to the taskbar rewrite, and affects some other apps that don't use Conveyor too. A workaround is under development.
* Changes in terminal size whilst a build is in progress won't be respected.
* Maven integration doesn't work on Windows.
* PowerShell 7 is not backwards compatible with "Windows PowerShell" (5.x and 6.x). The commands used for installing self-signed apps must be run in "Windows PowerShell", not PowerShell 7.
* Providing a directory as your primary input will cause an icons related crash. Make sure your first input points to a file.
* Specifying an input glob in combination with a target will cause the destination to be incorrectly treated as a file (e.g. `"foo.* -> bar"`).
* When using JDK11, you must use patch level 16+ (i.e. JDK 11.0.16+). Earlier builds will fail with a jlink error talking about hash mismatches. This is due to a format change that got backported to JDK11.
* `jdeps` is always executed even if the `app.jvm.modules` key doesn't include `detect`. The results however won't be used.

## 