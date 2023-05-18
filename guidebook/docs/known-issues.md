# Known issues

## Missing features

* Supported apps and packages:
    * Only GUI apps are supported currently. Support for CLI-only apps is implemented (Conveyor is packaged with itself), but the feature needs more polish before being ready to launch. Let us know if you'd like to try it out anyway.
    * Only DEB based Linux distros get native packages. For other distros Conveyor creates a tarball which doesn't auto update. RPM / FlatPak support is on the roadmap.
    * ARM Linux.
    * ARM Windows.
* Mac App Store support (Microsoft Store support is available).
* Cloud HSM services.
* JVM: support for reading Maven classpaths on non-UNIX platforms.
* There is no `app.updates` mode that lets you preserve the ability to auto update but only trigger it from your app.

## Issues

* Setting `app.updates = none` won't remove the apt repository from the Debian package.

## Windows-specific issues

Windows apps are packaged using MSIX, which changes some aspects of the runtime environment in order to lock apps down against modification in a similar way to what other operating systems provide. You must pay attention to these aspects:

* Your installation directory will be read only (this is also true on macOS and Linux).
* Depending on Windows version, attempting to execute your app EXE directly using an absolute path from outside of the app process tree may fail with a permission error. Instead, either:
    * Invoke the EXE name that was placed on your `%PATH%` automatically at install time, which you can find under `%LOCALAPPDATA%\Microsoft\WindowsApps`. It will have the same name as your main app exe except lowercased and with `-` instead of space characters.  
    * An alternative way to start your app is by opening the `shell:appsFolder\$AUMID` URL, where `$AUMID` (app user mode id) can be obtained by running `conveyor make app-user-model-id`. An example AUMID is `TestApp_49jahnq5qzr1m!TestApp`, e.g. you can start the app via the run dialog by entering `shell:appsFolder\TestApp_49jahnq5qzr1m!TestApp`.
* You can't change your Windows signing identity without breaking the update process. This is a Windows design issue that was resolved in Windows 11, but Microsoft's solution isn't compatible with Windows 10. As such we are developing an alternative. This problem doesn't affect apps distributed via the Microsoft Store, but otherwise your signing identity can change in the following scenarios:
    * Renaming your organization.
    * Changing the location of your organizational headquarters.
    * Going from an OV to an EV certificate.
    * Changing your personal name if not using an organizational certificate.
    * In some cases, changing from one certificate authority to another.
* The `AppData` directory is [virtualized](configs/windows.md#virtualization). This means:
    * Files stored under `AppData` will be cleaned up on uninstall automatically. This is usually what you want, but it's best to be aware of that and not store data the user can't afford to lose there.
    * Your app won't be able to see the `AppData` directories of other apps unless you adjust your configuration to de-virtualize those directories. 
    * UNIX domain sockets may fail if you create them in a virtualized directory on some versions of Windows. Conveyor 8+ configures your package to devirtualize the Temp directory so sockets created there should work, but sockets created elsewhere may encounter errors. [Devirtualize](configs/windows.md#virtualization) a directory if you plan to create UNIX domain sockets there.
* DLLs are no longer searched for in the `%PATH%`. If you want to load DLLs from other installed applications, you should locate that directory and then use `LoadLibraryEx` with the `LOAD_WITH_ALTERED_SEARCH_PATH` flag, passing in an absolute path to the desired DLL.

## Low priority issues 

* If you start a Windows Terminal via the shift-right click menu in Explorer, you'll get plain text progress tracking instead of the animated progress bars. Start Windows Terminal in the normal way as a workaround.
* Changes in terminal size whilst a build is in progress won't be respected.
* When using JDK11, you must use patch level 16+ (i.e. JDK 11.0.16+). Earlier builds will fail with a jlink error talking about hash mismatches. This is due to a format change that got backported to JDK11.
