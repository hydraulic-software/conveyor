# Release notes

## Conveyor 18.1 (May 20th 2025)

* Fixed a crash that could occur when the file inputs didn't exist.
* Fixed a rare problem that could occur when zipping a large file tree containing symlinks.
* Add compatibility with Java 24.
* Disable the Gradle configuration cache in the JavaFX sample.

## Conveyor 18 (May 1st 2025)

* The output printed when there's no terminal has been improved, which should make Conveyor's output more pleasant in CI log viewers. It now uses color and has clearer formatting.
* You can control the use of color and animation explicitly using the new `--console` flag, which is useful if you're redirecting Conveyor's output to a file.
* You can view the path to the latest log file with `conveyor --show-log=path`
* S3 uploads can now be configured to use the "force path style" setting, improving compatibility with some non-Amazon S3 implementations.

### :simple-windows: Windows

* Conveyor no longer uses the keychain to create new root keys. Existing root keys will continue to be read from the keychain.
* If using the eTPKCS11 HSM driver, slot 3 is now attempted if slot 0 fails. This should improve compatibility with some hardware keys issued by GlobalSign.
* Fix for a crash that could occur when attempting to set icons/manifests on binaries generated by the Godot engine, which have corrupted icon size headers.
* Fix for a problem that could occur when running an entrypoint as a Windows service (n.b. this is not officially supported).
* Fix for a problem that could affect Compose Multiplatform apps on Windows ARM.

### :simple-apple: macOS

* Conveyor no longer attempts to remove symlinks inside frameworks. Users who use buggy zip utilities that don't support symlinks correctly will need to use the standard macOS archive utility instead.
* Fixed an updater regression that could occur for Electron apps using the Mac App Store distribution (this is a non-standard distribution).
* Fix for app icons not always having the highest resolutions available.
* Fix for a crash that could occur when processing third party frameworks that use a binary Info.plist file.
* `.dylib.dSYM` bundles are now excluded during packaging, which should avoid issues with trying to sign such pseudo-libraries as well as shrinking download sizes.
* In compatibility-level 18 onwards, macOS 11 is now the default minimum requirement. It can be lowered again but this new default avoids errors when including libraries that require macOS 11 or higher, which is increasingly common.

### :simple-linux: Linux

* Fix the paths used for cache/log/config storage when XDG environment variables are set.

### :fontawesome-brands-java: JVM

* New feature: You can now add classpath entries relative to the user's home directory with a key like `app.jvm.extra-classpath = [ "~/AppData/Local/MyApp/Plugins/*.jar" ]`.
* The Jetpack Compose template app has been updated to be a full Multiplatform app using the current standard build system, with Android and iOS versions.
* Non-entrypoint EXEs are no longer registered in the AppX Manifest on Windows. This should resolve an issue that could occur when shipping to the Windows Store that contains a large number of EXEs, or when including EXEs that might also be shipped by other packages. 
