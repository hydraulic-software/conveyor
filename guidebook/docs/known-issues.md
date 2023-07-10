# Known issues

## Missing features

* Supported apps and packages:
    * Only GUI apps are supported currently. Support for CLI-only apps is implemented (Conveyor is packaged with itself), but the feature 
      needs more polish before being ready to launch. Let us know if you'd like to try it out anyway.
    * Only DEB based Linux distros get native packages. For other distros Conveyor creates a tarball which doesn't auto update. 
      RPM / FlatPak support is on the roadmap.
    * ARM Linux.
    * ARM Windows.
* Mac App Store support (Microsoft Store support is available).
* There is no `app.updates` mode that lets you preserve the ability to auto update but only trigger it from your app.
* JVM: support for reading Maven classpaths on non-UNIX platforms.

## Windows specific issues

Windows apps are packaged using MSIX, which changes some aspects of the runtime environment in order to improve security and robustness.
Be aware that:

* Your installation directory will be read only (this is also true on macOS and Linux).
* Some names and identities can't be changed after launch without breaking the update process. [Learn more about name changes](name-changes.md).

### Invoking packaged executables

Depending on Windows version, attempting to execute your app EXE directly using an absolute path from _outside_ your app process tree 
may fail with a permission error (e.g. from the Run dialog box). Instead, either:

  * Invoke the EXE name that was placed on your `%PATH%` automatically at install time, which you can find under 
    `%LOCALAPPDATA%\Microsoft\WindowsApps`. It will have the same name as your main app exe except lowercased and with `-` instead of 
    space characters.  
  * An alternative way to start your app is by opening the `shell:appsFolder\$AUMID` URL, where `$AUMID` (app user mode id) can be 
    obtained by running `conveyor make app-user-model-id`. An example AUMID is `TestApp_49jahnq5qzr1m!TestApp`, e.g. you can start 
    the app via the run dialog by entering `shell:appsFolder\TestApp_49jahnq5qzr1m!TestApp`.

This doesn't affect your app starting its own EXE or other EXEs in the package, or EXEs outside your package.

### AppData directories

The `AppData` directory is [virtualized](configs/windows.md#virtualization). This means:

* Files stored under `AppData` will be cleaned up on uninstall automatically. This is usually what you want, but it's best to be 
  aware of that and not store data the user can't afford to lose there.
* Your app won't be able to see the `AppData` directories of other apps unless you adjust your configuration to de-virtualize those directories.
  This should only matter if your app is intended to modify others.

!!! warning "UNIX domain sockets"
    UNIX domain sockets may fail if you create them in a virtualized directory on some versions of Windows. Conveyor 8+ configures your 
    package to devirtualize the Temp directory so sockets created there should work, but sockets created elsewhere may encounter errors. 
    [Devirtualize](configs/windows.md#virtualization) a directory if you plan to create UNIX domain sockets there.

### DLL search path

DLLs are no longer searched for in the `%PATH%`. Microsoft do this to improve security and robustness, but can break apps that assume DLLs can
be loaded from other apps installed by the user. If you want to load DLLs from other installed applications, you should locate that 
directory and then use `LoadLibraryEx` with the `LOAD_WITH_ALTERED_SEARCH_PATH` flag, passing in an absolute path to the desired DLL. 
Alternatively you can use the [SetDllDirectory](https://learn.microsoft.com/en-us/windows/win32/api/winbase/nf-winbase-setdlldirectorya) 
API to restore the previous search behavior for the current process. 

## Linux specific issues

* Setting `app.updates = none` won't remove the `apt` repository from the Debian package.
* On older Ubuntu/Debian versions (e.g. 20 LTS), `apt` can't upgrade from repositories hosted on GitHub Releases. This is due to a bug in 
  `apt` and is fixed in later versions.

## macOS specific issues

* Your app will only update when running. Unlike on Windows and Linux (Debian/Ubuntu), the operating system won't automatically upgrade the
  app in the background. This is a limitation of the Sparkle framework Conveyor uses.

## Low priority issues 

* If you start a Windows Terminal via the shift-right click menu in Explorer, you'll get plain text progress tracking instead of the animated progress bars. Start Windows Terminal in the normal way as a workaround.
* Changes in terminal size whilst a build is in progress won't be respected.
* When using JDK11, you must use patch level 16+ (i.e. JDK 11.0.16+). Earlier builds will fail with a jlink error talking about hash mismatches. This is due to a format change that got backported to JDK11.
