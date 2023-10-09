# Release notes

## New features

* **ARM support** for Windows and Linux. This rounds out our support for the 64-bit ARM architecture (aarch64), which was already available
  for macOS. ARM packages aren't enabled by default as you probably aren't testing them but you can turn them on by adding `windows.aarch64`
  and `linux.aarch64` to your `app.machines` list. A good way to test is by using Parallels on an Apple Silicon Mac.
* **[Creating ASAR files](configs/electron.md#appelectronasar)** for Electron apps. [This feature of Electron](https://www.electronjs.org/docs/latest/tutorial/asar-archives) improves performance and startup time 
  on Windows, and now Conveyor utilizes it automatically. (You must have NPM installed for this feature to work).
* **Smarter disk cache**. Conveyor now flushes entries from the disk cache based on a smarter cost function. This can improve
  performance when frequently rebuilding apps. 
* The generated download page now has favicons.
* The guidebook now has a section on [understanding delta updates](understanding-delta-updates.md).

Along with various minor bug fixes.

!!! note 
    For older release notes please use the version picker in the top bar.
