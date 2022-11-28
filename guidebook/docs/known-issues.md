# Known issues

## Missing features

* Supported apps and packages:
    * Only GUI apps are supported in this release. Support for CLI-only apps is implemented (Conveyor is packaged with itself), but the feature needs more polish before being ready to launch. Let us know if you'd like to try it out anyway.
    * Only DEB based Linux distros get native packages. For other distros Conveyor creates a tarball which doesn't auto update. RPM / FlatPak support is on the roadmap.
    * ARM Linux isn't yet supported.
    * Packages for app stores aren't yet supported.
* Registration of file type associations isn't yet supported.
* For JVM apps, there's no direct support for importing Maven projects and the supplied command to read classpaths only works on UNIX.

## Issues with planned fixes

* Some older versions of Windows have caching bugs in which replacing an MSIX on a remote download site will cause re-downloads to fail until a reboot. In future versions Conveyor will warn you if you're re-building a site for the same version that currently exists remotely.  
* Changes in terminal size whilst a build is in progress won't be respected.
* Providing a directory as your primary input will cause an icons related crash. Make sure your first input points to a file.
* Specifying an input glob in combination with a target will cause the destination to be incorrectly treated as a file (e.g. `"foo.* -> bar"`).

## Low priority issues 

* The commands used for installing self-signed apps must be run in "Windows PowerShell", not PowerShell 7. PowerShell 7 must be explicitly installed by users and installs in parallel to PowerShell 6, so this should not cause any unexpected issues.
* When using JDK11, you must use patch level 16+ (i.e. JDK 11.0.16+). Earlier builds will fail with a jlink error talking about hash mismatches. This is due to a format change that got backported to JDK11.
* The JavaCV OpenBLAS library is incompatible with JNI library extraction. If this library is detected you will be told how to disable library extraction.
