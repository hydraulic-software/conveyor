# Release notes

## Conveyor 14.2

* Windows: Improves the cache hit ratio for signed binaries, which should help preserve expensive signature operations when using DigiCert cloud signing.
* Mac: Frameworks are now relocated to avoid symlinks when possible. This should improve compatibility with third party zip utilities that don't implement the zip spec correctly. A warning is printed if symlinks are found from your own inputs.
* Mac: Resolved an issue that could cause icons to lack sharpness on some display types.
* Linux: `app.updates = NONE` now works correctly for Debian packages too. 
* JVM: Gradle plugin: now is compatible with the configuration cache, making it much faster.
* JVM: Gradle plugin: modules with conflicting names are now renamed inside the package.
* JVM: Gradle plugin: fix a bug that could cause a failure to find the right configuration name in the latest Compose Gradle project setups ([#108](https://github.com/hydraulic-software/conveyor/issues/108))
* JVM: Upgraded dependency versions in the Compose and JavaFX apps template apps.
* JVM: Upgraded the JDK definitions in the standard library.
* Electron: Upgraded template app to Electron 30.

## Conveyor 14.1 (April 13 2024)

This release fixes a bug with Linux file and URL associations. If you use these and ship to Linux, an upgrade is recommended.

## Conveyor 14 (March 19 2024)

This release adds some features useful for large enterprises. 

* You can now customize the signing process by supplying custom scripts (see [`app.mac.sign.scripts`](configs/mac.md#signing) and [`app.windows.sign.scripts`](configs/windows.md#signing)). This lets you use Conveyor in environments where you have your own internal signing servers.
* You can now add custom root TLS certificates to your JVM app's trust store using the [`app.jvm.additional-ca-certs`](configs/jvm.md#appjvmadditional-ca-certs) key. This is useful for cases where you don't really need a CA certificate, such as when you control both the client and server side of a connection.

### Usability improvements

* Remap specs now interpret a rule ending in `/` as a glob `/**` meaning "all files under this directory".
* You can now set a timeout for the macOS notarization process using the `app.mac.notarization.timeout-seconds` key, and allow the build to proceed even if stapling fails. This is useful for expensive CI systems where you don't want to wait for the notarization process to complete, and are willing to tolerate a slow first-start experience for your users (or inability to launch if the first start is offline).
* If you refer to an unset environment variable the error message now reminds you to use the `export` keyword.
* Conveyor now catches attempts to sign Mac apps using a Mac Installer certificate (meant for .pkg files), rather than a standard Developer ID certificate.
* Changing the site URL for a commercial license now requires an explicit command. The previous approach was a little too frictionless and sometimes users changed the app associated with a license key by accident.

### Bugfixes

* Conveyor now handles non-UTF8, non-English Windows systems better.
* Fixed a crash that could occur if you don't have `npm` installed whilst packaging an Electron project or if it's too old, and detects the case where `node_modules` directories contain symlinked packages.
* We fixed another case where exploring the cache directory with the macOS Finder could break things due to hidden `.DS_Store` files littering the place up, and audited the rest of the caching code to make sure it's robust against other Finder-related issues.
* For JVM apps the Gradle plugin now handles the newest style of Compose Multiplatform projects properly.
* Fixed a bug for server packages that contain Apache config templates. 
