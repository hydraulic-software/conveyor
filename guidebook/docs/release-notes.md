# Release notes

## 12.0 (2nd November 2023)

### New features

* **ARM support** for Windows and Linux. This rounds out our support for the 64-bit ARM architecture (aarch64), which was already available
  for macOS. ARM packages aren't enabled by default as you probably aren't testing them but you can turn them on by adding `windows.aarch64`
  and `linux.aarch64` to your `app.machines` list. A good way to test is by using Parallels on an Apple Silicon Mac.
* **[Migration from Squirrel](migrating-electron-apps.md)**. If you already deployed your Electron app you can now easily migrate existing installs to Conveyor.  
* **[Creating ASAR files](configs/electron.md#appelectronasar)** for Electron apps. [This feature of Electron](https://www.electronjs.org/docs/latest/tutorial/asar-archives) improves performance and startup time 
  on Windows, and now Conveyor utilizes it automatically. (You must have NPM installed for this feature to work).

### Visual improvements

* **Dark mode** is now fully supported by both the Windows installer UI and the generated download page. By default it will reflect the system setting, but you can also force it with the new `app.theme` key.
* The Windows installer UI now has an improved appearance to match the Mac Sparkle update/install window.
* The generated download page now has favicons.
* The Windows entrypoint EXE resources now has the icon replaced with your app icon by default, and has any version info in it stripped. This improves what appears in the Windows firewall consent dialog when using app frameworks.

### Other improvements

* The `unvirtualizedResources` capability has been removed when `conveyor.compatibility-level >= 12`, the underlying Windows bug this works around was worked around in a different way. 
* Conveyor now flushes entries from the disk cache based on a smarter cost function. This can improve performance when frequently rebuilding apps.
* The guidebook now has a section on [understanding delta updates](understanding-delta-updates.md).
* Sparkle has been upgraded to version 2.5.1, bringing smaller downloads and compatibility improvements.
* Conveyor now warns you if your config includes another config file that isn't found. You can upgrade this to an error by using `include required(path)`.
* JVM: We now prevent you accidentally removing the `jdk.crypto.ec` module from your JVM, as it's required for many SSL/TLS connections.
* JVM: Gradle has been upgraded in the generated template apps.

Along with various minor bug fixes.

!!! note 
    For older release notes please use the version picker in the top bar.
