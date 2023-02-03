# Release notes

## Conveyor 6.3

Bugfixes:

* Fix a bug that could cause build failures on Windows that led to spurious warnings about virus scanners.

## Conveyor 6.2.1

Bugfixes:

* Fix compatibility of aggressive updates mode with Windows administrator escalation.
* Fix support for Yubikeys as used by ssl.com

## Conveyor 6.1

Bugfixes:

* **Important:** Fix a non-determinism in generation of self-signing certificates that would cause upgrade failures for self-signed apps re-built after January 1st 2023.
* Resolved an issue with using HSM drivers on macOS.
* Enforce that the `app.fsname` key is written in `kebab-case`, as this name is transformed into different casing styles for different platforms to match native conventions and thus kebab-case was as previously undocumented assumption.
* Fix a crash that could occur if the Mac finder placed `.DS_Store` files inside the private disk cache area.
* Improved documentation around how to fix missing app icons on Linux.

## Conveyor 6

* To improve compatibility with Amazon S3/CloudFront, the way Debian packages are placed in the generated apt site has been changed.
  The new default "non-flat" layout places the apt and .deb files into a `debian` subdirectory. The previous "flat" layout (where all 
  the files are together in the same directory) continues to be used for projects that have already uploaded a flat site, or for projects 
  targeting GitHub Releases, as GitHub doesn't allow uploads of files in directories. The type of site can be controlled with the new
  `app.site.flat` key.
* Updated JVM template apps to Gradle plugin 1.3, and refreshed the JDK standard library. 
* Bugfix: improved UNIX flavor detection for native libraries in JAR files.
* Bugfix: fixed an issue with restarting the app after a forced update on Windows if the application name has a space in it.
* Bugfix: ensure that `app.version` is always interpreted as a string even if unquoted in HOCON.
