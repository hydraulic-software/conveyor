# Release notes

## Conveyor 15

This release adds the following config options:

* [The `app.windows.package-extras` key and CPU specific variants](configs/windows.md#appwindowsamd64aarch64package-extras) allow files to
  be added outside of the app-specific subdirectory. This is only useful for Electron and JVM apps where the root inputs are relocated to
  a conventional subdirectory that keeps them separated from the runtime.
* [The `app.mac.skip-framework-symlink-removal` key](configs/mac.md) allows frameworks to be exempted from the simplifying transform that
  removes redundant symlinks from frameworks.
