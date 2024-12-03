# Known issues

## Missing features

* Supported apps and packages:
    * Only GUI apps are supported currently. Support for CLI-only apps is implemented (Conveyor is packaged with itself), but the feature 
      needs more polish before being ready to launch. Let us know if you'd like to try it out anyway.
    * Only DEB based Linux distros get native packages. For other distros Conveyor creates a tarball which doesn't auto update. 
      RPM / FlatPak support is on the roadmap.
* Mac App Store support (Microsoft Store support is available).
* Support for the new Microsoft Azure Trusted Signing service.
* Support for selecting the region used in DigiCert KeyLocker.

## Windows specific issues

* Conveyor builds are much slower on Windows than on UNIX. This is due to the low performance of the Windows kernel filesystem. See [Build Performance](performance.md) for tips to speed up your builds.
* Conveyor supports both Windows 10 and 11 but assumes users are applying software updates. We don't support arbitrarily old versions of Windows 10, as machines that aren't updating often contain severe bugs or have malware problems which make testing them hard.

Windows apps are packaged using MSIX, which changes some aspects of the runtime environment in order to improve security and robustness.
Be aware of the following.

### Read only install directory

Your installation directory will be read only (this is also true on macOS and Linux). If you need to store data, put it into the `AppData` folder in the user's home directory.

### Identity changes

Changing the package name or signing identity after launch will cause updates to stop working until the user next runs the app. [Learn more about name changes](name-changes.md).

### AppData directories

The `AppData` directory is [virtualized](configs/windows.md#virtualization). This means:

* Files stored under `AppData` will be cleaned up on uninstall automatically. This is usually what you want, but it's best to be 
  aware of that and not store data the user can't afford to lose there.
* Your app won't be able to see the `AppData` directories of other apps unless you adjust your configuration to de-virtualize those directories.
  This should only matter if your app is intended to modify others.

### DLL search path

DLLs are no longer searched for in the `%PATH%`. Microsoft do this to improve security and robustness, but can break apps that assume DLLs can
be loaded from other apps installed by the user. If you want to load DLLs from other installed applications, you should locate that 
directory and then use `LoadLibraryEx` with the `LOAD_WITH_ALTERED_SEARCH_PATH` flag, passing in an absolute path to the desired DLL. 
Alternatively you can use the [SetDllDirectory](https://learn.microsoft.com/en-us/windows/win32/api/winbase/nf-winbase-setdlldirectorya) 
API to restore the previous search behavior for the current process. 

## Linux specific issues

* On older Ubuntu/Debian versions (e.g. 20 LTS), `apt` can't upgrade from repositories hosted on GitHub Releases. This is due to a bug in 
  `apt` and is fixed in later versions.
* New versions of Ubuntu claim the generated `apt` repository uses "weak algorithms". [This warning is incorrect and there are no known weaknesses in any of the algorithms used](https://discourse.ubuntu.com/t/new-requirements-for-apt-repository-signing-in-24-04/42854). We are working with Ubuntu to try and get them to change the warning; if they don't then we'll change how the repositories are signed anyway.
* On Ubuntu 24 we have received reports that YubiKeys signing doesn't work properly. Downgrading to Ubuntu 22 fixes things. This appears to be a YubiKey/Ubuntu issue rather than a Conveyor bug, but will surface as a Conveyor build error mentioning `CKR_DEVICE_ERROR`.

## macOS specific issues

* Your app will only update when running. Unlike on Windows and Linux (Debian/Ubuntu), the operating system won't automatically upgrade the
  app in the background. This is a limitation of the Sparkle framework Conveyor uses.
* Your app won't update until the user moves it out of their Downloads folder. This is due to a macOS security mechanism called "app translocation" and affects all Mac apps, it's not Conveyor specific. The user will be informed of this on startup and we modified the Sparkle update framework to present a clearer message to the user explaining what to do (we've also translated it into many different languages).

## Java specific issues

* When using JDK11, you must use patch level 16+ (i.e. JDK 11.0.16+). Earlier builds will fail with a jlink error talking about hash mismatches. This is due to a format change that got backported to JDK11.
* There's no support for reading Maven classpaths into config when building on Windows.

## Low priority issues 

* If you start a Windows Terminal via the shift-right click menu in Explorer, you'll get plain text progress tracking instead of the animated progress bars. Start Windows Terminal in the normal way as a workaround.
* Changes in terminal size whilst a build is in progress won't be respected.
