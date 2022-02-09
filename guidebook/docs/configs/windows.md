# Windows

## Synopsis

```properties
app.windows {
  # Windows specific inputs.
  inputs = [ ... ]

  # Controls whether a terminal window appears on startup.
  console = false

  # What online update style to use.
  updates = background

  # Whether to sign the EXE/DLL files or not (true/false).
  sign = ${app.sign}
}
```

## Update keys

**`app.windows.updates`** Controls the update user experience. One of the following:

* `background`: The default. The user will not see update prompts and the app will stay up to date in the background. Windows will check every 8 hours for an update regardless of whether the app is being used or not. This is the closest you can get to how Chrome does updates.
* `aggressive`: Windows will check for an update every time the user launches the app. If one is available, the user will be required to wait whilst it's applied, although it's possible to cancel in which case the update will be completed in the background.
* `null`: No automatic updates. The user can still trigger an update by re-visiting the generated download page and clicking install/download again.

Or it can be an object containing the keys below:

**`app.windows.updates.launch-check-frequency`** In hours. How much time must elapse before Windows re-checks with the server to see if an update has been released. If set to null, Windows will never check on launch. If set to zero, Windows always checks.

**`app.windows.updates.block-start`** True/false. If true then the user won't be allowed to start the app unless they accept the upgrade.

**`app.windows.updates.prompt`** True/false. If true, the user will be prompted when an update is available. If false, they won't be. Note that on some versions of Windows this doesn't work and a prompt is always shown.

??? warning
    In aggressive mode Windows has a bug in which after an update is applied the app won't be immediately re-launched. The taskbar/start code seems to be in some sort of sleep state where it doesn't notice the update finished and that the launch should resume. The moment the user interacts with the start button or search bar in any way, the app launch proceeds, which could be confusing. For this reason we don't currently recommend aggressive mode updates unless strictly required.

## Signing keys

**`app.windows.timestamping-server`** URL of a cryptographic timestamping server (often called a timestamping authority or TSA). These are usually run by certificate authorities. Defaults to using DigiCert because their timestamping server is fast and well run, but you can use any TSA that is trusted by Windows.

**`app.windows.sign`** Boolean, controls whether to sign the Windows EXE/DLL/package files or not. If false then you can't produce an MSIX file. Defaults to the value of `${app.sign}` (which is true).

