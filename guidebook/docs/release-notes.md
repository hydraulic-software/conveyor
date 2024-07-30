# Release notes

## Conveyor 15

This is a feature release.

### :material-api: Update control API

Conveyor 15 adds an API that your app can use to interact with online updates. It's available for [Electron](control-api-electron.md),
[JVM](control-api-jvm.md) and [native](control-api-native.md) apps. It lets you check if there are new versions available at the update site
as well as trigger an update/restart cycle, and so can be used to implement alternative update policies that aren't supported out of the
box.

The [`app.updates`](configs/update-modes.md) key now supports `MANUAL` as well as `NONE`. The latter lets you disable update support 
entirely, yielding a smaller package in some cases. It's appropriate when you don't want Conveyor to handle updates for you. `MANUAL`
turns off automatic updates whilst still allowing updates to be triggered using the control API.

The scaffold apps you can make using `conveyor generate` have been updated to demonstrate how to use this API. 

### :simple-windows: Windows

[The `app.windows.package-extras` key and CPU specific variants](configs/windows.md#appwindowsamd64aarch64package-extras) allow files to be added outside the app-specific subdirectory. 
This is only useful for Electron and JVM apps where the root inputs are relocated to a conventional subdirectory that keeps them separated 
from the runtime.

When signing with Sectigo certificates Conveyor now forces the use of the legacy cross-signed root. This improves compatibility with Windows
10 machines that aren't properly downloading root store updates from Microsoft, which can happen due to incorrect or over-aggressive operating
system settings.

### :simple-apple: macOS

[The `app.mac.skip-framework-symlink-removal` key](configs/mac.md) allows frameworks to be exempted from the simplifying transform that removes 
redundant symlinks from frameworks.

You can now configure update checks to occur as frequently as once a minute, down from the previous limit of once an hour. This makes
testing updates integration easier.

### :simple-electron: Electron

The handling of symlinked packages has been improved. This is useful when your app depends on NPM packages that aren't uploaded to a package
registry and where you've used npm's symlink ability to point into your dev tree.
