# Known issues

## Missing features

* Supported apps and packages:
    * Only GUI apps are supported currently. Support for CLI-only apps is implemented (Conveyor is packaged with itself), but the feature needs more polish before being ready to launch. Let us know if you'd like to try it out anyway.
    * Only DEB based Linux distros get native packages. For other distros Conveyor creates a tarball which doesn't auto update. RPM / FlatPak support is on the roadmap.
    * ARM Linux
    * ARM Windows.
* App store packages.
* Cloud HSM services.
* Currently, you can't change your Windows signing identity without breaking the update process (e.g. going from OV to EV, or renaming your company, or changing the location of the headquarters).
* JVM: support for reading Maven classpaths on non-UNIX platforms.

## Low priority issues 

* If you start a Windows Terminal via the shift-right click menu in Explorer, you'll get plain text progress tracking instead of the animated progress bars. Start Windows Terminal in the normal way as a workaround.
* Changes in terminal size whilst a build is in progress won't be respected.
* When using JDK11, you must use patch level 16+ (i.e. JDK 11.0.16+). Earlier builds will fail with a jlink error talking about hash mismatches. This is due to a format change that got backported to JDK11.
