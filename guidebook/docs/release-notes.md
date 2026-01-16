# Release notes

## Conveyor 21.1 (Jan 17 2025)

- Fixed a regression in macOS code signing that could affect some apps using Electron or JCEF. Upgrading from v21.0 is highly recommended.
- Improved memory usage when computing delta updates for macOS. Memory requirements should be lower now.

## Conveyor 21

- :simple-apple: macOS
    - Upgraded to Sparkle 2.8.0 which has some security fixes and a UI refresh for macOS Tahoe.
- :fontawesome-brands-windows: Windows
    - Improved installer reliability by adding more workarounds for Windows bugs, driven by diagnostics reports. Also improved the diagnostics to search for more Windows issues (e.g. damage caused by "debloater" scripts).
    - The error web page the user sees when their disk is full has been made more helpful. 
    - Added the `app.windows.installer.elevate` key that makes the installer request administrator access via UAC. You should rarely need to set this as it's inferred automatically from the manifests embedded in EXE files.
- :fontawesome-brands-java: Java 25 now has entries in the config stdlib.
- :simple-kotlin: The Compose project template now adds ARM versions of the Compose libraries.
- Updated some dependencies.

## Older versions

Please use the version selector at the top to view older release notes.