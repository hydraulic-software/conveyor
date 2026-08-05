# Release notes

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
