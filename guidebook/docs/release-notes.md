# Release notes

## Conveyor 14.1 (April 13 2024)

This is a bugfix release:

* Windows: Fix a bug that could occur if your input files contained two files with the same file extension but mismatched upper/lower case.
* Linux: Fix a bug with file and URL associations. If you use these and ship to Linux, an upgrade is recommended.
* Linux: Change the copy/pasteable script to use a way of getting the CPU arch that works on a minimal Ubuntu install.

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
