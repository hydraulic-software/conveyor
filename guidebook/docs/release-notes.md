# Release notes

## Conveyor 5

### New features

* Updates can now be set to [aggressive mode](configs/index.md#update-modes), meaning the packaged app will check for an update on every 
  launch. You can use this to easily keep clients up to date with server protocol changes, without needing to think about backwards compatibility.
* The new `app.site.copy-to` key lets you define a location to upload the resulting site via SFTP using the `make copied-site` task.
* The new `app.url-schemes` key lets you [associate your app with protocol schemes](configs/os-integration.md) (`my-app://deep/link`).
* The new `app.mac.bundle-extras{,.amd64,.aarch64}` keys allow you to add extra files anywhere in your Mac bundle even for non-native apps. 
* The `app.site.base-url` key is now set to GitHub Releases automatically when `app.vcs-url` points to an HTTPS GitHub repository URL.
* The [new `http-headers` sub-key](configs/inputs.md#object-syntax) of input objects can be used to pass authentication tokens or other 
  arbitrary secrets as part of the download. This is useful for fetching artifacts from CI systems. 

### Electron

* The Electron default configuration has been improved with more macOS specific customizations.
* The top level `package-lock {}` object has been renamed to `package-json {}`, with the old name remaining recognized for backwards compatibility purposes.

### JVM

* JVM options will now have the token `<libpath>` replaced with the path where shared libraries are deposited. This is the same value as
  found in the `java.library.path` list, but you can use this token in any system property. It's useful for libraries that don't follow the
  best practice of calling `System.loadLibrary` first before attempting to unpack a JNI library from their JAR.
* The config syntax for system properties is now more do-what-i-mean: you don't have to quote property names anymore to stop them being 
  interpreted as JSON object keys.
* The client enhancements config now has a FlatLAF system property to make it load the native libraries from the right location.
* The JDK configs have been refreshed.

### Fixes

* JAR library extraction now properly recognizes and discards shared libraries built for Android and GNU-toolchain built Solaris.

### Documentation

* A new troubleshooting section has been added.

## Conveyor 4.1

This is a bugfix release:

* Fix icon positioning in the Windows installer stub EXE to handle non-standard UI scaling factors.
* Make the Compose template app use 1.2 final.
* Resolve an exception that could occur when bundling statically linked Linux binaries.
* Fix the self-signed launch script, which appears to have been affected by a recent behavior change in Windows 10.
* Fix an issue with signing of fat binaries that affects macOS Ventura.
* Work around an issue in Windows affecting some users in which the JVM was unable to establish outbound connections.

## Conveyor 4

### New features

* The `app.site.extra-header-html` key lets you inject arbitrary HTML into the `<head>` area of the generated download page.
* Windows icons are now "unplated" (without a border) by default. This overrides the default Windows taskbar icon styling, but brings packaged apps into visual alignment with Microsoft's first party apps.
* P7B format certificates are now supported.
* Certificate validity periods are now checked before use.
* Electron apps are now supported on Linux.

### New features for JVM apps

* When `conveyor.compatibility-level` >= 4 any native libraries placed in the app inputs will be moved to the right locations in the generated package to be loaded. Native libraries found in JARs were already being extracted and placed there - this just lets you do the same thing for libraries outside of JARs.
* Fixed: the Azul JDK for Mac didn't work when combined with JMODs.

## Conveyor 3

### New features

* A significantly re-organized tutorial which shows how to adapt existing apps earlier and more explicitly.
* The [`app.windows.start-on-login` key](configs/windows.md#start-on-login), which ensures your application will be started automatically when the user logs in without any need for user interaction. 

### New features for JVM apps

* A better Gradle plugin which:
    * Supports Jetpack Compose 1.2 
    * Automatically imports the JDK from [the selected Java toolchain](https://docs.gradle.org/current/userguide/toolchains.html) when possible. This simplifies configuration further and eliminates duplicate configuration.
* The template apps were cleaned up. The Compose Desktop template app now uses Compose 1.2 (beta).
* If JDK inputs don't have a `jmods` directory, the JDK is bundled into the app as-is without using `jlink`. This helps with packaging apps using GraalVM.
* JDK standard library was refreshed.

### Smaller improvements

* Input objects now have an `extract` property which controls the default behavior of extracting zips and tarballs as they're copied into the package.
* Config under the top level `temp` key is dropped before `conveyor json` renders, allowing you to keep config clean by placing keys there that are only meant to be concatenated into other keys.
* Fixed: tarballs/zips that contain a Mac bundle no longer trigger the single-root-directory detection.
* Fixed: errors during EXE enhancement are no longer fatal.

----

## Conveyor 2.1

### Fixes and smaller improvements

* The minimum macOS version for generated apps was lowered to 10.14 (Mojave). Minimum OS verson requirements are now documented. 
* A guidebook section with [comparisons to alternative tools](faq/comparisons.md) was added.
* Fixed: packaged apps were incompatible with macOS Catalina. Thanks to Apple for assisting with debugging this.
* Fixed: the generated certificate signing requests can now be re-exported if you deleted them whilst also having a passphrase on your key.

### For JVM apps

* The `app.jvm.extract-native-libraries` key was added to control JAR stripping.
* The JDK definitions were refreshed.
* Fixed: jdeps is no longer run if it's not actually being requested via the `detect` token.
* Fixed: the `jdk.accessibility` module is now always included if AWT/Swing are. If this isn't done then apps might crash at startup if screen readers are installed.

----

## Conveyor 2

### New features

**Improved Windows end user UX.** Conveyor now gives Windows users a small EXE file that triggers download and installation of the MSIX package. This simplifies the Windows UX, makes it more familiar for end users, avoids problems with a small minority of machines that have fallen behind on software updates and enables other future features. The `.appinstaller` file is still there and used behind the scenes, so admins can easily bypass the EXE to use the packages directly.

Just like in the previous release the installation process will download only the parts of the MSIX file that the user doesn't already have on their system, will even hard link files between different apps and doesn't require admin privileges. The user will see your icon/logo whilst the download and installation proceeds, and at the end the app will launch automatically. This yields fast installs that require exactly one click. The installer EXE is a tightly written Win32 app that's just a few hundred kilobytes in size. Like any installer it can be deleted after usage, or re-run to trigger a forced upgrade check and relaunch.

**Site metadata.** Your download site now has a `metadata.properties` file in the site directory which contains information about the software in the form of `key=value` text. You can control what else gets written here by changing the `app.site.export-keys` list, which contains a list of config keys to write out. This file is useful for software that wants to know what the latest version is, without needing to deal with platform specific XML.

**New features for JVM apps.** You can now:

* Specify machine-specific JVM options
* Control the options for each launcher/machine combination independently.
* Control the Windows console mode of each launcher independently.
* Control the list of files that are cleaned up after JDK linking with the `app.jvm.unwanted-jdk-files` key. 
* Write CLI launchers using shorthand config syntax of just the class name, without an object. The name of the executable is taken by transforming the class name to `kebab-case` and removing any `Kt` extension.

### Fixes and smaller improvements

* The clipboard copy icon on the generated download page now works in Firefox.
* The error message you get when using a JDK that doesn't supply Mac builds is now more helpful.
* The new `app` task is similar to the `package` task - it builds an unpackaged app for the machine selected by the `app.machines` key.
* The default Windows timestamping authority has changed from DigiCert to Certum due to [this DigiCert issue](https://knowledge.digicert.com/solution/authenticode-signature-verification-fails-with-new-timestamping-cross-root.html).
* The JDKs available via the standard library have been refreshed. The GraalVM stdlib configs have been removed from the docs and JDK table due to the need for better GraalVM support for this feature to really shine (the stdlib configs remain so existing build configs should continue to work).
* Documentation related to code signing has been improved.
* Fixed usage with recent Azul JDKs, which contain non-standard symlinks in the macOS download.
* The PowerShell one-liner used for self-signed Windows apps now works when the web server doesn't set MIME types correctly (e.g. for GitHub Releases), and when the system is configured to restrict PowerShell.
* Fixed notarization failure when using Apple certificates linked to developer accounts that are authorized for iPhone development.
* Fixed bugs that could occur when a company name in certificates used characters requiring X.500 escape sequences.
* The template apps no longer require the system properties set by packaging to start up.
* The CMake template app uses the win32 subsystem (no console) on Windows.
* Fixed bugs that affected Linux packages built on Windows.

----

## Conveyor 1

First release with support for packaging JVM, Electron and native apps for any OS, from any OS, using a simple and intuitive config syntax and a straightforward command line tool.
