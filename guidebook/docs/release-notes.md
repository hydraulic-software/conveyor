# Release notes

## Conveyor 22.2 (September 1 2026)

- Windows launchers now contain standard version metadata, and packaged update checkers no longer request administrator privileges when the standalone installer does.
- Fixed custom Windows signing configurations that use a single script for both files and containers.
- Native Python extension modules are now signed in macOS app bundles, and Conveyor warns when native executables are missing the rpath needed to load bundled frameworks.
- Fixed JVM image creation dropping JCEF sidecar files, and select a compatible linker when packaging with JDK 26.
- Fixed ZIP symlinks so archives unpack correctly with Apple Archive Utility.
- Fixed native Electron modules being missed when Gradle stores their JARs in hidden cache directories, and ensure packaged Electron apps know their update-site URL.
- Improved automatic main-class selection for JARs containing multiple candidates and diagnostics for entry points that require a newer Java version.
- Updated generated Compose Multiplatform projects to the current JetBrains template layout.
- Improved diagnostics for Windows secure-connection failures and support for regional DigiCert ONE endpoints.

## Conveyor 22.1 (July 21 2026)

- Fix a crash when using DigiCert's remote CA due to a protocol change on their side.
- Fix a bug that could cause CONVEYOR_JVM_OPTIONS to be ignored in some circumstances.
- Keys containing lists can now be exported to `metadata.properties`
- For JVM projects: Work around a regression in new versions of Gradle.
- Fix a bug that could cause relative paths to be resolved incorrectly when the config key declaring them contained an environment variable interpolation ([GitHub issue #81](https://github.com/hydraulic-software/conveyor/issues/81))
- For JVM projects: updates to the included JDK definitions.

## Conveyor 22 (March 21 2026)

- :simple-linux: The set of mirrors used to fetch Ubuntu/Debian package metadata has been changed to avoid rate limiting on the main mirrors, and to add the ports repos.
- :fontawesome-brands-java: You can now let users [set JVM command line arguments via environment variables](configs/jvm.md#appjvmallowed-options). This should be used as a last resort for resolving support cases, as the user will have to configure the environment variables themselves.
- :fontawesome-brands-java: The version of the Gradle plugin used by the project templates has been updated. The new version improves compatibility with Gradle 9. Additionally the set of JDKs known out of the box has been updated.

### Bug fixes

- Cases where there is no root key password but the HSM does have a password now work correctly.
- The disk cache's eviction algorithm has been improved.
- A bad interaction between JavaFX and Debian/Ubuntu package dependencies in newer releases has been fixed.
- Fixed a bug that could generate a bad update site when Windows ARM64 is used in combination with identity rotation.

## Older versions

Please use the version selector at the top to view older release notes.
