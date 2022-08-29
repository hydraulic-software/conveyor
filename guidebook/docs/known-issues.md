# Known issues

## Missing features

* Supported apps and packages:
  * Only GUI apps are supported in this release. Support for CLI-only apps is implemented (Conveyor is packaged with itself), but the feature needs more polish before being ready to launch. Let us know if you'd like to try it out anyway.
  * Only DEB based Linux distros get native packages. For other distros Conveyor creates a tarball which doesn't auto update. RPM / FlatPak support is on the roadmap.
  * ARM Linux isn't yet supported.
  * Packages for app stores aren't yet supported.
* For JVM apps:
  * The Gradle plugin doesn't support Kotlin Multiplatform projects yet, due to KMM's alpha status.
  * There's no direct support for importing Maven projects, and the supplied command to read classpaths doesn't work on Windows.  
* There's no way to customize the generated download HTML. This feature is implemented but requires more polish before it's ready.
* Registration of URL handlers and file type associations isn't yet supported.
* Debian packages don't have their license metadata set yet, so will show in GNOME Software as proprietary even if they aren't.
* The P7B certificate format isn't supported.

## Issues with planned fixes

* On Windows 11 app icons will show in the taskbar as too small and surrounded by a white box. This is actually a regression in Windows due to the taskbar rewrite, and affects some other apps that don't use Conveyor too. A workaround is under development.
* On macOS Catalina (10.15) packaged apps won't start up. Apple has provided assistance and the cause is known, a fix is in development. 
* On Windows machines with screen readers installed, JVM Swing/AWT/Compose apps may crash due to lack of the `jdk.accessibility` module. Conveyor will start adding this module automatically when necessary in a future release.
* Changes in terminal size whilst a build is in progress won't be respected.
* Providing a directory as your primary input will cause an icons related crash. Make sure your first input points to a file.
* Specifying an input glob in combination with a target will cause the destination to be incorrectly treated as a file (e.g. `"foo.* -> bar"`).
* `jdeps` is always executed even if the `app.jvm.modules` key doesn't include `detect`. The results however won't be used. This is fixed in the next release.

## Low priority issues 

* When using JDK11, you must use patch level 16+ (i.e. JDK 11.0.16+). Earlier builds will fail with a jlink error talking about hash mismatches. This is due to a format change that got backported to JDK11.
* The commands used for installing self-signed apps must be run in "Windows PowerShell", not PowerShell 7. PowerShell 7 must be explicitly installed by users and installs in parallel to PowerShell 6, so this should not cause any unexpected issues. 
