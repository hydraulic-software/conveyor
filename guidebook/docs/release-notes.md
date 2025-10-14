# Release notes

## Conveyor 20.1

- Mac: Upgraded to Sparkle 2.8.0 which has some security fixes and a UI refresh for macOS Tahoe.

## Conveyor 20 (September 2025)

- A new update mode has been added: `aggressive-if-possible`, which is like regular aggressive updates in that it gives synchronous update without user interaction on each app start, but will allow the user to keep using old versions if they have not upgraded their OS to meet a new minimum.
- This release improves the Windows installer experience when there are problems. The installer now opens a web page and reports the error to us instead of showing a dialog box. The web page adapts the advice given based on what error is encountered. The installer now also reports telemetry in the case of a Windows system in which core services have been shut down, as this corrupt system state can cause errors and we're investigating how users can get into this state.
- Minor bug fixes.
- JVM: Refresh the JDK library and sample apps. Java 25 is currently not included.

## Older versions

Please use the version selector at the top to view older release notes.