**`app.windows.signing-key`**, **`app.windows.certificate`** See [signing keys](index.md#signing).

## Console key

**`app.windows.console`** Windows requires a program to declare up front if it's a command line or GUI app using a flag in the EXE file headers. If an app is declared GUI then anything it prints to stdout/stderr won't appear anywhere, not even when run from the command line. If it's declared to be a command line app and run from outside a terminal, then a console window will appear alongside the app's main window. 

Normally this header is set by the compiler. Conveyor can set it for you to the value of the `app.windows.console` key. If you don't specify this setting then Conveyor will attempt to guess, based on the presence of DLLs that are known to be used by JVM GUI toolkits. If DLLs for AWT/Swing, JavaFX, SWT, Skiko/JetPack Compose or LWJGL are present then it's believed to be a GUI app. Otherwise it's a console app. If this heuristic isn't good enough then please let us know so we can update it, and then set the key explicitly.

When a package contains console mode EXEs your MSIX package will register itself as a command line app. The moment installation completes it becomes accessible in all running terminal sessions.

What if your app is both a CLI *and* a GUI app simultaneously? You can certainly still accept command line switches, even for GUI mode apps. It's only console output and input that's affected by this flag. If you need both then you will need to prepare separate EXEs for your app, one for each mode, and provide them as Windows-specific inputs. The GUI mode exe should have the same name as the generated launcher would: `${app.display-name}.exe`. The CLI mode exe can be named whatever you want.

## Manifest keys

Manifests are XML files that control how your app is managed by the operating system. There are two different manifests, but they use some of the same data.

**`app.windows.manifests.version-quad`** A four-part numeric version derived from `app.version` if not specified. Example: "1.2.3.4".

**`app.windows.manifests.validate`** The MSIX manifest XML is validated against Microsoft's schemas because non-validating XML won't install. In the unlikely event this goes wrong and you need to disable it, set this to false. Normally you will never encounter validation errors, but if you alter the manifest properties or provide your own XML you may encounter such issues.

### The MSIX manifest

An XML file in the final generated MSIX package. It controls many different integration points with the OS, including:

* Package metadata, which controls how your app appears in the confirmation screen the user sees when they open the package.
* Compatibility information: min Windows version required and max version tested, which can be used to tell the user they can't install the app, and which may change how Windows applies backwards compatibility logic.
* The name of the EXE file that should be run from the start menu.
* Permission/capability requests.

[A full schema is available here](https://docs.microsoft.com/en-us/uwp/schemas/appxpackage/uapmanifestschema/schema-root). Some parts of the default manifest can be controlled via configuration, but you can also completely replace the manifest with your own.

**`app.windows.manifests.msix.min-version`** Which version of Windows is required to run this application. Defaults to `10.0.17763.0` i.e. Windows 10 build 17763, which was released in November 2018 (codename "Redstone 5").

**`app.windows.manifests.msix.max-version-tested`** Which version of Windows the app has been tested on. This is used by Windows to detect packages for apps that have stopped being maintained and thus may need workarounds for bugs. The version this is set to will be updated with time to reflect whatever the latest versions of Windows 10 are, reflecting the assumption that if you're rebuilding your packages then the app is in active use and being tested.

**`app.windows.manifests.msix.capabilities = [ "rescap:runFullTrust" ]`**  The list of requested permissions, [as documented by Microsoft](https://docs.microsoft.com/en-us/windows/uwp/packaging/app-capability-declarations). A string can be prefixed by a namespace code like `uap:` or `uap2:` to put the `<Capability/>` tag into the right namespace.

By default the app requests `rescap:runFullTrust` which is intended for normal Win32 apps, and means the app is hardly sandboxed at all. A small amount of filesystem virtualization is applied to ensure the app can be uninstalled cleanly and apps aren't allowed to write to their own install folder, but that's about it. You shouldn't list any other capabilities unless you are explicitly using modern UWP Windows APIs that support sandboxing.

The list of available XML namespaces is:

* `com:` http://schemas.microsoft.com/appx/manifest/com/windows10
* `com2:` http://schemas.microsoft.com/appx/manifest/com/windows10/2
* `desktop:` http://schemas.microsoft.com/appx/manifest/desktop/windows10
* `desktop2:` http://schemas.microsoft.com/appx/manifest/desktop/windows10/2
* `desktop3:` http://schemas.microsoft.com/appx/manifest/desktop/windows10/3
* `desktop4:` http://schemas.microsoft.com/appx/manifest/desktop/windows10/4
* `f2:` http://schemas.microsoft.com/appx/manifest/foundation/windows10/2
* `iot:` http://schemas.microsoft.com/appx/manifest/iot/windows10
* `rescap:` http://schemas.microsoft.com/appx/manifest/foundation/windows10/restrictedcapabilities
* `rescap2:` http://schemas.microsoft.com/appx/manifest/foundation/windows10/restrictedcapabilities/2
* `rescap3:` http://schemas.microsoft.com/appx/manifest/foundation/windows10/restrictedcapabilities/3
* `rescap4:` http://schemas.microsoft.com/appx/manifest/foundation/windows10/restrictedcapabilities/4
* `rescap6:` http://schemas.microsoft.com/appx/manifest/foundation/windows10/restrictedcapabilities/6
* `uap:` http://schemas.microsoft.com/appx/manifest/uap/windows10
* `uap2:` http://schemas.microsoft.com/appx/manifest/uap/windows10/2
* `uap3:` http://schemas.microsoft.com/appx/manifest/uap/windows10/3
* `uap4:` http://schemas.microsoft.com/appx/manifest/uap/windows10/4
* `uap5:` http://schemas.microsoft.com/appx/manifest/uap/windows10/5
* `uap6:` http://schemas.microsoft.com/appx/manifest/uap/windows10/6
* `uap7:` http://schemas.microsoft.com/appx/manifest/uap/windows10/7

**`app.windows.manifests.msix.identity-name`** An ASCII name for the program used for internal identification, that doesn't have to be globally unique. Should be written in lower case `kebab-case` and defaults to `${app.fsname}`. It will be converted to `PascalCase` to match the normal Windows style.

**`app.windows.manifests.msix.{display-name,description,vendor}`** Package metadata that will appear in the Windows user interface. Taken from the top level app metadata by default but can be overridden.

**`app.windows.manifests.msix.content`** If set, supplies a string containing a complete manifest that replaces the standard one. The other keys will be ignored in this case, as they are only used to customize the built-in template.

### The EXE manifest

An [application manifest](https://docs.microsoft.com/en-us/windows/win32/sbscs/application-manifests) is an XML file embedded into the executable of a program. It controls:

* Backwards compatibility modes.
* Whether the app needs administrator privileges or not ([see here for more info](https://docs.microsoft.com/en-us/previous-versions/bb756929(v=msdn.10))).
* HiDPI scaling options.
* Scroll event resolution.

And a variety of other operating system behaviours. The default manifest provided by Conveyor is used for JVM apps and should be sufficient. For non JVM apps your toolchain should embed a manifest itself, although future versions of Conveyor may support setting the manifest for any EXE file. 

You can adjust some of the values using config keys; if they don't meet your needs then just replace the entire content.

**`app.windows.manifests.exe.content`** The EXE manifest XML embedded into the binary. It incorporates the following keys into the default XML content:

**`app.windows.manifests.exe.requested-execution-level`** Controls whether the user sees a privilege escalation prompt when running. One of:

* `asInvoker` - whatever privilege level the user has.
* `highestAvailable` - whatever privilege level the user can potentially escalate to.
* `asAdministrator` - requires administrator access and cannot run without it.

